package com.github.cfogrady.vitalwear

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Configuration
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.character.PartnerScreenComposable
import com.github.cfogrady.vitalwear.adventure.AdventureService
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.battle.composable.*
import com.github.cfogrady.vitalwear.battle.data.BEMBattleLogic
import com.github.cfogrady.vitalwear.battle.BattleService
import com.github.cfogrady.vitalwear.card.AppCardLoader
import com.github.cfogrady.vitalwear.card.CardReceiver
import com.github.cfogrady.vitalwear.card.DimToBemStatConversion
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.character.data.PreviewCharacterManager
import com.github.cfogrady.vitalwear.character.mood.MoodBroadcastReceiver
import com.github.cfogrady.vitalwear.character.mood.MoodService
import com.github.cfogrady.vitalwear.character.transformation.TransformationScreenFactory
import com.github.cfogrady.vitalwear.common.card.CardCharacterImageService
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.SpriteBitmapConverter
import com.github.cfogrady.vitalwear.common.card.SpriteFileIO
import com.github.cfogrady.vitalwear.common.data.migrations.CreateAndPopulateMaxAdventureCompletionCardMeta
import com.github.cfogrady.vitalwear.data.AppDatabase
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.complications.PartnerComplicationState
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ScrollingNameFactory
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.data.GameState
import com.github.cfogrady.vitalwear.log.LogSettings
import com.github.cfogrady.vitalwear.common.log.TinyLogTree
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.firmware.FirmwareReceiver
import com.github.cfogrady.vitalwear.firmware.PostFirmwareLoader
import com.github.cfogrady.vitalwear.heartrate.HeartRateService
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager
import com.github.cfogrady.vitalwear.settings.SettingsComposableFactory
import com.github.cfogrady.vitalwear.steps.StepIOService
import com.github.cfogrady.vitalwear.steps.StepSensorService
import com.github.cfogrady.vitalwear.steps.StepState
import com.github.cfogrady.vitalwear.training.BackgroundTrainingScreenFactory
import com.github.cfogrady.vitalwear.util.SensorThreadHandler
import com.github.cfogrady.vitalwear.training.TrainingScreenFactory
import com.github.cfogrady.vitalwear.training.TrainingService
import com.github.cfogrady.vitalwear.vitals.VitalService
import com.github.cfogrady.vitalwear.workmanager.VitalWearWorkerFactory
import com.github.cfogrady.vitalwear.workmanager.WorkProviderDependencies
import com.google.common.collect.Lists
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.Random

class VitalWearApp : Application(), Configuration.Provider {

    private val spriteBitmapConverter = SpriteBitmapConverter()
    private val spriteFileIO = SpriteFileIO()
    val cardSpriteIO = CardSpritesIO(spriteFileIO, spriteBitmapConverter)
    lateinit var backgroundManager: BackgroundManager
    lateinit var firmwareManager: FirmwareManager
    val partnerComplicationState = PartnerComplicationState()
    val characterSpritesIO = CharacterSpritesIO(spriteFileIO, spriteBitmapConverter)
    lateinit var saveDataRepository: SaveDataRepository

    private val sensorThreadHandler = SensorThreadHandler()
    private lateinit var imageScaler : ImageScaler
    lateinit var notificationChannelManager: NotificationChannelManager
    lateinit var bitmapScaler: BitmapScaler
    lateinit var cardMetaEntityDao: CardMetaEntityDao
    lateinit var cardLoader: AppCardLoader
    lateinit var database : AppDatabase
    lateinit var characterManager: CharacterManager
    lateinit var previewCharacterManager: PreviewCharacterManager
    lateinit var battleService: BattleService
    lateinit var vitalBoxFactory: VitalBoxFactory
    lateinit var partnerScreenComposable: PartnerScreenComposable
    lateinit var fightTargetFactory: FightTargetFactory
    lateinit var trainingScreenFactory: TrainingScreenFactory
    lateinit var trainingService: TrainingService
    lateinit var backgroundTrainingScreenFactory: BackgroundTrainingScreenFactory
    lateinit var sharedPreferences: SharedPreferences
    lateinit var logSettings: LogSettings
    lateinit var stepService: StepSensorService
    lateinit var shutdownReceiver: ShutdownReceiver
    lateinit var shutdownManager: ShutdownManager
    lateinit var heartRateService : HeartRateService
    lateinit var moodBroadcastReceiver: MoodBroadcastReceiver
    lateinit var scrollingNameFactory: ScrollingNameFactory
    lateinit var saveService: SaveService
    lateinit var transformationScreenFactory: TransformationScreenFactory
    lateinit var complicationRefreshService: ComplicationRefreshService
    lateinit var vitalService: VitalService
    lateinit var adventureService: AdventureService
    lateinit var cardReceiver: CardReceiver
    lateinit var firmwareReceiver: FirmwareReceiver
    lateinit var moodService: MoodService
    lateinit var settingsComposableFactory: SettingsComposableFactory
    private lateinit var applicationBootManager: ApplicationBootManager
    private lateinit var vbUpdater: VBUpdater
    var backgroundHeight = 0.dp
    val gameState = MutableStateFlow(GameState.IDLE)

    override fun onCreate() {
        super.onCreate()

        //TODO: Migrate sharedPreferences over to saveDataRepository
        saveDataRepository = SaveDataRepository(this.saveDataStore)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val tinyLogTree = TinyLogTree(this)
        logSettings = LogSettings(sharedPreferences, tinyLogTree)
        logSettings.setupLogging()
        Timber.i("Running VitalWear App. Version: ${BuildConfig.VERSION_NAME}  ${BuildConfig.VERSION_CODE}")
        val crashHandler = CrashHandler(this, logSettings, Thread.getDefaultUncaughtExceptionHandler())
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
        buildDependencies()
        applicationContext.registerReceiver(shutdownReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
        // NOT_EXPORTED prevents debug and release app from sending each other broadcasts
        ContextCompat.registerReceiver(applicationContext, moodBroadcastReceiver, IntentFilter(MoodBroadcastReceiver.MOOD_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
        val powerBroadcastReceiver = PowerBroadcastReceiver()
        val powerChangeIntentFilter = IntentFilter()
        powerChangeIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        powerChangeIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        ContextCompat.registerReceiver(applicationContext, powerBroadcastReceiver, powerChangeIntentFilter, ContextCompat.RECEIVER_EXPORTED)

        val appShutdownHandler = AppShutdownHandler(shutdownManager)
        // This may be run on app shutdown by Android... but updating or killing via the IDE never triggers this.
        Runtime.getRuntime().addShutdownHook(appShutdownHandler)
        applicationBootManager.onStartup(this)
    }

    private fun buildDependencies() {
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear")
            .addMigrations(CreateAndPopulateMaxAdventureCompletionCardMeta()).allowMainThreadQueries().build()
        backgroundManager = BackgroundManager(cardSpriteIO, sharedPreferences)
        val postFirmwareLoader = PostFirmwareLoader(backgroundManager)
        firmwareManager = FirmwareManager(spriteBitmapConverter, postFirmwareLoader)
        val dimToBemStatConversion = DimToBemStatConversion(database.statConversionDao())
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationChannelManager = NotificationChannelManager(notificationManager)
        complicationRefreshService = ComplicationRefreshService(this, partnerComplicationState)
        characterManager = CharacterManagerImpl(complicationRefreshService, database.characterDao(), characterSpritesIO, database.speciesEntityDao(), database.cardMetaEntityDao(), database.transformationEntityDao(), database.characterSettingsDao(), database.characterAdventureDao(), database.transformationHistoryDao(), database.attributeFusionEntityDao(), database.specificFusionEntityDao(), dimToBemStatConversion)
        cardMetaEntityDao = database.cardMetaEntityDao()
        val cardSpriteLoader = CardSpriteLoader()
        val commonCardLoader = CardLoader(characterSpritesIO, cardSpriteLoader, cardSpriteIO, cardMetaEntityDao, database.speciesEntityDao(), database.transformationEntityDao(), database.adventureEntityDao(), database.attributeFusionEntityDao(), database.specificFusionEntityDao(), DimReader())
        cardLoader = AppCardLoader(commonCardLoader, database.cardSettingsDao())
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        vitalService = VitalService(characterManager, complicationRefreshService)
        heartRateService = HeartRateService(sensorManager, sensorThreadHandler)
        vbUpdater = VBUpdater(applicationContext)
        val stepState = StepState()
        val stepIOService = StepIOService(sharedPreferences, stepState)
        saveService = SaveService(characterManager as CharacterManagerImpl, stepIOService, sharedPreferences)
        stepService = StepSensorService(sensorManager, sensorThreadHandler, Lists.newArrayList(vitalService), stepState, stepIOService, saveService)
        moodService = MoodService(heartRateService, sensorManager, vbUpdater, characterManager, vitalService, saveService)
        moodBroadcastReceiver = MoodBroadcastReceiver(moodService)

        trainingService = TrainingService(sensorManager, heartRateService, saveService)
        shutdownManager = ShutdownManager(saveService)
        val random = Random()
        val bemBattleLogic = BEMBattleLogic(random)
        battleService = BattleService(cardSpriteIO, database.speciesEntityDao(), characterSpritesIO, characterManager, firmwareManager, bemBattleLogic, saveService, vitalService, backgroundManager, random, database.cardSettingsDao(), database.cardMetaEntityDao(), dimToBemStatConversion)
        imageScaler = ImageScaler(applicationContext.resources.displayMetrics, applicationContext.resources.configuration.isScreenRound)
        backgroundHeight = imageScaler.calculateBackgroundHeight()
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

        transformationScreenFactory = TransformationScreenFactory(characterManager, backgroundHeight, firmwareManager, bitmapScaler, vitalBoxFactory, vbUpdater)
        partnerScreenComposable = PartnerScreenComposable(bitmapScaler, backgroundHeight, stepService, heartRateService)
        adventureService = AdventureService(gameState, database.cardMetaEntityDao(), characterManager, database.adventureEntityDao(), cardSpriteIO, notificationChannelManager, database.characterAdventureDao(), stepService, sensorManager)
        val cardCharacterImageService = CardCharacterImageService(database.speciesEntityDao(), characterSpritesIO)
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardCharacterImageService)
        shutdownReceiver = ShutdownReceiver(shutdownManager)
        applicationBootManager = ApplicationBootManager(characterManager as CharacterManagerImpl, firmwareManager, stepService, vbUpdater, moodService, notificationChannelManager, complicationRefreshService)
        cardReceiver = CardReceiver(cardLoader)
        firmwareReceiver = FirmwareReceiver(firmwareManager, notificationChannelManager)
        settingsComposableFactory = SettingsComposableFactory(backgroundManager, vitalBoxFactory, bitmapScaler, logSettings, saveService)
    }

    override val workManagerConfiguration: Configuration
        get() {
            val workProviderDependencies = WorkProviderDependencies(
                characterManager,
                notificationChannelManager,
                vbUpdater,
                saveService,
                sharedPreferences,
            )
            return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(workProviderDependencies)).build()
        }
}