package DALI

import EmitterKit.Event
import io.github.vjames19.futures.jdk8.ForkJoinExecutor
import io.github.vjames19.futures.jdk8.Future
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.lang.Error
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.CompletableFuture

public class DALIMember private constructor() {
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
        fun login(authCode: String): CompletableFuture<DALIMember> {
            val urlString = "%s/api/signin/code".format(DALIapi.config.serverURL.toString())

            return CompletableFuture.supplyAsync {
                val response = khttp.post(
                    urlString,
                    json = mapOf("code" to authCode))

                val json = response.jsonObject
                DALIapi.config.token = json.getString("token")

                val user = json.getJSONObject("user").guard { throw Error("No user object!") }
                val member = DALIMember.parse(user).guard { throw Error("Failed to parse member") }
                DALIapi.config.member = member
                loggedInMemberChangedEvent.emit(member)
                member
            }
        }

        fun loginSilently(): CompletableFuture<DALIMember?> {
            val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
            val urlString = "%s/api/users/me".format(DALIapi.config.serverURL.toString())

            return CompletableFuture.supplyAsync {
                val response = khttp.get(urlString, headers = mapOf("authorization" to token))
                val json = response.jsonObject
                val member = DALIMember.parse(json)
                DALIapi.config.member = member
                loggedInMemberChangedEvent.emit(member)
                member
            }
        }

        fun logout() {
            loggedInMemberChangedEvent.emit(null)
            DALIapi.config.member = null
            DALIapi.config.token = null
        }

        /**
         * Parse a member object from a given JSON object
         */
        private fun parse(json: JSONObject): DALIMember? {
            val name = json.getString("fullName").guard { return null }
            val email = json.getString("email").guard { return null }
            val isAdmin = json.getBoolean("isAdmin").guard { return null }
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