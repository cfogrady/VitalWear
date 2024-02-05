package com.github.cfogrady.vitalwear.card

import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter

@RunWith(Parameterized::class)
class DimToBemStatConversionTest() {
    class Stats(val name: String, val dp: Int, val hp: Int, val ap: Int, val phase: Int, val nextPhase: Int?, val fromPhase: Int?, val fromFusionPhase: Int?,  val expectedBp: Int, val expectedHp: Int, val expectedAp: Int, val expectedPhase: Int) {}

    class StatConversionDaoMock(private val nextPhase: Int?, private val fromPhase: Int?, private val fromFusionPhase: Int?): StatConversionDao {

        override fun getTransformationsToCharacterPhase(
            cardName: String,
            toCharacterId: Int
        ): Int? {
            return fromPhase
        }

        override fun getNextTransformationPhaseFromCharacter(
            cardName: String,
            fromCharacterId: Int
        ): Int? {
            return nextPhase
        }

        override fun getFusionToCharacterPhase(cardName: String, speciesId: Int): Int? {
            return fromFusionPhase
        }

        override fun getSameSpeciesFromBEM(spriteDir: String): List<SpeciesEntity> {
            return emptyList()
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(
            name = "Stats"
        )
        fun getStats(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf(Stats( "VII From Fusion", 60, 14, 7, 5, null, null, 5, 6455, 5769, 2069, 6)),
                arrayOf(Stats( "VII Strong From Fusion", 65, 12, 12, 5, null, null, 5, 6737, 5603, 2146, 6)),
                arrayOf(Stats( "VII From Transformation", 60, 14, 7, 5, null, 5, null, 6455, 5769, 2069, 6)),
                arrayOf(Stats( "VII From Fusion or Transform from V", 60, 14, 7, 5, null, 4, 5, 6455, 5769, 2069, 6)),
                arrayOf(Stats( "Looping Mode Change (Aderek Custom)", 70, 12, 8, 5, 5, 5, null, 6651, 5254, 1962, 5)),
                arrayOf(Stats( "I (no stats)", 0, 0, 0, 0, null, null, null, 0, 0, 0, 0)),
                arrayOf(Stats( "II (no stats)", 0, 0, 0, 1, null, 0, null, 0, 0, 0, 1)),
                arrayOf(Stats( "III (average)", 10, 3, 2, 2, null, 1, null, 4657, 3005, 1022, 2)),
                arrayOf(Stats( "IV Strong", 25, 5, 3, 3, null, 2, null, 5641, 3496, 1289, 3)),
                arrayOf(Stats( "IV Middle", 20, 4, 3, 3, null, 2, null, 5171, 3282, 1289, 3)),
                arrayOf(Stats( "IV Weak", 15, 4, 2, 3, null, 2, null, 4701, 3282, 1156, 3)),
                arrayOf(Stats( "V Weak", 25, 9, 4, 4, null, 3, null, 5048, 4408, 1475, 4)),
                arrayOf(Stats( "V Middle", 30, 9, 4, 4, null, 3, null, 5459, 4408, 1475, 4)),
                arrayOf(Stats( "V Middle+", 35, 10, 3, 4, null, 3, null, 5869, 4530, 1416, 4)),
                arrayOf(Stats( "V Strong", 40, 8, 5, 4, null, 3, null, 6280, 4285, 1535, 4)),
                arrayOf(Stats( "VI Weak", 45, 16, 6, 5, null, 4, null, 5293, 5540, 1906, 5)),
                arrayOf(Stats( "VI Middle", 50, 14, 7, 5, null, 4, null, 5564, 5397, 1934, 5)),
                arrayOf(Stats( "VI Middle+", 55, 14, 8, 5, null, 4, null, 5836, 5397, 1962, 5)),
                arrayOf(Stats( "VI Strong", 60, 16, 7, 5, null, 4, null, 6108, 5540, 1934, 5)),
                arrayOf(Stats( "VI Strong+", 70, 12, 8, 5, null, 4, null, 6651, 5254, 1962, 5)),
                arrayOf(Stats( "HP Stat Phase Upgrade", 30, 14, 4, 4, null, 3, null, 5459, 5397, 1475, 4)),
                arrayOf(Stats( "All Stat Upgrade", 70, 14, 9, 4, null, 3, null, 6651, 5397, 1990, 4)),
            )
        }
    }


    @Parameter
    lateinit var stats: Stats

    @Test
    fun testStatConversion() = runTest{
        val dimToBemStatConversion = DimToBemStatConversion(StatConversionDaoMock(stats.nextPhase, stats.fromPhase, stats.fromFusionPhase))
        val speciesEntity = SpeciesEntity("", 0, stats.phase, 0, 0, 0, 0, 0, stats.dp, stats.hp, stats.ap, 0, 0, 0, "", false)
        val conversion: SpeciesEntity = dimToBemStatConversion.convertSpeciesEntity(speciesEntity)
        Assert.assertEquals("${stats.name} BP scaling mismatch", stats.expectedBp, conversion.bp)
        Assert.assertEquals("${stats.name} HP scaling mismatch", stats.expectedHp, conversion.hp)
        Assert.assertEquals("${stats.name} AP scaling mismatch", stats.expectedAp, conversion.ap)
        Assert.assertEquals("${stats.name} Phase change mismatch", stats.expectedPhase, conversion.phase)
    }
}