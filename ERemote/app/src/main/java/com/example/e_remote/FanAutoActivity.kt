package com.example.e_remote

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import api.GetClient
import api.MainNotFront
import api.SocketData
import api.SocketDataListener
import kotlinx.android.synthetic.main.activity_fan_auto.*

const val WHICH_MESSAGE = "com.example.e_remote.WHICHMESSAGE"
const val TIME_MESSAGE = "com.example.e_remote.TIMEMESSAGE"

class FanAutoActivity : AppCompatActivity(), SocketDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fan_auto)

        SocketData.setOnMsgChangeListener(this)
        WebSocketManager.sendMessage("queryFA")

        elemVisib(View.GONE)
        MainNotFront.isIt = true
    }

    fun elemVisib(what_view: Int) {
        button_set_fan_auto_on.visibility = what_view
        button_set_fan_auto_off.visibility = what_view
        text_auto_on.visibility = what_view
        text_auto_off.visibility = what_view
        text_auto_on_time.visibility = what_view
        text_auto_off_time.visibility = what_view
    }


    override fun onFanAuto(msg: String) {
        if (msg == "DC") {
            finish()
            return
        }
        val msgList = msg.split(",")
        runOnUiThread {
            switch_fan_auto.isChecked = intToBool(msgList.elementAt(1).toInt())
            if (switch_fan_auto.isChecked) { elemVisib(View.VISIBLE) }
            else { elemVisib(View.GONE) }
        }
        text_auto_on_time.text = msgList.elementAt(2)
        text_auto_off_time.text = msgList.elementAt(3)
    }

    fun intToBool(num: Int) : Boolean {
        if (num > 0) {return true}
        return false
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.switch_fan_auto -> {
                if (switch_fan_auto.isChecked) { getReq("fa=enable") }
                else { getReq("fa=disable") }
            }
        }

        when (v?.id) {
            R.id.button_set_fan_auto_on -> {
                setFanAutoTime(true)
            }
        }

        when (v?.id) {
            R.id.button_set_fan_auto_off -> {
                setFanAutoTime(false)
            }
        }

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

    fun setFanAutoTime(isAutoOn: Boolean) {
        val intent = Intent(this, SetFanAutoActivity::class.java).apply {
            putExtra(WHICH_MESSAGE, isAutoOn)
            if (isAutoOn) { putExtra(TIME_MESSAGE, text_auto_on_time.text) }
            else { putExtra(TIME_MESSAGE, text_auto_off_time.text) }

        }
        startActivity(intent)
    }
}