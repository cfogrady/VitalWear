package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData

class Firmware constructor(val loadingIcon : Bitmap,
                           val insertCardIcon: Bitmap,
                           val defaultBackground: Bitmap,
                           val characterSelectorIcon: Bitmap,
                           val stepsIcon: Bitmap,
                           val vitalsIcon: Bitmap,
                           val battleIcon: Bitmap,
                           val attackSprites: List<Bitmap>,
                           val largeAttackSprites: List<Bitmap>,
                           val battleBackground: Bitmap,
) {

}