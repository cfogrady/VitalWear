package com.github.cfogrady.vitalwear

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Configuration
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.activity.MainScreenComposable
import com.github.cfogrady.vitalwear.activity.PartnerScreenComposable
import com.github.cfogrady.vitalwear.battle.composable.*
import com.github.cfogrady.vitalwear.battle.data.BEMBattleLogic
import com.github.cfogrady.vitalwear.battle.BattleService
import com.github.cfogrady.vitalwear.card.AppCardLoader
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.character.mood.BEMMoodUpdater
import com.github.cfogrady.vitalwear.character.mood.MoodBroadcastReceiver
import com.github.cfogrady.vitalwear.character.transformation.TransformationScreenFactory
import com.github.cfogrady.vitalwear.common.card.CardCharacterImageService
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.common.card.SpriteFileIO
import com.github.cfogrady.vitalwear.data.AppDatabase
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.complications.PartnerComplicationState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ScrollingNameFactory
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.debug.ExceptionService
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.training.BackgroundTrainingScreenFactory
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import com.github.cfogrady.vitalwear.training.TrainingScreenFactory
import com.github.cfogrady.vitalwear.training.TrainingService
import com.github.cfogrady.vitalwear.vitals.VitalService
import com.github.cfogrady.vitalwear.workmanager.VitalWearWorkerFactory
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies
import com.google.common.collect.Lists
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Random

class VitalWearApp : Application(), Configuration.Provider {
    private val spriteBitmapConverter = SpriteBitmapConverter()
    val firmwareManager = FirmwareManager(spriteBitmapConverter)
    val partnerComplicationState = PartnerComplicationState()
    val exceptionService = ExceptionService()
    private val spriteFileIO = SpriteFileIO()
    private val characterSpritesIO = CharacterSpritesIO(spriteFileIO, spriteBitmapConverter)
    val cardSpriteIO = CardSpritesIO(spriteFileIO, spriteBitmapConverter)
    private val sensorThreadHandler = SensorThreadHandler()
    private lateinit var imageScaler : ImageScaler
    lateinit var notificationChannelManager: NotificationChannelManager
    lateinit var bitmapScaler: BitmapScaler
    lateinit var cardMetaEntityDao: CardMetaEntityDao
    lateinit var cardLoader: AppCardLoader
    lateinit var database : AppDatabase
    lateinit var characterManager: CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager
    lateinit var backgroundManager: BackgroundManager
    lateinit var battleService: BattleService
    lateinit var vitalBoxFactory: VitalBoxFactory
    lateinit var partnerScreenComposable: PartnerScreenComposable
    lateinit var mainScreenComposable: MainScreenComposable
    lateinit var fightTargetFactory: FightTargetFactory
    lateinit var trainingScreenFactory: TrainingScreenFactory
    lateinit var trainingService: TrainingService
    lateinit var backgroundTrainingScreenFactory: BackgroundTrainingScreenFactory
    lateinit var sharedPreferences: SharedPreferences
    lateinit var stepService: SensorStepService
    lateinit var shutdownReceiver: ShutdownReceiver
    lateinit var shutdownManager: ShutdownManager
    lateinit var heartRateService : HeartRateService
    lateinit var moodBroadcastReceiver: MoodBroadcastReceiver
    lateinit var scrollingNameFactory: ScrollingNameFactory
    lateinit var saveService: SaveService
    lateinit var transformationScreenFactory: TransformationScreenFactory
    lateinit var complicationRefreshService: ComplicationRefreshService
    lateinit var vitalService: VitalService
    private lateinit var applicationBootManager: ApplicationBootManager
    private lateinit var bemUpdater: BEMUpdater
    var backgroundHeight = 0.dp
    val gameState = MutableStateFlow(GameState.IDLE)

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        buildDependencies()
        applicationContext.registerReceiver(shutdownReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
        applicationContext.registerReceiver(moodBroadcastReceiver, IntentFilter(MoodBroadcastReceiver.MOOD_UPDATE))
        SensorStepService.setupDailyStepReset(this)
        val appShutdownHandler = AppShutdownHandler(shutdownManager, sharedPreferences)
        // This may be run on app shutdown by Android... but updating or killing via the IDE never triggers this.
        Runtime.getRuntime().addShutdownHook(appShutdownHandler)
        applicationBootManager.onStartup(this)
    }

    private fun buildDependencies() {
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        //TODO: Should replace sharedPreferences with datastore (see https://developer.android.com/training/data-storage/shared-preferences)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationChannelManager = NotificationChannelManager(notificationManager)
        complicationRefreshService = ComplicationRefreshService(this, partnerComplicationState)
        characterManager = CharacterManagerImpl(complicationRefreshService, database.characterDao(), characterSpritesIO, database.speciesEntityDao(), database.cardMetaEntityDao(), database.transformationEntityDao(), spriteBitmapConverter, database.characterSettingsDao(), database.characterAdventureDao(), database.transformationHistoryDao())
        cardMetaEntityDao = database.cardMetaEntityDao()
        val commonCardLoader = CardLoader(characterSpritesIO, cardSpriteIO, cardMetaEntityDao, database.speciesEntityDao(), database.transformationEntityDao(), database.adventureEntityDao(), database.attributeFusionEntityDao(), database.specificFusionEntityDao(), DimReader())
        cardLoader = AppCardLoader(commonCardLoader, database.cardSettingsDao())
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        vitalService = VitalService(characterManager, complicationRefreshService)
        stepService = SensorStepService(sharedPreferences, sensorManager, sensorThreadHandler, Lists.newArrayList(vitalService))
        heartRateService = HeartRateService(sensorManager, sensorThreadHandler)
        moodBroadcastReceiver = MoodBroadcastReceiver(BEMMoodUpdater(heartRateService, stepService), characterManager)
        bemUpdater = BEMUpdater(applicationContext)
        saveService = SaveService(characterManager as CharacterManagerImpl, stepService, sharedPreferences)
        trainingService = TrainingService(sensorManager, heartRateService, saveService)
        shutdownManager = ShutdownManager(saveService)
        firmwareManager.loadFirmware(applicationContext)
        backgroundManager = BackgroundManager(firmwareManager)
        val random = Random()
        val bemBattleLogic = BEMBattleLogic(random)
        battleService = BattleService(cardSpriteIO, database.speciesEntityDao(), characterSpritesIO, characterManager, firmwareManager, bemBattleLogic, saveService, vitalService, random, database.cardSettingsDao())
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
        trainingScreenFactory = TrainingScreenFactory(vitalBoxFactory, bitmapScaler, backgroundHeight, trainingService, gameState)
        backgroundTrainingScreenFactory = BackgroundTrainingScreenFactory(trainingScreenFactory, trainingService)

        transformationScreenFactory = TransformationScreenFactory(characterManager, backgroundHeight, firmwareManager, bitmapScaler, vitalBoxFactory, bemUpdater)
        partnerScreenComposable = PartnerScreenComposable(bitmapScaler, backgroundHeight, stepService)
        mainScreenComposable = MainScreenComposable(gameState, characterManager, saveService, firmwareManager, backgroundManager, backgroundTrainingScreenFactory, imageScaler, bitmapScaler, partnerScreenComposable, vitalBoxFactory)
        val cardCharacterImageService = CardCharacterImageService(database.speciesEntityDao(), characterSpritesIO)
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardCharacterImageService)
        shutdownReceiver = ShutdownReceiver(shutdownManager)
        applicationBootManager = ApplicationBootManager(characterManager as CharacterManagerImpl, stepService, bemUpdater, saveService, notificationChannelManager, complicationRefreshService)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        // After we've setup the workManagerConfiguration, start the service
        val workProviderDependencies = WorkProviderDependencies(
            characterManager,
            notificationChannelManager,
            bemUpdater,
            stepService,
            saveService,
            sharedPreferences
        )
        return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(workProviderDependencies)).build()
    }
}