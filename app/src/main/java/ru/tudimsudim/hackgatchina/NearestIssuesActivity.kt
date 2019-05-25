package ru.tudimsudim.hackgatchina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import ru.tudimsudim.hackgatchina.model.Issue
import java.util.*
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import java.lang.Exception

class NearestIssuesActivity : AppCompatActivity() {

    var geo: GeoMaster = GeoMaster(this)
    private lateinit var adapter : IssuesAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        fab.setOnClickListener { view ->
            openNewIssue();
        }
        createNotificationChannel()
        geo.init()
        geo.updateGeofence(
            Collections.singletonList(
                Issue(
                    id = "519",
                    title = "Test",
                    text = "Alarma! Pomagite",
                    longitude = 30.3162999,
                    latitude = 59.9698522
                )
            )
        )


        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = IssuesAdapter(width)
        recycler_view.adapter = adapter

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        var locations = geo.getUpdatedLocations()
        println(locations?.longitude)
        println(locations?.latitude)


        GlobalScope.launch(Dispatchers.Main) {
            var issues = emptyList<Issue>()
            try {
                val document = withContext(Dispatchers.IO) {
                    issues = HttpClient.getIssues()
                }
            }catch (ex: Exception){
                ex.printStackTrace()
            }
            adapter.update(issues)
        }
    }

    private fun openNewIssue() {
        val message = "stub"
        val intent = Intent(this, NewIssueActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("main_id", "Main", importance).apply {
                description = "Main channel"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
