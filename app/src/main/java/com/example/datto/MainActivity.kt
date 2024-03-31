package com.example.datto

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    val bottomNavigation: BottomNavigationView by lazy {
        findViewById(R.id.bottom_navigation)
    }

    val scrollView: NestedScrollView by lazy {
        findViewById(R.id.app_scroll_view)
    }

    val appBar: MaterialToolbar by lazy {
        findViewById(R.id.app_top_app_bar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_layout)

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.app_fragment)
            when (currentFragment) {
                is GroupList -> {
                    bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_home).isChecked = true
                }

                is Memories -> {
                    bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_memory).isChecked = true
                }

                is Create -> {
                    bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_event).isChecked = true
                }

                is Notification -> {
                    bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_notification).isChecked =
                        true
                }

                is Profile -> {
                    bottomNavigation.menu.findItem(R.id.bottom_app_bar_menu_profile).isChecked = true
                }
            }
        }

        appBar.setNavigationOnClickListener {
            setDefaultLayout()
            supportFragmentManager.popBackStack()
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
                    supportFragmentManager.beginTransaction().replace(R.id.app_fragment, Memories())
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

    private fun setDefaultLayout(viewBottomNav: Boolean = true) {
        bottomNavigation.visibility = if (viewBottomNav) View.VISIBLE else View.GONE
        val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin,
            layoutParams.rightMargin,
            if (viewBottomNav) 80 else 0
        )
        scrollView.layoutParams = layoutParams
    }
}