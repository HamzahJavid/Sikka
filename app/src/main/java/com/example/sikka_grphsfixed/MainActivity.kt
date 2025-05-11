package com.example.sikka_grphsfixed

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.sikka_grphsfixed.adapters.MyFragmentAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuIcon: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        viewPager = findViewById(R.id.view_pager)
        bottomNav = findViewById(R.id.bottom_nav)
        menuIcon = findViewById(R.id.menu_icon)

        viewPager.adapter = MyFragmentAdapter(this,intent.getStringExtra("UserID"))

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_records -> viewPager.currentItem = 0
                R.id.nav_analysis -> viewPager.currentItem = 1
                R.id.nav_budgets -> viewPager.currentItem = 2
                R.id.nav_accounts -> viewPager.currentItem = 3
                else -> false
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position in 0 until bottomNav.menu.size()) {
                    bottomNav.menu.getItem(position).isChecked = true
                }
            }
        })

        // Menu icon click → open drawer
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        // Handle clicks on Navigation Drawer items if you want
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> { /* Handle Home Click */ }
                R.id.nav_settings -> { /* Handle Settings Click */ }
                R.id.nav_about -> { /* Handle About Click */ }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
