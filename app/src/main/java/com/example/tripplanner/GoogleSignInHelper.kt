import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.calendar.CalendarScopes

class GoogleSignInHelper(
    private val fragment: Fragment,
    private val resultListener: SignInResultListener
) {

    interface SignInResultListener {
        fun onSignInSuccess(account: GoogleSignInAccount)
        fun onSignInFailure(errorMessage: String)
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        GoogleSignIn.getClient(fragment.requireActivity(), gso)
    }

    fun startSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                resultListener.onSignInSuccess(account)
            } catch (e: ApiException) {
                resultListener.onSignInFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}
