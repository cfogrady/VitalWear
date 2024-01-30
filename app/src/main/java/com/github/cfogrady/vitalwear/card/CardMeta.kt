package com.github.cfogrady.vitalwear.card

import android.os.Parcel
import android.os.Parcelable
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity

class CardMeta(val cardName: String,
               val cardId: Int,
               val cardChecksum: Int,
               val cardType: CardType,
               val franchise: Int,
               val maxAdventureCompletion: Int?
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<CardMeta> {
        const val DIM_FRANCHISE = 0

        override fun createFromParcel(parcel: Parcel): CardMeta {
            return CardMeta(parcel)
        }

        override fun newArray(size: Int): Array<CardMeta?> {
            return arrayOfNulls(size)
        }

        fun fromCardMetaEntity(entity: CardMetaEntity): CardMeta {
            return CardMeta(entity.cardName, entity.cardId, entity.cardChecksum, entity.cardType, entity.franchise, entity.maxAdventureCompletion)
        }
    }

    fun toCardMetaEntity(): CardMetaEntity {
        return CardMetaEntity(cardName, cardId, cardChecksum, cardType, franchise, maxAdventureCompletion)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        CardType.valueOf(parcel.readString()!!),
        parcel.readInt(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cardName)
        parcel.writeInt(cardId)
        parcel.writeInt(cardChecksum)
        parcel.writeString(cardType.name)
        parcel.writeInt(franchise)
        parcel.writeValue(maxAdventureCompletion)
    }

    override fun describeContents(): Int {
        return 0
    }

}