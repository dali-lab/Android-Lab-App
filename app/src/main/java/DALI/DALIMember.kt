package DALI

import EmitterKit.Event
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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
        fun login(authCode: String): CompletableFuture<DALIMember> {
            val urlString = "%s/api/signin/code".format(DALIapi.config.serverURL.toString())

            val completableFuture = CompletableFuture<DALIMember>()
            val queue = Volley.newRequestQueue(DALIapi.config.context)
            val jsonRequest = JsonObjectRequest(Request.Method.POST, urlString, JSONObject(mapOf("code" to authCode)),
                Response.Listener { response ->
                    DALIapi.config.token = response.getString("token")

                    val user = response.getJSONObject("user").guard { throw Error("No user object!") }
                    val member = DALIMember.parse(user).guard { throw Error("Failed to parse member") }
                    DALIapi.config.member = member
                    loggedInMemberChangedEvent.emit(member)
                    completableFuture.complete(member)
                },
                Response.ErrorListener { error ->
                    System.out.println(error.localizedMessage)
                })

            queue.add(jsonRequest)
            return completableFuture
        }

        fun loginSilently(): CompletableFuture<DALIMember?> {
            val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
            val urlString = "%s/api/users/me".format(DALIapi.config.serverURL.toString())

            val future = CompletableFuture<DALIMember?>()

            val request = object : JsonObjectRequest(urlString, null,
                Response.Listener { response ->
                    val member = DALIMember.parse(response)
                    DALIapi.config.member = member

                    future.complete(member)
                    loggedInMemberChangedEvent.emit(member)
                },
                Response.ErrorListener { error ->
                    future.completeExceptionally(error)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf("authorization" to token)
                }
            }
            DALIapi.requestQueue.add(request)

            return future
        }

        fun logout() {
            loggedInMemberChangedEvent.emit(null)
            DALIapi.config.member = null
            DALIapi.config.token = null
        }

        /**
         * Parse a member object from a given JSON object
         */
        internal fun parse(json: JSONObject): DALIMember? {
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