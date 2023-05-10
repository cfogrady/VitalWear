package com.github.cfogrady.vitalwear

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Configuration
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.activity.MainScreenComposable
import com.github.cfogrady.vitalwear.activity.PartnerScreenComposable
import com.github.cfogrady.vitalwear.battle.composable.*
import com.github.cfogrady.vitalwear.battle.data.BEMBattleLogic
import com.github.cfogrady.vitalwear.battle.BattleService
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.character.mood.BEMMoodUpdater
import com.github.cfogrady.vitalwear.character.mood.MoodBroadcastReceiver
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.complications.PartnerComplicationState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ScrollingNameFactory
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.*
import com.github.cfogrady.vitalwear.debug.ExceptionService
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import com.github.cfogrady.vitalwear.training.ExerciseScreenFactory
import com.github.cfogrady.vitalwear.vitals.VitalService
import com.github.cfogrady.vitalwear.workmanager.VitalWearWorkerFactory
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies
import com.google.common.collect.Lists
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Random

class VitalWearApp : Application(), Configuration.Provider {
    private val spriteBitmapConverter = SpriteBitmapConverter()
    val firmwareManager = FirmwareManager(spriteBitmapConverter)
    val partnerComplicationState = PartnerComplicationState()
    val exceptionService = ExceptionService()
    private val sensorThreadHandler = SensorThreadHandler()
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
    lateinit var heartRateService : HeartRateService
    lateinit var moodBroadcastReceiver: MoodBroadcastReceiver
    lateinit var scrollingNameFactory: ScrollingNameFactory
    lateinit var saveService: SaveService
    lateinit var applicationBootManager: ApplicationBootManager
    private lateinit var bemUpdater: BEMUpdater
    var backgroundHeight = 0.dp

    override fun onCreate() {
        super.onCreate()
        buildDependencies()
        applicationContext.registerReceiver(shutdownReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
        applicationContext.registerReceiver(moodBroadcastReceiver, IntentFilter(MoodBroadcastReceiver.MOOD_UPDATE))
        SensorStepService.setupDailyStepReset(this)
        val appShutdownHandler = AppShutdownHandler(shutdownManager, sharedPreferences)
        // This may be run on app shutdown by Android... but updating or killing via the IDE never triggers this.
        Runtime.getRuntime().addShutdownHook(appShutdownHandler)
        GlobalScope.launch {
            applicationBootManager.onStartup()
        }
    }

    fun buildDependencies() {
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        //TODO: Should replace sharedPreferences with datastore (see https://developer.android.com/training/data-storage/shared-preferences)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val complicationRefreshService = ComplicationRefreshService(this, partnerComplicationState)
        cardLoader = CardLoader(applicationContext, spriteBitmapConverter)
        characterManager = CharacterManagerImpl(complicationRefreshService, database.characterDao(), cardLoader)
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val vitalService = VitalService(characterManager, complicationRefreshService)
        stepService = SensorStepService(sharedPreferences, sensorManager, sensorThreadHandler, Lists.newArrayList(vitalService))
        heartRateService = HeartRateService(sensorManager, sensorThreadHandler)
        moodBroadcastReceiver = MoodBroadcastReceiver(BEMMoodUpdater(heartRateService, stepService), characterManager)
        bemUpdater = BEMUpdater(applicationContext)
        saveService = SaveService(characterManager as CharacterManagerImpl, stepService, sharedPreferences)
        shutdownManager = ShutdownManager(saveService)
        firmwareManager.loadFirmware(applicationContext)
        backgroundManager = BackgroundManager(cardLoader, firmwareManager)
        val random = Random()
        val bemBattleLogic = BEMBattleLogic(random)
        battleService = BattleService(cardLoader, characterManager, firmwareManager, bemBattleLogic, saveService, vitalService, random)
        imageScaler = ImageScaler(applicationContext.resources.displayMetrics, applicationContext.resources.configuration.isScreenRound)
        backgroundHeight = imageScaler.scaledDpValueFromPixels(ImageScaler.VB_HEIGHT.toInt())
        bitmapScaler = BitmapScaler(imageScaler)
        scrollingNameFactory = ScrollingNameFactory(backgroundHeight, bitmapScaler)
        vitalBoxFactory = VitalBoxFactory(imageScaler, ImageScaler.VB_WIDTH.toInt(), ImageScaler.VB_HEIGHT.toInt())
        val opponentSplashFactory = OpponentSplashFactory(bitmapScaler)
        val opponentNameScreenFactory = OpponentNameScreenFactory(bitmapScaler, backgroundHeight, scrollingNameFactory)
        val readyScreenFactory = ReadyScreenFactory(bitmapScaler, backgroundHeight)
        val goScreenFactory = GoScreenFactory(bitmapScaler, backgroundHeight)
        val attackScreenFactory = AttackScreenFactory(bitmapScaler, backgroundHeight)
        val hpCompareFactory = HPCompareFactory(bitmapScaler, backgroundHeight)
        val endFightReactionFactory = EndFightReactionFactory(bitmapScaler, firmwareManager, characterManager, backgroundHeight)
        val endFightVitalsFactory = EndFightVitalsFactory(bitmapScaler, firmwareManager, backgroundManager, backgroundHeight)
        fightTargetFactory = FightTargetFactory(battleService, vitalBoxFactory, opponentSplashFactory, opponentNameScreenFactory, readyScreenFactory, goScreenFactory, attackScreenFactory, hpCompareFactory, endFightReactionFactory, endFightVitalsFactory)
        exerciseScreenFactory = ExerciseScreenFactory(saveService, vitalBoxFactory, bitmapScaler, backgroundHeight)
        partnerScreenComposable = PartnerScreenComposable(bitmapScaler, backgroundHeight, stepService)
        mainScreenComposable = MainScreenComposable(characterManager, saveService, firmwareManager, backgroundManager, imageScaler, bitmapScaler, partnerScreenComposable, vitalBoxFactory)
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardLoader)
        shutdownReceiver = ShutdownReceiver(shutdownManager)
        applicationBootManager = ApplicationBootManager(database, cardLoader, characterManager as CharacterManagerImpl, stepService, bemUpdater, saveService)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        // After we've setup the workManagerConfiguration, start the service
        val workProviderDependencies = WorkProviderDependencies(
            characterManager,
            stepService,
            saveService,
            sharedPreferences
        )
        return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(workProviderDependencies)).build()
    }
}