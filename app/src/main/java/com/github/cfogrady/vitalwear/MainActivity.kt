package com.github.cfogrady.vitalwear

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.data.FirmwareManager
import com.github.cfogrady.vitalwear.databinding.ActivityMainBinding


class MainActivity : Activity(), View.OnClickListener {
    private lateinit var firmwareManager: FirmwareManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firmwareManager = (application as VitalWearApp).firmwareManager
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loadButton.setOnClickListener(this)
        binding.characterButton.setOnClickListener {
            var intent = Intent(this, CharacterSelectActivity::class.java)
            startActivity(intent)
        }
    }

    fun loadImage() {
        //openDirectory(null)
        var intent = Intent(this, StorageSelectorActivity::class.java)
        startActivity(intent)
    }

    fun openDirectory(pickerInitialUri: Uri?) {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(FILE_LOAD_REQUEST_CODE == requestCode) {
            var uriData = data?.data;
            var filePath = uriData?.path
            var stream = openFileInput(filePath);
            var card = dimReader.readCard(stream, true);
            Log.i("MainActivity", "onActivityResult: " + card.header.text)
        }
    }

    companion object {
        const val FILE_LOAD_REQUEST_CODE = 1;
        val dimReader = DimReader()
    }

    override fun onClick(v: View?) {
        loadImage()
    }
}