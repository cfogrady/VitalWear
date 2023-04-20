package com.github.cfogrady.vitalwear.firmware

import android.graphics.Bitmap

//TODO: Break into smaller firmware components (training, battle, emotes, menus, etc.)
class Firmware constructor(val loadingIcon : Bitmap,
                           val insertCardIcon: Bitmap,
                           val defaultBackground: Bitmap,
                           val characterSelectorIcon: Bitmap,
                           val trainingIcon: Bitmap,
                           val stepsIcon: Bitmap,
                           val vitalsIcon: Bitmap,
                           val battleScreen: Bitmap,
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
                           val squatText: Bitmap,
                           val squatIcon: Bitmap,
                           val crunchText: Bitmap,
                           val crunchIcon: Bitmap,
                           val punchText: Bitmap,
                           val punchIcon: Bitmap,
                           val dashText: Bitmap,
                           val dashIcon: Bitmap,
                           val trainingState: List<Bitmap>,
                           val mission: Bitmap,
                           val clear: Bitmap,
) {

}