package edu.shape.postdraftsforsocialmedia

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        val REQUEST_IMAGE_CATPURE = 1
        val REQUEST_SELECT_IMAGE = 2
    }
    fun onCameraClicked(v : View){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CATPURE)
    }
    fun onPhotoLibraryClicked(v : View) {
        val pickerIntent = Intent(MediaStore.ACTION_PICK_IMAGES)
        if (pickerIntent.resolveActivity(getPackageManager()) != null ){
            startActivityForResult(pickerIntent, REQUEST_SELECT_IMAGE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        if(requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data as Uri
            imageView.setImageURI(selectedImageUri)
        } else if (requestCode == REQUEST_IMAGE_CATPURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    var mIsRecording = false
    lateinit var mRecorder : MediaRecorder

    @SuppressLint("NewApi")
    fun onRecordClicked(v : View){
        if(!mIsRecording) {

            if(checkSelfPermission( android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 3)
                return
            }
            mRecorder = MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            val fileName = filesDir.path + "/test.m4a"
            val file = File(fileName)
            mRecorder.setOutputFile(file)
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)


            try {
                mRecorder.prepare()
            } catch ( e : IOException) {
                Toast.makeText(this, "${e.localizedMessage}", Toast.LENGTH_LONG).show()
                return
            }
            Toast.makeText(this, "recording started ${fileName}", Toast.LENGTH_LONG).show()
            val myButton = findViewById<Button>(R.id.button3)
            myButton.setText("Stop")
            mRecorder.start()
            mIsRecording = true
        } else {
            Toast.makeText(this, "recording stopped", Toast.LENGTH_LONG).show()
            val myButton = findViewById<Button>(R.id.button3)
            myButton.setText("Record")
            mRecorder.stop()
            mRecorder.release()
            mIsRecording = false
        }
    }

    lateinit var mPlayer : MediaPlayer
    var mIsPlaying = false
    @RequiresApi(Build.VERSION_CODES.R)
    fun onPlayClicked(v : View) {
        if (!mIsPlaying){
            mPlayer = MediaPlayer()
            try {
                Toast.makeText(this, "play recording", Toast.LENGTH_LONG).show()
                val fileName = filesDir.path + "/test.m4a"
                mPlayer.setDataSource(fileName)
                mPlayer.prepare()
                mPlayer.start()
                mIsPlaying = true
            } catch (e : IOException) {

            }
        } else {
            mPlayer.release()
            mIsPlaying = false
        }
    }
}