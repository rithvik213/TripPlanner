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
import android.widget.ImageView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton

class OnboardingFragment1 : Fragment(), GoogleSignInHelper.SignInResultListener {

    private lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding1, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleSignInHelper = GoogleSignInHelper(this, this)

        val signInButton = view.findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT)
        signInButton.setOnClickListener {
            googleSignInHelper.startSignIn()
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
    }

    override fun onSignInFailure(errorMessage: String) {
        Log.d("GoogleSignIn", "Sign-in failed with message: $errorMessage")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("GoogleSignIn", "onActivityResult - requestCode: $requestCode, resultCode: $resultCode")
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data)
    }
}
