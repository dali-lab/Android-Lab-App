package edu.dartmouth.dali.dalilab

import DALI.DALIConfig
import DALI.DALIMember
import DALI.DALIapi
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.*
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import io.github.vjames19.futures.jdk8.*
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

class LoginActivity : AppCompatActivity() {
    val RC_SIGN_IN: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    /// Do setup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Fabric.with(this, Crashlytics())

        val config = DALIConfig("https://dalilab-api.herokuapp.com", this)
        DALIapi.configure(config)

        if (DALIapi.canRememberLogin()) {
            startLoading()
            DALIMember.loginSilently().onSuccess {
                it?.let { transitionToMain() }
            }.onFailure {
                stopLoading()
            }
        }

        val serverClientId = getString(R.string.server_client_id)
        var mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(serverClientId)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)

        var signInButton = this.findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_WIDE)
        signInButton.setOnClickListener {
            signInButtonPressed()
        }

        DALIMember.loggedInMemberChangedEvent.on {
            it?.let {
                this.transitionToMain()
            }
        }
    }

    fun startLoading() {
        val progress = this.findViewById<ProgressBar>(R.id.progressBar)
        progress.visibility = View.VISIBLE

        val signInButton = this.findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.isClickable = false
        signInButton.alpha = 0.2.toFloat()
    }

    fun stopLoading() {
        val progress = this.findViewById<ProgressBar>(R.id.progressBar)
        progress.visibility = View.INVISIBLE

        val signInButton = this.findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.isClickable = true
        signInButton.alpha = 1.0.toFloat()
    }

    fun transitionToMain() {
        val intent = MainActivity.newIntent(applicationContext)
        intent.addFlags(intent.getFlags().and(Intent.FLAG_ACTIVITY_NO_HISTORY))
        applicationContext.startActivity(intent)
    }

    override fun onBackPressed() {}

    /// The activity completed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> handleGoogleSignInResolution(data)
        }
    }

    /// MARK: - Helpers

    private fun signInButtonPressed() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    /**
     * When Google Sign In is complete, get the account as a result
     *
     * @param resultCode: Code of the result
     * @param data: The data retrieved from the Google Sign In system
     */
    private fun handleGoogleSignInResolution(data: Intent?) {
        startLoading()
        // Perform google sign-in
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        var account: GoogleSignInAccount? = null
        try {
            account = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // TODO: Handle this error
            System.out.println(e.localizedMessage)
            stopLoading()
        }

        // Get the auth code and login with the DALI API
        val authCode: String = unwrap(account?.serverAuthCode) or { return }
        DALIMember.login(authCode).onFailure {
            // TODO: Handle this error
            stopLoading()
            System.out.println(it.localizedMessage)
        }.onSuccess {
            stopLoading()
        }
    }
}
