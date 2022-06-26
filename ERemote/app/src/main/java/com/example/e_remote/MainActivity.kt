package com.example.e_remote

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import api.*
import kotlinx.android.synthetic.main.activity_main.* // for element.setOnClickListener

class MainActivity : AppCompatActivity(), MessageListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreference =  getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)

        val username = sharedPreference.getString("username", null)
        val password = sharedPreference.getString("password", null)
        if (username.isNullOrEmpty() or password.isNullOrEmpty()){
            changeCred()
        }

        val ip = sharedPreference.getString("ip",null)
        if (ip.isNullOrEmpty()) {
            setIP()
        }

        WebSocketManager.initConnect(ip.toString(), this)
        enaSwitches(false)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreference =  getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("ip",null)
        WebSocketManager.init(ip.toString(), this)
    }


    fun onClick(v: View?) {
        when (v?.id) {
            R.id.switch_light -> {
                getReq("l=")
            }
        }

        when (v?.id) {
            R.id.switch_fan -> {
                getReq("f=")
            }
        }

        when (v?.id) {
            R.id.switch_ext -> {
                // get popup_pwd.xml view
                val li = LayoutInflater.from(this)
                val promptsView = li.inflate(R.layout.popup_pwd, null)
                val alertDialogBuilder = AlertDialog.Builder(
                    this
                )

                // set popup_pwd.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView)
                val userInput = promptsView
                    .findViewById<View>(R.id.editTextDialogUserInput) as EditText

                // set dialog message
                alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton(
                        "OK"
                    ) { dialog, id -> // get user input and send the http request
                        getReq("e=" + userInput.text)
                    }
                    .setNegativeButton(
                        "Cancel"
                    ) { dialog, id ->
                        WebSocketManager.sendMessage("queryState")
                        dialog.cancel() }

                // create alert dialog
                val alertDialog = alertDialogBuilder.create()

                // show it
                alertDialog.show()
            }
        }

        when (v?.id) {
            R.id.button_ip -> {
                setIP()
            }
        }

        when (v?.id) {
            R.id.button_cred -> {
                changeCred()
            }
        }

        when (v?.id) {
            R.id.button_fan_auto -> {
                fanAuto()
            }
        }
    }

    fun getReq(urlArg: String) {
        val sharedPreference =  getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("ip",null)
        if (ip.isNullOrEmpty()) {
            setIP()
            return
        }

        val username = sharedPreference.getString("username", null)
        val password = sharedPreference.getString("password", null)
        if (username.isNullOrEmpty() or password.isNullOrEmpty()){
            changeCred()
            return
        }

        val url = "http://" + ip + "/et?" + urlArg
        val gclient = GetClient(applicationContext, username!!, password!!)
        gclient.run(url)
    }

    fun setIP() {
        val intent = Intent(this, SetIPActivity::class.java)
        startActivity(intent)
    }

    fun changeCred() {
        val intent = Intent(this, changeCredActivity::class.java)
        startActivity(intent)
    }

    fun fanAuto() {
        val intent = Intent(this, FanAutoActivity::class.java)
        startActivity(intent)
    }

    override fun onConnectSuccess() {
        enaSwitches(true)
        showToast( " Connected successfully" )
    }

    override fun onConnectFailed() {
        enaSwitches(false)
        mainToFront()
        // showToast( " Connecting" )
    }

    override fun onClose() {
        mainToFront()
        enaSwitches(false)
        // showToast( " Closed successfully" )
    }

    override fun onMessage(text: String?) {
        val text_list = text.toString().split(",")
        if (text_list.elementAtOrNull(0) == "state") {
            setSwitchState(intToBool(text_list.elementAt(1).toInt()),
                intToBool(text_list.elementAt(2).toInt()),
                intToBool(text_list.elementAt(3).toInt()))
        }
        else if (text_list.elementAtOrNull(0) == "fa") {
            SocketData.setFanAutoMsg(text.toString())
        }
    }

    private fun showToast(text: String?) {
        runOnUiThread {
            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun enaSwitches(state: Boolean) {
        runOnUiThread {
            button_fan_auto.isEnabled = state
            switch_light.isEnabled = state
            switch_fan.isEnabled = state
            switch_ext.isEnabled = state
        }
    }

    private fun setSwitchState(lightState: Boolean, fanState: Boolean, extState: Boolean) {
        runOnUiThread {
            switch_light.isChecked = lightState
            switch_fan.isChecked = fanState
            switch_ext.isChecked = extState
        }
    }

    override fun onDestroy() {
        super .onDestroy ()
        WebSocketManager.close()
    }

    fun intToBool(num: Int) : Boolean {
        if (num > 0) {return true}
        return false
    }

    fun mainToFront() {
        if (MainNotFront.isIt) {
            val intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            MainNotFront.isIt = false
        }
    }
}