package edu.dartmouth.dali.dalilab

import DALI.DALIMember
import DALI.DALIapi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlin.math.sign
import android.support.v4.app.NavUtils
import android.view.MenuItem


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

        addPreferencesFromResource(R.xml.pref_general)
        val signOutPreference = findPreference("signOut")
        signOutPreference.setOnPreferenceClickListener {
            DALIMember.logout()
            var mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(this, mGoogleSignInOptions).signOut()

            val intent = LoginActivity.newIntent(applicationContext)
            applicationContext.startActivity(intent)
            finish()
            true
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }

        fun notifyEnterExitLab(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifyEnterExitLab", false)
        }

        fun notifyCheckIn(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifyCheckIn", true)
        }

        fun notifyVoting(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifyVoting", true)
        }

        fun sharing(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sharing", true)
        }
    }
}
