package com.github.cfogrady.vitalwear

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Configuration
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.activity.MainScreenComposable
import com.github.cfogrady.vitalwear.activity.PartnerScreenComposable
import com.github.cfogrady.vitalwear.battle.composable.*
import com.github.cfogrady.vitalwear.battle.data.BEMBattleLogic
import com.github.cfogrady.vitalwear.battle.data.BattleService
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.complications.PartnerComplicationState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.*
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.training.ExerciseScreenFactory
import java.time.LocalDate
import java.util.Random

class VitalWearApp : Application(), Configuration.Provider {
    private val spriteBitmapConverter = SpriteBitmapConverter()
    val firmwareManager = FirmwareManager(spriteBitmapConverter)
    val partnerComplicationState = PartnerComplicationState()
    private lateinit var imageScaler : ImageScaler
    lateinit var bitmapScaler: BitmapScaler
    lateinit var cardLoader : CardLoader
    lateinit var database : AppDatabase
    lateinit var characterManager: CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager
    lateinit var backgroundManager: BackgroundManager
    lateinit var battleService: BattleService
    lateinit var vitalBoxFactory: VitalBoxFactory
    lateinit var partnerScreenComposable: PartnerScreenComposable
    lateinit var mainScreenComposable: MainScreenComposable
    lateinit var fightTargetFactory: FightTargetFactory
    lateinit var exerciseScreenFactory: ExerciseScreenFactory
    lateinit var sharedPreferences: SharedPreferences
    lateinit var stepService: SensorStepService
    lateinit var shutdownReceiver: ShutdownReceiver
    lateinit var shutdownManager: ShutdownManager
    var backgroundHeight = 0.dp

    override fun onCreate() {
        super.onCreate()
        buildDependencies()
        applicationContext.registerReceiver(shutdownReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
        SensorStepService.setupDailyStepReset(this)
        val appShutdownHandler = AppShutdownHandler(shutdownManager, sharedPreferences)
        stepService.handleBoot(LocalDate.now())
        // This may be run on app shutdown by Android... but updating or killing via the IDE never triggers this.
        Runtime.getRuntime().addShutdownHook(appShutdownHandler)
    }

    fun buildDependencies() {
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        //TODO: Should replace sharedPreferences with datastore (see https://developer.android.com/training/data-storage/shared-preferences)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        cardLoader = CardLoader(applicationContext, spriteBitmapConverter)
        characterManager = CharacterManager()
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepService = SensorStepService(characterManager, sharedPreferences, sensorManager)
        // BEMUpdater initializes the WorkManager, so all dependencies must have already been called.
        // TODO: change BEMUpdater to get the WorkManager instance dynamically as needed instead of as a dependency
        characterManager.init(database.characterDao(), cardLoader, BEMUpdater(applicationContext))
        firmwareManager.loadFirmware(applicationContext)
        backgroundManager = BackgroundManager(cardLoader, firmwareManager)
        val random = Random()
        val bemBattleLogic = BEMBattleLogic(random)
        battleService = BattleService(cardLoader, characterManager, firmwareManager, bemBattleLogic, random)
        imageScaler = ImageScaler(applicationContext.resources.displayMetrics, applicationContext.resources.configuration.isScreenRound)
        backgroundHeight = imageScaler.scaledDpValueFromPixels(ImageScaler.VB_HEIGHT.toInt())
        bitmapScaler = BitmapScaler(imageScaler)
        vitalBoxFactory = VitalBoxFactory(imageScaler, ImageScaler.VB_WIDTH.toInt(), ImageScaler.VB_HEIGHT.toInt())
        val opponentSplashFactory = OpponentSplashFactory(bitmapScaler)
        val opponentNameScreenFactory = OpponentNameScreenFactory(bitmapScaler, backgroundHeight)
        val readyScreenFactory = ReadyScreenFactory(bitmapScaler, backgroundHeight)
        val goScreenFactory = GoScreenFactory(bitmapScaler, backgroundHeight)
        val attackScreenFactory = AttackScreenFactory(bitmapScaler, backgroundHeight)
        val hpCompareFactory = HPCompareFactory(bitmapScaler, backgroundHeight)
        val endFightReactionFactory = EndFightReactionFactory(bitmapScaler, firmwareManager, characterManager, backgroundHeight)
        fightTargetFactory = FightTargetFactory(battleService, vitalBoxFactory, opponentSplashFactory, opponentNameScreenFactory, readyScreenFactory, goScreenFactory, attackScreenFactory, hpCompareFactory, endFightReactionFactory)
        exerciseScreenFactory = ExerciseScreenFactory(characterManager, vitalBoxFactory, bitmapScaler, backgroundHeight)
        partnerScreenComposable = PartnerScreenComposable(bitmapScaler, backgroundHeight, stepService)
        mainScreenComposable = MainScreenComposable(characterManager, firmwareManager, backgroundManager, imageScaler, bitmapScaler, partnerScreenComposable, vitalBoxFactory)
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardLoader)
        shutdownManager = ShutdownManager(stepService, characterManager)
        shutdownReceiver = ShutdownReceiver(shutdownManager)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        // After we've setup the workManagerConfiguration, start the service
        return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(characterManager, stepService)).build()
    }
}