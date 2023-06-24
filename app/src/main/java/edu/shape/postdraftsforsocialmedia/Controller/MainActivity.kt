package edu.shape.postdraftsforsocialmedia.Controller

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
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import edu.shape.postdraftsforsocialmedia.Model.Contacts
import edu.shape.postdraftsforsocialmedia.Model.SqliteDatabase
import edu.shape.postdraftsforsocialmedia.R
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    // api id for url
    private var api_id1 = "b5998796c4cf4407a86c44f67361b265"

    private lateinit var image_holder: ImageView
    lateinit var editText: EditText
    lateinit var mOutput: TextView
    lateinit var editTextContent: EditText
    lateinit var editTextPostName: EditText
    lateinit var textViewWeather: TextView

    // member variables that hold location info
    private var mLastLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private var LatitudeText: String? = ""
    private var LongitudeText: String? = ""
    private var mGeocoder: Geocoder? = null
    private var mLocationProvider: FusedLocationProviderClient? = null
    private var file: File? = null
    private var imageFile: File? = null
    private var outputStream: FileOutputStream? = null
    private var inputStream: FileInputStream? = null
    private var outputStreamImage: FileOutputStream? = null
    private var inputStreamImage: FileInputStream? = null

    private var sharedPreferences: SharedPreferences? = null
    private var id: String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Social Draft Genius"
        image_holder = findViewById(R.id.imageView)
        image_holder.setImageResource(R.drawable.ic_launcher_foreground)
        editText = findViewById(R.id.editTextHashtag)
        editTextContent = findViewById(R.id.editTextContent)
        editTextPostName = findViewById(R.id.editTextPostName)
        mOutput = findViewById(R.id.textViewLocation)
        textViewWeather = findViewById(R.id.textViewWeather)
        id = intent.getStringExtra("id").toString()
        file = File(this.filesDir, "${id}.txt")
        imageFile = File(this.filesDir, "${id}.jpg")
        sharedPreferences = getSharedPreferences("MySharedPreMain", MODE_PRIVATE)
        //Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
        load()
        if (sharedPreferences!!.contains(TAG_KEY)) {
            editText!!.setText(sharedPreferences!!.getString(TAG_KEY, ""))
        }
        if (sharedPreferences!!.contains(LOCATION_KEY)) {
            mOutput!!.text = sharedPreferences!!.getString(LOCATION_KEY, "")
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
                    if (fineLocationGranted != null && fineLocationGranted) {
                        // Precise location access granted.
                        // permissionOk = true;
                        Toast.makeText(this, "location permission granted", Toast.LENGTH_SHORT)
                            .show()
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        // Only approximate location access granted.
                        // permissionOk = true;
                        Toast.makeText(this, "location permission granted", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // permissionOk = false;
                        // No location access granted.
                        Toast.makeText(this, "location permission not granted", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        // LocationReques sets how often etc the app receives location updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locate()
    }

    companion object {
        val REQUEST_IMAGE_CATPURE = 1
        val REQUEST_SELECT_IMAGE = 2
        const val TAG_KEY = "TAG_KEY"
        const val CONTENT_KEY = "CONTENT_KEY"
        const val LOCATION_KEY = "LOCATION_KEY"
        const val POST_KEY = "POST_KEY"

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
        val data =
            editText!!.text.toString() + "|" + mOutput!!.text.toString() + "|" + editTextContent!!.text.toString() + "|" + editTextPostName.text.toString()+"|"+textViewWeather.text.toString()
        val bitmap = (image_holder!!.drawable as BitmapDrawable).bitmap
        try {
            outputStream = FileOutputStream(file)
            outputStream!!.write(data.toByteArray())
            outputStream!!.close()
            outputStreamImage = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamImage)
            outputStreamImage!!.flush()
            outputStreamImage!!.close()
            val idInt = id.toInt()
            val contacts = Contacts(idInt,editTextPostName.text.toString())
            val dataBase = SqliteDatabase(this)
            dataBase.updateContacts(contacts)
//            Toast.makeText(this, "data saved", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SelectActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(v: View?) {
        val intent = Intent(this, SelectActivity::class.java)
        startActivity(intent)
    }

    fun load() {
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
            textViewWeather!!.text = data.split("|").toTypedArray()[4]
            inputStreamImage = FileInputStream(imageFile)
            val bitmap = BitmapFactory.decodeStream(inputStreamImage)
            inputStreamImage!!.close()
            image_holder.setImageBitmap(bitmap)
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
        getTemp()
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
            return
        }
        mLocationRequest?.let {
            mLocationProvider!!.requestLocationUpdates(
                it, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        mLastLocation = result.lastLocation!!
                        LatitudeText = mLastLocation!!.latitude.toString()
                        LongitudeText = mLastLocation!!.longitude.toString()
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
                mOutput!!.text = "Location: ${addressLines}"
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

    private fun getTemp() {
        // Instantiate the RequestQueue.
        if (LatitudeText != "") {
            val url: String =
                "https://api.weatherbit.io/v2.0/current?" + "lat=" + LatitudeText + "&lon=" + LongitudeText + "&key=" + api_id1
            var client = OkHttpClient()
//        Toast.makeText(this, mLastLocation?.latitude.toString(), Toast.LENGTH_SHORT).show()
//        val url ="https://api.weatherbit.io/v2.0/current?lat=35.7796&lon=-78.6382&key=b5998796c4cf4407a86c44f67361b265"
            //Build request
            val request = Request.Builder().url(url).build()
            // Execute request

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    // get the JSON object
                    val result = response.body?.string() ?: "[]"
                    runOnUiThread {
                        // get the Array from obj of name - "data"
                        val obj = JSONObject(result)
                        val jsonArray = obj.getJSONArray("data")
                        val showResult = jsonArray.getJSONObject(0)
                        val weatherObject = showResult.getJSONObject("weather")

                        textViewWeather.text =
                            "Weather: " + weatherObject.getString("description") + " " + showResult.getString(
                                "temp"
                            ) + " deg Celsius"
                    }
                }
            })

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
//            Toast.makeText(this,id,Toast.LENGTH_LONG).show()
            val fileName = filesDir.path + "/${id}.m4a"
            val file = File(fileName)
            mRecorder.setOutputFile(file)
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)


            try {
                mRecorder.prepare()
            } catch (e: IOException) {
//                Toast.makeText(this, "${e.localizedMessage}", Toast.LENGTH_LONG).show()
                return
            }
            Toast.makeText(this, "recording started", Toast.LENGTH_LONG).show()
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
                val fileName = filesDir.path + "/${id}.m4a"
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