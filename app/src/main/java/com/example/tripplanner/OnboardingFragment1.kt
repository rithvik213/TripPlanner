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
        // Handle sign-in success, e.g., navigate to the next fragment or activity
    }

    override fun onSignInFailure(errorMessage: String) {
        // Handle sign-in failure, e.g., show an error message to the user
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data)
    }
}
