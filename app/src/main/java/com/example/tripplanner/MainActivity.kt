package com.example.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var fabPerson: FloatingActionButton
    private lateinit var fabHome: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize BottomAppBar and FloatingActionButton and initially hide them
        bottomAppBar = findViewById(R.id.bottomAppBar)
        fabPerson = findViewById(R.id.fab_person)
        fabHome = findViewById(R.id.fab_home)

        bottomAppBar.visibility = View.GONE
        fabPerson.visibility = View.GONE
        fabHome.visibility = View.GONE

        // Set the ActionBar to null
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Add listener for navigation changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onboardingFragment -> {
                    // Hide BottomAppBar and FAB when on the onboarding screen
                    bottomAppBar.visibility = View.GONE
                    fabPerson.visibility = View.GONE
                    fabHome.visibility = View.GONE
                }
                else -> {
                    // Show BottomAppBar and FAB on other screens
                    bottomAppBar.visibility = View.VISIBLE
                    fabPerson.visibility = View.VISIBLE
                    fabHome.visibility = View.VISIBLE
                }
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}