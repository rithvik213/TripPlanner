package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

//Main activity is just used as a container and for the bottom app bar
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomAppBar: Toolbar
    private lateinit var fabPerson: AppCompatImageButton
    private lateinit var fabHome: AppCompatImageButton
    private lateinit var fabTrips: AppCompatImageButton
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        fabPerson = findViewById(R.id.fab_person)
        fabHome = findViewById(R.id.fab_home)
        fabTrips = findViewById(R.id.fab_trips)

        bottomAppBar.visibility = View.GONE
        fabPerson.visibility = View.GONE
        fabHome.visibility = View.GONE
        fabTrips.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Add a listener to log every time navigation occurs
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavController", "Navigated to ${destination.label}")
        }

        fabHome.setOnClickListener {
            navController.popBackStack(R.id.discoverPageFragment, false)
        }

        fabPerson.setOnClickListener {
            navController.navigate(R.id.global_action_to_personScreen)
        }

        fabTrips.setOnClickListener {
            navController.navigate(R.id.global_action_to_tripScreen)
        }

        //Make sure we don't have the bottom app bar in the onboarding section
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onboardingFragment -> {
                    bottomAppBar.visibility = View.GONE
                    fabPerson.visibility = View.GONE
                    fabHome.visibility = View.GONE
                    fabTrips.visibility = View.GONE
                }
                else -> {
                    bottomAppBar.visibility = View.VISIBLE
                    fabPerson.visibility = View.VISIBLE
                    fabHome.visibility = View.VISIBLE
                    fabTrips.visibility = View.VISIBLE
                }
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}