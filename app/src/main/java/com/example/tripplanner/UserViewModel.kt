package com.example.tripplanner

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class UserViewModel : ViewModel() {
    private var googleAccount: GoogleSignInAccount? = null

    fun setGoogleAccount(account: GoogleSignInAccount) {
        googleAccount = account
    }

    fun getGoogleAccount(): GoogleSignInAccount? = googleAccount
}
