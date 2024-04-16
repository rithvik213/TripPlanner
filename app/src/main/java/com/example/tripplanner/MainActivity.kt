package com.example.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomAppBar: Toolbar
    private lateinit var fabPerson: AppCompatImageButton
    private lateinit var fabHome: AppCompatImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        fabPerson = findViewById(R.id.fab_person)
        fabHome = findViewById(R.id.fab_home)

        bottomAppBar.visibility = View.GONE
        fabPerson.visibility = View.GONE
        fabHome.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        fabHome.setOnClickListener {
            navController.navigate(R.id.global_action_to_homeScreen)
        }

        fabPerson.setOnClickListener {
            navController.navigate(R.id.global_action_to_personScreen)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onboardingFragment -> {
                    bottomAppBar.visibility = View.GONE
                    fabPerson.visibility = View.GONE
                    fabHome.visibility = View.GONE
                }
                else -> {
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