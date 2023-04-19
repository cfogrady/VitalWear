package com.github.cfogrady.vitalwear.data

import android.graphics.Bitmap

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
                           val readyIcon: Bitmap,
                           val goIcon: Bitmap,
                           val partnerHpIcons: List<Bitmap>,
                           val opponentHpIcons: List<Bitmap>,
                           val hitIcons: List<Bitmap>,
                           val happyEmote: List<Bitmap>,
                           val loseEmote: List<Bitmap>,
                           val sweatEmote: Bitmap,
                           val injuredEmote: List<Bitmap>,
) {

}