package edu.uw.ischool.mutiay.arewethereyet

import android.R.attr.phoneNumber
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.ActivityNotFoundException
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import android.provider.Telephony
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream


const val ALARM_ACTION = "edu.uw.ischool.mutiay.ALARM"
class MainActivity : AppCompatActivity() {
    lateinit var startBtn: Button
    lateinit var message: EditText
    lateinit var phone: EditText
    lateinit var minutes: EditText
    var receiver : BroadcastReceiver? = null
    val SEND_SMS_PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        startBtn = findViewById(R.id.startButton)
        message = findViewById(R.id.messageInputLayout)
        phone = findViewById(R.id.phoneInputLayout)
        minutes = findViewById(R.id.minutesEditText)

//        phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())



        // Deal with user input minutes
        val watcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()&&message.text.isNotEmpty()&&phone.text.isNotEmpty()) {
                    startBtn.isEnabled = true} else {
                    startBtn.isEnabled = false
                    }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    val num = s.toString().toIntOrNull()
                    when {
                        num == null || num < 1 -> {
                            // If it's null or less than 1, reset the text to "1"
                            minutes.setText("1")
                            minutes.setSelection(minutes.text.length)
                        }
                    }
                }
            }
        }
        minutes.addTextChangedListener(watcher)





        startBtn.setOnClickListener {
            // Check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
            } else {
                if (phone.text.length > 15) {
                    makeToast("Invalid Phone Number")
                } else if (message.text.isEmpty()) {
                    makeToast("Need Message to send")
                } else if (minutes.text.isEmpty()) {
                    makeToast("Need interval")
                } else if (minutes.text.toString().toInt() === 0) {
                    makeToast("Need none zero interval")} else{
                    if(startBtn.text == "Start") {
                        setAlarm()

                        startBtn.setText("Stop")
                    } else {
                        stopAlarm()

                        startBtn.setText("Start")
                    } }
            }

        }


    }

    fun setAlarm() {
        val activityThis = this


        if (receiver == null) {

            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val message = message.text.toString()
                    val number = phone.text.toString()

                    showToast(number,message)

                    Log.i("working", "alarm show")


                    // Send message
                    val smsManager: SmsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(number, null, message, null, null)
                    sendMMSWithMedia(phoneNumber = number)
                }
            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // Create the PendingIntent
        val intent = Intent(ALARM_ACTION)
//        intent.putExtra("number", phone.text.toString())
//        intent.putExtra("message", message.text.toString())
//        intent.putExtra("time", minutes.text.toString())

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Get the Alarm Manager
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeeer = minutes.text.toString()
        var miliInterval = ((timeeer?.toInt() ?: 0) * 60000).toLong()
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+miliInterval, miliInterval, pendingIntent)

    }

    fun stopAlarm() {
        unregisterReceiver(receiver)
        receiver = null

    }

    fun makeToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(phoneNumber: String?, message: String?) {
        val inflater = layoutInflater
        val container: ViewGroup? = findViewById(R.id.custom_toast_container)
        val layout: View = inflater.inflate(R.layout.custom_toast, container)

        // Set the text for the caption and the body
        val textCaption: TextView = layout.findViewById(R.id.custom_toast_caption)
        val textBody: TextView = layout.findViewById(R.id.custom_toast_body)
        textCaption.text = "Texting $phoneNumber"
        textBody.text = message

        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    // Handle permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                if (startBtn.text == "Start") {
                    setAlarm()
                    startBtn.text = "Stop"
                } else {
                    stopAlarm()
                    startBtn.text = "Start"
                }
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendMMSWithMedia(phoneNumber: String) {

        val audioResId = R.raw.test1
        val videoResId = R.raw.test2


        val audioInputStream = resources.openRawResource(audioResId)
        val audioFile = File(filesDir, "test1.mp3")
        val audioOutputStream = FileOutputStream(audioFile)
        audioInputStream.copyTo(audioOutputStream)
        audioOutputStream.close()
        audioInputStream.close()

        val videoInputStream = resources.openRawResource(videoResId)
        val videoFile = File(filesDir, "test2.mp4")
        val videoOutputStream = FileOutputStream(videoFile)
        videoInputStream.copyTo(videoOutputStream)
        videoOutputStream.close()
        videoInputStream.close()


        val audioFileUri = FileProvider.getUriForFile(this, "edu.uw.ischool.mutiay.arewethereyet.fileprovider", audioFile)
        val videoFileUri = FileProvider.getUriForFile(this, "edu.uw.ischool.mutiay.arewethereyet.fileprovider", videoFile)


        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra("address", phoneNumber)
            putExtra(Intent.EXTRA_SUBJECT, "Here are some files for you!")
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(audioFileUri, videoFileUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }


        val chooser = Intent.createChooser(sendIntent, "Select App")
        try {
            startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No application found to send MMS.", Toast.LENGTH_SHORT).show()
        }
    }









}
