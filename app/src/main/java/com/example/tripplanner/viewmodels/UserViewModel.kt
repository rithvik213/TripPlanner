package com.example.tripplanner.viewmodels

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

// Allows us to store the account details for a user from sign in to use
// for uploading to their Google Calender
class UserViewModel : ViewModel() {
    private var googleAccount: GoogleSignInAccount? = null

    fun setGoogleAccount(account: GoogleSignInAccount) {
        googleAccount = account
    }

    fun getGoogleAccount(): GoogleSignInAccount? = googleAccount
}
