package com.github.cfogrady.vitalwear.tiles

import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.wear.tiles.GlanceTileService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.wear.tiles.*
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.ImageScaler
import com.github.cfogrady.vitalwear.activity.PartnerScreenComposable
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.data.FirmwareManager

class PetTileService : GlanceTileService(), LifecycleOwner {
    private val RESOURCES_VERSION = "1"
    private val dispatcher = ServiceLifecycleDispatcher(this)

    lateinit var imageScaler: ImageScaler
    lateinit var bitmapScaler: BitmapScaler
    lateinit var partnerScreenComposable: PartnerScreenComposable
    lateinit var firmwareManager: FirmwareManager
    lateinit var characterManager: CharacterManager
    override fun onCreate() {
        super.onCreate()
        dispatcher.onServicePreSuperOnCreate()
        imageScaler = (application as VitalWearApp).imageScaler
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        partnerScreenComposable = (application as VitalWearApp).partnerScreenComposable
        firmwareManager = (application as VitalWearApp).firmwareManager
        characterManager = (application as VitalWearApp).characterManager
    }

    @Composable
    override fun Content() {
//        val intent = Intent(applicationContext, CharacterSelectActivity::class.java)
//        val characterSelector = {
//            startActivity(intent)
//        }
        var myState by remember { mutableStateOf(0)}
        val padding = imageScaler.getPadding()
        val firmware = firmwareManager.getFirmware()
        firmware.observe(this) { firmware ->
            //just trigger a state refresh
            myState++
        }
        val character = characterManager.getActiveCharacter()
        character.observe(this) { character ->
            myState++
        }
        if(character.value != null && firmware.value != null) {
            Text(text = "Loaded")
            Box(modifier = GlanceModifier
                .padding(padding)
                .fillMaxSize()) {
                //bitmapScaler.ScaledBitmapGlance(bitmap = firmware.value!!.defaultBackground, contentDescription = "Background")
                androidx.glance.Image(provider = ImageProvider(firmware.value!!.defaultBackground), contentDescription = "Background")
                //partnerScreenComposable.GlancePartnerScreen(character = character.value!!, firmware = firmware.value!!)
            }
        } else {
            Text(text = "Not yet loaded")
        }
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    @CallSuper
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    // this method is added only to annotate it with @CallSuper.
    // In usual Service, super.onStartCommand is no-op, but in LifecycleService
    // it results in dispatcher.onServicePreSuperOnStart() call, because
    // super.onStartCommand calls onStart().
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle
}