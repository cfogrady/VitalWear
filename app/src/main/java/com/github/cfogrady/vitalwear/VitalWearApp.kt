package com.github.cfogrady.vitalwear

import android.app.Application
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
import com.github.cfogrady.vitalwear.training.ExerciseScreenFactory
import java.util.Random

class VitalWearApp : Application(), Configuration.Provider {
    val spriteBitmapConverter = SpriteBitmapConverter()
    val firmwareManager = FirmwareManager(spriteBitmapConverter)
    val partnerComplicationState = PartnerComplicationState()
    lateinit var imageScaler : ImageScaler
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

    override fun onCreate() {
        super.onCreate()
        //TODO: Remove allowMainThread before release
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "VitalWear").allowMainThreadQueries().build()
        firmwareManager.loadFirmware(applicationContext)
        cardLoader = CardLoader(applicationContext, spriteBitmapConverter)
        characterManager = CharacterManager()
        characterManager.init(database.characterDao(), cardLoader, BEMUpdater(applicationContext))
        backgroundManager = BackgroundManager(cardLoader, firmwareManager)
        val random = Random()
        val bemBattleLogic = BEMBattleLogic(random)
        battleService = BattleService(cardLoader, characterManager, firmwareManager, bemBattleLogic, random)
        imageScaler = ImageScaler(applicationContext.resources.displayMetrics, applicationContext.resources.configuration.isScreenRound)
        val backgroundHeight = imageScaler.convertPixelsToDp(ImageScaler.VB_HEIGHT.toInt())
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
        partnerScreenComposable = PartnerScreenComposable(bitmapScaler, backgroundHeight)
        mainScreenComposable = MainScreenComposable(characterManager, firmwareManager, backgroundManager, imageScaler, bitmapScaler, partnerScreenComposable, vitalBoxFactory)
        previewCharacterManager = PreviewCharacterManager(database.characterDao(), cardLoader)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        //characterManager = CharacterManager()
        return Configuration.Builder().setWorkerFactory(VitalWearWorkerFactory(characterManager)).build()
    }
}