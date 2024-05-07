package com.example.tripplanner

import GoogleSignInHelper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tripplanner.viewmodels.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn

class OnboardingFragment : Fragment(), GoogleSignInHelper.SignInResultListener {

    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var rootView: ViewGroup
    private lateinit var userViewModel: UserViewModel

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
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        setupInitialLayout(view)
    }

    private fun setupInitialLayout(view: View) {
        view.findViewById<ImageButton>(R.id.getstarted).setOnClickListener {
            switchToLayout(R.layout.discover_together)
        }
    }

    private fun switchToLayout(layoutResId: Int) {
        //Inflate the new layout and replace the current layout with it
        val newLayout = LayoutInflater.from(context).inflate(layoutResId, rootView, false)
        rootView.removeAllViews()
        rootView.addView(newLayout)

        //Set up specific functionalities based on the layout
        when (layoutResId) {
            R.layout.discover_together -> setupDiscoverTogetherLayout(newLayout)
            R.layout.start_exploring -> setupStartExploringLayout(newLayout)
        }
    }

    //Start sign in with Discover Togther Layout
    private fun setupDiscoverTogetherLayout(view: View) {
        view.findViewById<ImageButton>(R.id.googlesigninbutton)?.setOnClickListener {
            googleSignInHelper.startSignIn()
        }
    }

    //Request location perms in the start exploring layout
    private fun setupStartExploringLayout(view: View) {
        view.findViewById<ImageButton>(R.id.grantPermissions).setOnClickListener {
            requestLocationPermission()
        }
    }

    override fun onSignInSuccess(account: GoogleSignInAccount) {
        Log.d("GoogleSignIn", "Sign-in successful for account: ${account.displayName}")
        //If we have location permissions goto the Discover Page
        if (hasLocationPermission()) {
            account.displayName?.let { navigateToDiscoverPage(it) }
        } else {
            //If not, goto Start Exploring layout so we can request it
            switchToLayout(R.layout.start_exploring)
        }
        userViewModel.setGoogleAccount(account)
    }

    override fun onSignInFailure(errorMessage: String) {
        Log.d("GoogleSignIn", "Sign-in failed with message: $errorMessage")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("GoogleSignIn", "Request code: $requestCode, grantResults: ${grantResults.joinToString()}")

        //If location permission is granted, get the Google account and goto to the Discover Page
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (account != null) {
                Log.d("GoogleSignIn", "Permission granted and account retrieved: ${account.displayName}")
                account.displayName?.let { navigateToDiscoverPage(it) }
            } else {
                Log.d("GoogleSignIn", "Google account retrieval failed after permission granted.")
            }
        } else {
            Log.d("GoogleSignIn", "Location permission was denied by user.")
        }
    }

    private fun navigateToDiscoverPage(userName: String) {
        val bundle = Bundle().apply {
            putString("userName", userName)
        }
        findNavController().navigate(R.id.action_onboardingFragment_to_discoverPageFragment, bundle)
    }
}
