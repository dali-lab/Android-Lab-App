package DALI

import EmitterKit.Event
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onSuccess
import org.json.JSONObject
import java.lang.Error
import java.net.URL
import java.util.concurrent.CompletableFuture

class DALIMember private constructor() {
    companion object {
        /// Currently signed in member
        val current: DALIMember?
            get() = DALIapi.config.member

        val loggedInMemberChangedEvent = Event<DALIMember?>()

        /**
         * Login to the API with the given Google server auth code
         *
         * @param authCode: The authorization code
         * @return Future which will complete when the sign in is complete
         */
        fun login(authCode: String): CompletableFuture<DALIMember?> {
            val urlString = "%s/api/signin/code".format(DALIapi.config.serverURL.toString())

            return ServerCommunicator.post(urlString, JSONAny(JSONObject(mapOf("code" to authCode)))).map {
                val response = it.obj
                DALIapi.config.token = response.getString("token")
                val user = response.getJSONObject("user").guard { throw Error("No user object!") }
                val member = DALIMember.parse(JSONAny(user)).guard { throw Error("Failed to parse member") }
                DALIapi.config.member = member
                member
            }.onSuccess {
                loggedInMemberChangedEvent.emit(it)
            }
        }

        fun loginSilently(): CompletableFuture<DALIMember?> {
            val urlString = "%s/api/users/me".format(DALIapi.config.serverURL.toString())

            return ServerCommunicator.get(urlString).onSuccess {
                Log.d("DALIMember", it.toString())
            }
            .map {
                DALIapi.config.member = DALIMember.parse(it)
                Log.d("DALIMember", DALIapi.config.member.toString())
                DALIapi.config.member
            }.onSuccess {
                loggedInMemberChangedEvent.emit(it)
            }
        }

        fun logout() {
            loggedInMemberChangedEvent.emit(null)
            DALIapi.config.member = null
            DALIapi.config.token = null
        }

        fun getWithID(id: String): CompletableFuture<DALIMember?> {
            val urlString = "%s/api/users/%s".format(DALIapi.config.serverURL.toString(), id)

            return ServerCommunicator.get(urlString).map {
                DALIMember.parse(it)
            }
        }

        /**
         * Parse a member object from a given JSON object
         */
        internal fun parse(jsonAny: JSONAny?): DALIMember? {
            val json = unwrap(jsonAny?.optObject) or { return null }
            val name = json.getString("fullName").guard { return null }
            val email = json.getString("email").guard { return null }
            val isAdmin = json.getBoolean("isAdmin")
            val id = json.getString("id").guard { return null }
            val photoURLString = json.getString("photoUrl").guard { return null }

            var member = DALIMember()
            member.name = name
            member.email = email
            member.isAdmin = isAdmin
            member.id = id
            member.photoURL = URL(photoURLString)

            return member
        }
    }

    lateinit var name: String
        private set
    lateinit var email: String
        private set
    var isAdmin: Boolean = false
        private set
    lateinit var id: String
        private set
    lateinit var photoURL: URL
        private set
}