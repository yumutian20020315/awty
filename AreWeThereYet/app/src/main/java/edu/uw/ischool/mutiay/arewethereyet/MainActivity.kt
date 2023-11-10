package edu.uw.ischool.mutiay.arewethereyet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout

const val ALARM_ACTION = "edu.uw.ischool.mutiay.ALARM"
class MainActivity : AppCompatActivity() {
    lateinit var startBtn: Button
    lateinit var message: EditText
    lateinit var phone: EditText
    lateinit var minutes: EditText
    var receiver : BroadcastReceiver? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        startBtn = findViewById(R.id.startButton)
        message = findViewById(R.id.messageInputLayout)
        phone = findViewById(R.id.phoneInputLayout)
        minutes = findViewById(R.id.minutesEditText)

        phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

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
            if (phone.text.length !== 14) {
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

    fun setAlarm() {
        val activityThis = this

        if (receiver == null) {

            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val message = intent!!.getStringExtra("message")
                    val number = intent.getStringExtra("number")
//                    Toast.makeText(context, number + ": " + message, Toast.LENGTH_SHORT).show()

                    showToast(number,message)

                    Log.i("working", "alarm show")
                }
            }
            val filter = IntentFilter(ALARM_ACTION)
            registerReceiver(receiver, filter)
        }

        // Create the PendingIntent
        val intent = Intent(ALARM_ACTION)
        intent.putExtra("number", phone.text.toString())
        intent.putExtra("message", message.text.toString())
        intent.putExtra("time", minutes.text.toString())

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Get the Alarm Manager
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeeer = intent.getStringExtra("time")
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



}
