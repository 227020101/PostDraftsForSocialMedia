package edu.shape.postdraftsforsocialmedia

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var image_holder: ImageView
    lateinit var editText: EditText
    lateinit var mOutput: TextView
    lateinit var editTextContent: EditText
    lateinit var editTextPostName: EditText

    // member variables that hold location info
    protected var mLastLocation: Location? = null
    protected var mLocationRequest: LocationRequest? = null
    protected var mGeocoder: Geocoder? = null
    protected var mLocationProvider: FusedLocationProviderClient? = null
    private var file: File? = null
    private var imageFile: File? = null
    private var outputStream: FileOutputStream? = null
    private var inputStream: FileInputStream? = null
    private var outputStreamImage: FileOutputStream? = null
    private var inputStreamImage: FileInputStream? = null

    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_holder = findViewById(R.id.imageView)
        image_holder.setImageResource(R.drawable.ic_launcher_foreground)
        editText = findViewById(R.id.editTextHashtag)
        editTextContent = findViewById(R.id.editTextContent)
        editTextPostName = findViewById(R.id.editTextPostName)
        mOutput = findViewById(R.id.textViewLocation)
        file = File(this.filesDir, FILE_NAME)
        imageFile = File(this.filesDir, IMAGE_FILE_NAME)
        sharedPreferences = getSharedPreferences("MySharedPreMain", MODE_PRIVATE)

        if (sharedPreferences!!.contains(TAG_KEY)) {
            editText!!.setText(sharedPreferences!!.getString(TAG_KEY, ""))
        }
        if (sharedPreferences!!.contains(LOCATION_KEY)) {
            mOutput!!.text= sharedPreferences!!.getString(LOCATION_KEY, "")
        }
        if (sharedPreferences!!.contains(CONTENT_KEY)) {
            editTextContent!!.setText(sharedPreferences!!.getString(CONTENT_KEY, ""))
        }
        if (sharedPreferences!!.contains(POST_KEY)) {
            editTextPostName!!.setText(sharedPreferences!!.getString(POST_KEY, ""))
        }
        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),
                ActivityResultCallback<Map<String, Boolean>> { result: Map<String, Boolean> ->
                    val fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false
                    )
                    val coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false
                    )
                })
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        // LocationReques sets how often etc the app receives location updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10
        mLocationRequest!!.fastestInterval = 5
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    companion object {
        val REQUEST_IMAGE_CATPURE = 1
        val REQUEST_SELECT_IMAGE = 2
        const val TAG_KEY = "TAG_KEY"
        const val CONTENT_KEY = "CONTENT_KEY"
        const val LOCATION_KEY = "LOCATION_KEY"
        const val POST_KEY = "POST_KEY"
        const val FILE_NAME = "id.txt"
        const val IMAGE_FILE_NAME = "id.jpg"
    }

    fun onCameraClicked(v: View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CATPURE)
    }

    fun onPhotoLibraryClicked(v: View) {
        val pickerIntent = Intent(MediaStore.ACTION_PICK_IMAGES)
        if (pickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickerIntent, REQUEST_SELECT_IMAGE)
        }
    }
    fun save(v: View) {
        val data = editText!!.text.toString() + "|" + mOutput!!.text.toString()+"|"+editTextContent!!.text.toString()+"|"+editTextPostName.text.toString()
        val bitmap = (image_holder!!.drawable as BitmapDrawable).bitmap
        try {
            outputStream = FileOutputStream(file)
            outputStream!!.write(data.toByteArray())
            outputStream!!.close()
            outputStreamImage = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamImage)
            outputStreamImage!!.flush()
            outputStreamImage!!.close()
            Toast.makeText(this, "data saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun load(v: View?) {
        val length = file!!.length().toInt()
        val bytes = ByteArray(length)
        try {
            inputStream = FileInputStream(file)
            inputStream!!.read(bytes)
            inputStream!!.close()
            val data = String(bytes)
            editText!!.setText(data.split("|").toTypedArray()[0])
            mOutput!!.text = data.split("|").toTypedArray()[1]
            editTextContent!!.setText(data.split("|").toTypedArray()[2])
            editTextPostName!!.setText(data.split("|").toTypedArray()[3])
            inputStreamImage = FileInputStream(imageFile)
            val bitmap = BitmapFactory.decodeStream(inputStreamImage)
            inputStreamImage!!.close()
            image_holder.setImageBitmap(bitmap)
            Toast.makeText(baseContext, "data loaded", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data as Uri
            image_holder.setImageURI(selectedImageUri)
        } else if (requestCode == REQUEST_IMAGE_CATPURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            image_holder.setImageBitmap(bitmap)
        }
        generateLabels()
        locate()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun locate() {
        mLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationRequest?.let {
            mLocationProvider!!.requestLocationUpdates(
                it, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        mLastLocation = result.lastLocation!!
                    }
                }, Looper.getMainLooper()
            )
        }
        mGeocoder = Geocoder(this)
        try {
            // Only 1 address is needed here.
            val addresses = mGeocoder!!.getFromLocation(
                mLastLocation!!.latitude, mLastLocation!!.longitude, 1
            )
            if (addresses!!.size == 1) {
                val address = addresses!![0]
                val addressLines = StringBuilder()
                if (address.maxAddressLineIndex > 0) {
                    for (i in 0 until address.maxAddressLineIndex) {
                        addressLines.append(
                            """${address.getAddressLine(i)}""".trimIndent()
                        )
                    }
                } else {
                    addressLines.append(address.getAddressLine(0))
                }
                mOutput!!.text = addressLines
            } else {
                mOutput!!.text = "WARNING! Geocoder returned more than 1 addresses!"
            }
        } catch (e: Exception) {
        }
    }

    private fun generateLabels() {
        val bitmap = (image_holder!!.drawable as BitmapDrawable).bitmap
        val image = InputImage.fromBitmap(bitmap, 0)
        val laber = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        laber.process(image).addOnSuccessListener { labels ->
            var labelText = "Recommend Hashtag"
            for (label in labels) {
                val text = "#" + label.text
                labelText += "\n$text"
            }
            val finalLabelText = labelText
            runOnUiThread { editText.setText(finalLabelText) }
        }
    }

    var mIsRecording = false
    lateinit var mRecorder: MediaRecorder

    @SuppressLint("NewApi")
    fun onRecordClicked(v: View) {
        if (!mIsRecording) {

            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
            } catch (e: IOException) {
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

    lateinit var mPlayer: MediaPlayer
    var mIsPlaying = false

    @RequiresApi(Build.VERSION_CODES.R)
    fun onPlayClicked(v: View) {
        if (!mIsPlaying) {
            mPlayer = MediaPlayer()
            try {
                Toast.makeText(this, "play recording", Toast.LENGTH_LONG).show()
                val fileName = filesDir.path + "/test.m4a"
                mPlayer.setDataSource(fileName)
                mPlayer.prepare()
                mPlayer.start()
                mIsPlaying = true
            } catch (e: IOException) {

            }
        } else {
            mPlayer.release()
            mIsPlaying = false
        }
    }
}