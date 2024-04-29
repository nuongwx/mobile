package com.example.datto

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.NestedScrollView
import com.example.datto.Credential.CredentialService
import com.example.datto.utils.FirebaseNotification
import com.example.datto.utils.WidgetUpdater
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    // Handle back button to exit app
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000 // 2 seconds

    val bottomNavigation: BottomNavigationView by lazy {
        findViewById(R.id.bottom_navigation)
    }

    val scrollView: NestedScrollView by lazy {
        findViewById(R.id.app_scroll_view)
    }

    val appBar: MaterialToolbar by lazy {
        findViewById(R.id.app_top_app_bar)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //Notification init
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        // Update widget
        WidgetUpdater().update(this)

        // Credential validation
        if (CredentialService().get() != "") {
            CredentialService().credentialValidation(this)
        }

        val firebaseNotification = FirebaseNotification(this)
        firebaseNotification.subscribeToTopic("news")
        firebaseNotification.unsubscribeFromTopic("weather")
//        firebaseNotification.compose("news", "Breaking News", "A major event just happened!")
//        firebaseNotification.compose("news", "Scheduled News", "A scheduled event is about to happen!", "2024:04:29T13:47:00")
        // Set up layout
        if (CredentialService().get() == "") {
            setContentView(R.layout.activity_main)
            findViewById<Button>(R.id.sign_in_button).setOnClickListener {
                startActivity(Intent(this, SignInActivity::class.java))
            }
            findViewById<Button>(R.id.sign_up_button).setOnClickListener {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        } else {
            // Set default layout
            setContentView(R.layout.activity_app_layout)

            supportFragmentManager.addOnBackStackChangedListener {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.app_fragment)
                when (currentFragment) {
                    is GroupList -> {
                        bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_home).isChecked =
                            true
                    }

                    is Memories -> {
                        bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_memory).isChecked =
                            true
                    }

                    is Create -> {
                        bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_event).isChecked =
                            true
                    }

                    is Notification -> {
                        bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_notification).isChecked =
                            true
                    }

                    is Profile -> {
                        bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_profile).isChecked =
                            true
                    }
                }
            }

            appBar.setNavigationOnClickListener {
                handleBackEvent()
            }

            bottomNavigation.setOnItemSelectedListener { item ->

                when (item.itemId) {
                    R.id.bottom_app_bar_menu_home -> {
                        Log.d("MainActivity", "Home clicked")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, GroupList())
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    R.id.bottom_app_bar_menu_memory -> {
                        Log.d("MainActivity", "Memory clicked")
                        setDefaultLayout(false)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, Memories())
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    R.id.bottom_app_bar_menu_event -> {
                        Log.d("MainActivity", "Event clicked")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, Create())
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    R.id.bottom_app_bar_menu_notification -> {
                        Log.d("MainActivity", "Notification clicked")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, Notification())
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    R.id.bottom_app_bar_menu_profile -> {
                        Log.d("MainActivity", "Profile clicked")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.app_fragment, Profile())
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    else -> {
                        setDefaultLayout()
                        true
                    }
                }
            }

            // Set fragment
            Log.d("MainActivity", "Setting default fragment")
            supportFragmentManager.beginTransaction().replace(R.id.app_fragment, GroupList())
                .addToBackStack(null)
                .commit()
        }
    }

private fun setDefaultLayout(viewBottomNav: Boolean = true) {
    val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin,
        layoutParams.topMargin,
        layoutParams.rightMargin,
        if (viewBottomNav) resources.getDimensionPixelSize(R.dimen.bottom_navigation_height) else 0
    )
    scrollView.layoutParams = layoutParams
    bottomNavigation.visibility = if (viewBottomNav) View.VISIBLE else View.GONE
}

    private fun handleBackEvent() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            setDefaultLayout()
            supportFragmentManager.popBackStack()
        } else {
            if (backPressedTime + backPressInterval > System.currentTimeMillis()) {
                finish()
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackEvent()
    }
}