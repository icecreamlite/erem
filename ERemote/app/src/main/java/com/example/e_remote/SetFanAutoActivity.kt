package com.example.e_remote

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import api.GetClient
import kotlinx.android.synthetic.main.activity_set_fan_auto.*
import java.text.SimpleDateFormat
import java.util.*

class SetFanAutoActivity : AppCompatActivity() {
    var isAutoOn: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_fan_auto)

        isAutoOn = intent.getBooleanExtra(WHICH_MESSAGE, true)

        if (isAutoOn) { setTitle("Fan Auto On") }
        else { setTitle("Fan Auto Off") }

        val sdf = SimpleDateFormat("hh:mm a")
        val date = sdf.parse(intent.getStringExtra(TIME_MESSAGE))
        val c = Calendar.getInstance()
        c.time = date

        timePicker.hour = c.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = c.get(Calendar.MINUTE)
    }

    fun setTime(view: View) {
        var fa_what: String
        if (isAutoOn) { fa_what = "faon=" }
        else { fa_what = "faof=" }
        var hr = timePicker.hour.toString()
        if (hr.toInt() < 10) { hr = "0" + hr }
        var min  = timePicker.minute.toString()
        if (min.toInt() < 10) { min = "0" + min }
        getReq(fa_what + hr + ":" + min)
        finish()

    }

    fun getReq(urlArg: String) {
        val sharedPreference =  getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("ip",null)
        val username = sharedPreference.getString("username", null)
        val password = sharedPreference.getString("password", null)
        val url = "http://" + ip + "/et?" + urlArg
        val gclient = GetClient(applicationContext, username!!, password!!)
        gclient.run(url)
    }
}