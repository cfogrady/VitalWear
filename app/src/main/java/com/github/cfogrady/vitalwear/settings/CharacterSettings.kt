package com.github.cfogrady.vitalwear.settings

import android.os.Parcel
import android.os.Parcelable

class CharacterSettings(val characterId: Int,
                        val trainInBackground: Boolean,
                        val allowedBattles: AllowedBattles,
                        val assumedFranchise: Int?,
): Parcelable {

    companion object CREATOR : Parcelable.Creator<CharacterSettings> {
        override fun createFromParcel(parcel: Parcel): CharacterSettings {
            return CharacterSettings(parcel)
        }

        override fun newArray(size: Int): Array<CharacterSettings?> {
            return arrayOfNulls(size)
        }

        fun fromCharacterSettingsEntity(entity: CharacterSettingsEntity): CharacterSettings {
            return CharacterSettings(entity.characterId, entity.trainInBackground, entity.allowedBattles, entity.assumedFranchise)
        }

        fun defaultSettings(): CharacterSettings {
            return CharacterSettings(0, true, AllowedBattles.CARD_ONLY, null)
        }
    }

    enum class AllowedBattles(val descr: String, val showOnDIM: Boolean, val interactsWithDIMs: Boolean) {
        CARD_ONLY("Card Only", true, false),
        ALL_FRANCHISE("Any Card In Franchise", true, false),
        ALL_FRANCHISE_AND_DIM("Any Card In Franchise And DIMs", false, true),
        ALL("Any Card", false, true);
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        AllowedBattles.valueOf(parcel.readString()!!),
        parcel.readValue(null) as Int?
    ) {
    }

    fun toCharacterSettingsEntity(): CharacterSettingsEntity {
        return CharacterSettingsEntity(characterId, trainInBackground, allowedBattles, assumedFranchise)
    }

    fun copy(characterId: Int = this.characterId,
             trainInBackground: Boolean = this.trainInBackground,
             allowedBattles: AllowedBattles = this.allowedBattles,
             assumedFranchise: Int? = this.assumedFranchise): CharacterSettings {
        return CharacterSettings(characterId, trainInBackground, allowedBattles, assumedFranchise)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(characterId)
        parcel.writeByte(if (trainInBackground) 1 else 0)
        parcel.writeString(allowedBattles.name)
        parcel.writeValue(assumedFranchise)
    }

    override fun describeContents(): Int {
        return 0
    }
}