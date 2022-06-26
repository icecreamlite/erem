package com.example.e_remote

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.activity_set_ipactivity.*

class SetIPActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_ipactivity)

        val sharedPreference =  getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("ip", null)
        val numPicker1 = findViewById<NumberPicker>(R.id.numberPicker1)
        numPicker1.minValue = 192
        numPicker1.maxValue = 192
        val numPicker2 = findViewById<NumberPicker>(R.id.numberPicker2)
        numPicker2.minValue = 168
        numPicker2.maxValue = 168
        val numPicker3 = findViewById<NumberPicker>(R.id.numberPicker3)
        numPicker3.minValue = 0
        numPicker3.maxValue = 255
        val numPicker4 = findViewById<NumberPicker>(R.id.numberPicker4)
        numPicker4.minValue = 0
        numPicker4.maxValue = 255
        if (!ip.isNullOrEmpty()) {
            val ipList = ip.split(".")
            val thirdVal = ipList.elementAt(2)
            val fourthVal = ipList.elementAt(3)
            numPicker3.value = thirdVal.toInt()
            numPicker4.value = fourthVal.toInt()
        }
    }

    fun saveIp(view: View) {
        val ipToSave = "192.168." + numberPicker3.value.toString() + "." + numberPicker4.value.toString()
        val sharedPreference = getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("ip", ipToSave)
        editor.commit()
        WebSocketManager.close()
        finish()
    }
}