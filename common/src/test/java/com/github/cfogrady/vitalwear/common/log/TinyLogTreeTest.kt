package com.github.cfogrady.vitalwear.common.log

import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TinyLogTreeTest {

    class TestCase(val name: String, val files: Array<File>, val expectedFile: File?) {}

    companion object {
        @JvmStatic
        @Parameterized.Parameters(
            name = "TestCase"
        )
        fun getFiles(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf(
                    TestCase(
                        "Most Recent Date",
                        arrayOf(File("log_2024-03-19_0.txt"), File("log_2024-03-20_0.txt"), File("log_2024-03-21_0.txt"),),
                        File("log_2024-03-21_0.txt")
                    )
                ),
                arrayOf(
                    TestCase(
                        "Reverse Order",
                        arrayOf(File("log_2024-03-21_0.txt"), File("log_2024-03-20_0.txt"), File("log_2024-03-19_0.txt"),),
                        File("log_2024-03-21_0.txt")
                    )
                ),
                arrayOf(
                    TestCase(
                        "Most Recent Date Has Count",
                        arrayOf(File("log_2024-03-19_0.txt"), File("log_2024-03-20_0.txt"), File("log_2024-03-21_0.txt"), File("log_2024-03-21_1.txt"), File("log_2024-03-21_2.txt"),),
                        File("log_2024-03-21_2.txt")
                    )
                ),
                arrayOf(
                    TestCase(
                        "Less Significant Date Has Higher Count",
                        arrayOf(File("log_2024-03-19_0.txt"), File("log_2024-03-20_0.txt"), File("log_2024-03-20_1.txt"), File("log_2024-03-21_0.txt"),),
                        File("log_2024-03-21_0.txt")
                    )
                ),
                arrayOf(
                    TestCase(
                        "Empty List",
                        arrayOf(),
                        null
                    )
                ),
                arrayOf(
                    TestCase(
                        "Non Log Files",
                        arrayOf(File("log_2024-03-19_0.bad"), File("log_2024-03-19_a.txt"), File("test.txt"), File("2024-03-19_0.txt"), File("log_YYYY-03-19_0.txt")),
                        null
                    )
                ),
            )
        }
    }

    @Parameterized.Parameter
    lateinit var testCase: TestCase

    @Test
    fun testGetMostRecentLogFileFromFiles() = runTest{
        val actual = TinyLogTree.getMostRecentLogFileFromFiles(testCase.files)
        Assert.assertEquals("${testCase.name} did not retrieve expected file", testCase.expectedFile?.name, actual?.name)
    }
}