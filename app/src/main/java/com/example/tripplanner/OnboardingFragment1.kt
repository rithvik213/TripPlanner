package com.example.tripplanner

import GoogleSignInHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton

class OnboardingFragment1 : Fragment(), GoogleSignInHelper.SignInResultListener {

    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var rootView: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_onboarding1, container, false) as ViewGroup
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleSignInHelper = GoogleSignInHelper(this, this)

        setupInitialLayout(view)
    }

    private fun setupInitialLayout(view: View) {
        view.findViewById<ImageButton>(R.id.getstarted).setOnClickListener {
            switchToLayout(R.layout.discover_together)
        }
    }

    private fun switchToLayout(layoutResId: Int) {
        val newLayout = LayoutInflater.from(context).inflate(layoutResId, rootView, false)
        rootView.removeAllViews()
        rootView.addView(newLayout)

        when (layoutResId) {
            R.layout.discover_together -> setupDiscoverTogetherLayout(newLayout)
            R.layout.start_exploring -> setupStartExploringLayout(newLayout)
        }
    }

    private fun setupDiscoverTogetherLayout(view: View) {
        view.findViewById<ImageButton>(R.id.googlesigninbutton)?.setOnClickListener {
            googleSignInHelper.startSignIn()
        }
    }

    private fun setupStartExploringLayout(view: View) {
        val grantPermissionsButton = view.findViewById<ImageButton>(R.id.grantPermissions)
        val noPermissionsButton = view.findViewById<ImageButton>(R.id.noPermissions)

        grantPermissionsButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_discoverPageFragment)
        }
    }

    override fun onSignInSuccess(account: GoogleSignInAccount) {
        Log.d("GoogleSignIn", "Sign-in successful for account: ${account.displayName}")
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        //navController.navigate(R.id.action_onboardingFragment_to_homeScreenFragment)

        val bundle = Bundle().apply {
            putString("userName", account.displayName)
        }

        navController.navigate(R.id.action_onboardingFragment_to_discoverPageFragment, bundle)
        switchToLayout(R.layout.start_exploring)
    }

    override fun onSignInFailure(errorMessage: String) {
        Log.d("GoogleSignIn", "Sign-in failed with message: $errorMessage")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data)
    }
}

