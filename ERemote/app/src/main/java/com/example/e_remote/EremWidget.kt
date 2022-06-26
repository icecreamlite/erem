package com.example.e_remote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import api.GetClient

/**
 * Implementation of App Widget functionality.
 */
class EremWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.erem_widget)
            remoteViews.setOnClickPendingIntent(R.id.button_widget_l, getPendingSelfIntent(context, BTNL))
            remoteViews.setOnClickPendingIntent(R.id.button_widget_f, getPendingSelfIntent(context, BTNF))
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == BTNL) {
            getReq("l=", context)
        }
        else if (intent.action == BTNF) {
            getReq("f=", context)
        }
    }

    fun getReq(urlArg: String, context: Context?) {
        val sharedPreference =  context?.getSharedPreferences("com.example.e_remote.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE)
        val ip = sharedPreference?.getString("ip",null)
        val username = sharedPreference?.getString("username", null)
        val password = sharedPreference?.getString("password", null)
        val url = "http://" + ip + "/et?" + urlArg
        val gclient = GetClient(context!!, username!!, password!!)
        gclient.run(url)
    }

    companion object {
        private const val BTNL = "LON"
        private const val BTNF = "FON"
    }
}
