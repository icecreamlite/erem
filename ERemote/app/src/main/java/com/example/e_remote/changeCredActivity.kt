package com.example.e_remote

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class changeCredActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_cred)

        val sharedPreference = getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val userSaved = sharedPreference.getString("username", null)
        if (!userSaved.isNullOrEmpty()){
            val userTxt = findViewById<EditText>(R.id.text_username)
            userTxt.setText(userSaved)
        }
    }

    fun saveCred(view: View) {
        val userText = findViewById<EditText>(R.id.text_username)
        val usernm = userText.text.toString()
        val passText= findViewById<EditText>(R.id.password_password)
        val passwd = passText.text.toString()
        if (usernm.isNullOrEmpty() or passwd.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        val sharedPreference = getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("username", usernm)
        editor.putString("password", passwd)
        editor.commit()
        finish()
    }
}