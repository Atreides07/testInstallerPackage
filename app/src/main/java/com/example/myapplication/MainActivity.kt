package com.example.myapplication

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class MainActivity : AppCompatActivity() {

    var handler: Handler? = null;

    var action: (() -> Unit) = {};

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        action();
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCenter.start(application, "58d21be6-17de-41b2-96b5-5cc01cb716a7",
                Analytics::class.java, Crashes::class.java)

        setContentView(R.layout.activity_main)

        val constraintLayout = this.findViewById<ConstraintLayout>(R.id.MainView);
        val messageTextView=this.findViewById<TextView>(R.id.MessageTextView);

        this.findViewById<TextView>(R.id.BuildTextView).text="API ${Build.VERSION.SDK_INT}"

        handler = Handler();

        action = {
            handler!!.post {

                try {
                    packageManager.setInstallerPackageName(application.packageName, application.packageName)
                    val name=packageManager.getInstallerPackageName(application.packageName)

                    if(TextUtils.isEmpty(name)) {
                        constraintLayout.setBackgroundColor(Color.YELLOW)
                        messageTextView.text = "IPC name empty :("
                    }else{
                        constraintLayout.setBackgroundColor(Color.GREEN)
                        messageTextView.text = "IPC name: $name"
                    }
                } catch (ex: java.lang.Exception) {
                    constraintLayout.setBackgroundColor(Color.RED)
                    Log.e("MainActivity", "failed to patch installer", ex)
                    messageTextView.text = "IPC error: ${ex.message}";
                }
            }

        }

        action();
        //requestPermission(this, action, Manifest.permission.REQUEST_INSTALL_PACKAGES)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission(
            activity: Activity,
            action: () -> Unit,
            vararg permissions: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            action.invoke();
            return;
        }

        var resultPermissions= ArrayList<String>();

        for(permission in permissions) {
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                            permission) != PackageManager.PERMISSION_GRANTED
            ) {
                resultPermissions.add(permission);
            }
        }


        if(resultPermissions.isNotEmpty()) {
            try {
                val toTypedArray = resultPermissions.toTypedArray()
                activity.requestPermissions(toTypedArray, 1);
            }
            catch (ex: java.lang.Exception) {
                Log.e("permissionRequest", "error get permission", ex);
            }
        }
        else
            action();
    }

    private fun showNotification(text: String) {

        Toast.makeText(this@MainActivity,
                text,
                Toast.LENGTH_LONG)
                .show()
    }
}