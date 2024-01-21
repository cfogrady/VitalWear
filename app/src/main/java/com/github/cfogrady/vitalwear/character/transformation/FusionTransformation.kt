package com.github.cfogrady.vitalwear.character.transformation

import android.graphics.Bitmap

class FusionTransformation(override val slotId: Int, val supportIdle: Bitmap, val supportIdle2: Bitmap, val supportAttack: Bitmap) : ExpectedTransformation(slotId, true) {
}