package api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.example.e_remote.MainActivity
import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class GetClient(private val context: Context, private val user: String, private val passwd: String) {
    val authenticator = DigestAuthenticator(Credentials(user, passwd))
    val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()
    val client: OkHttpClient = OkHttpClient.Builder()
        .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .addInterceptor(AuthenticationCacheInterceptor(authCache))
        // .addInterceptor(BasicAuthInterceptor(user, passwd))
        .build()

    val mHandler = Handler(Looper.getMainLooper())

    @Throws(IOException::class)
    fun run(url: String) {

        val request: Request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val mMessage = e.toString()
                Log.e(LOG_TAG, mMessage) // no need inside run()
                mHandler.post {
                    WebSocketManager.close()
                    Toast.makeText(context, mMessage, Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val mMessage = response.toString()
                if (response.isSuccessful) {
                    Log.i(LOG_TAG, mMessage) // no need inside run()
                }
                else {
                    Log.i(LOG_TAG, mMessage) // no need inside run()

                    if (response.code == 401) {
                        mHandler.post {
                            WebSocketManager.sendMessage("queryState")
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else if (response.code == 405) {mHandler.post { Toast.makeText(context, "EREM is locked", Toast.LENGTH_SHORT).show()}}

                }
            }
        }) }

    companion object {
        private const val LOG_TAG = "OkHttp"
    }
}