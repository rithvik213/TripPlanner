package com.example.tripplanner

import GoogleSignInHelper
import android.content.Intent
import android.os.Bundle
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding1, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleSignInHelper = GoogleSignInHelper(this, this)

        // Correctly reference the SignInButton
        val signInButton = view.findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT)
        signInButton.setOnClickListener {
            // Invoke your sign-in helper here
            googleSignInHelper.startSignIn()
        }
    }


    override fun onSignInSuccess(account: GoogleSignInAccount) {
        // Obtain the NavHostFragment using the FragmentManager from the hosting activity
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Get the NavController from the NavHostFragment
        val navController = navHostFragment.navController

        // Navigate to the desired fragment using the action defined in the navigation graph
        //navController.navigate(R.id.action_onboardingFragment_to_newTripFragment)
        navController.navigate(R.id.action_onboardingFragment_to_homeScreenFragment)
    }



    override fun onSignInFailure(errorMessage: String) {
        // Handle sign-in failure, e.g., show an error message to the user
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data)
    }
}
