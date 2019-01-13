package DALI

import android.content.Context
import android.content.SharedPreferences
import java.net.URI
import java.net.URL

private val PREFS_FILENAME = "edu.dartmouth.dali.apiConfig"

public class DALIConfig(serverURLString: String, context: Context) {
    private val preferences: SharedPreferences

    internal val serverURL: URL
    internal val apiKey: String? = null
    internal val context: Context

    internal var _member: DALIMember? = null
    internal var member: DALIMember?
        get() { return _member }
        set(value) { _member = value }

    private var _token: String? = null
    internal var token: String?
        set(value) {
            _token = value
            preferences.edit().putString("token", value).apply()
        }
        get() {
            _token?.let { return it }
            return preferences.getString("token", null)
        }

    init {
        serverURL = URL(serverURLString)
        this.context = context
        preferences = context.getSharedPreferences(PREFS_FILENAME, 0)
    }

    internal fun urlByAppending(string: String): URL {
        return URL(serverURL, string)
    }
}