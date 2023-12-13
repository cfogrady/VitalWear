package com.github.cfogrady.vitalwear.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import java.io.File

const val PROMPT_TEXT = "PROMPT_TEXT"
const val FILE_FILTER = "FILE_FILTER"
const val SELECTED_FILE = "SELECTED_FILE"

class FileExploreActivity : ComponentActivity() {
    companion object {
        val TAG = "FileExploreActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val promptText = intent?.getStringExtra(PROMPT_TEXT)?: "Select File"
        val fileFilter = intent?.getStringExtra(FILE_FILTER)?: ".*"
        setContent {
            FileNavigator(promptText, fileFilter)
        }
    }

    @Composable
    fun FileNavigator(promptText: String, fileFilter: String) {
        var currentDirectory by remember { mutableStateOf(Environment.getExternalStorageDirectory()) }
        ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            item { 
                Text(text = promptText, modifier = Modifier.padding(5.dp), fontWeight = FontWeight.Bold)
            }
            item {
                Text(text = currentDirectory.path, modifier = Modifier.padding(5.dp), fontWeight = FontWeight.Bold)
            }
            if (currentDirectory.parentFile != null && currentDirectory.parentFile.list() != null) {
                item {
                    Text(text = "..", modifier = Modifier.padding(5.dp).clickable {
                        currentDirectory = currentDirectory.parentFile
                    })
                }
            }
            items(items = filterFiles(currentDirectory.listFiles()?:Array<File>(0, {_ -> File("")}), Regex(fileFilter))) { file ->
                Text(text = file.name, modifier = Modifier.padding(5.dp).clickable {
                    if (file.isDirectory) {
                        currentDirectory = file
                    } else {
                        val intent = Intent()
                        intent.putExtra(SELECTED_FILE, file.path)
                        setResult(0, intent)
                        finish()
                    }
                })
            }
        }
    }

    private fun filterFiles(items: Array<File>, filter: Regex): List<File> {
        return items.filter { item ->
            item.isDirectory || item.name.matches(filter)
        }
    }
}