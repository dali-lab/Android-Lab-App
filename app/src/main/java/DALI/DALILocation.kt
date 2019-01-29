package DALI

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import edu.dartmouth.dali.dalilab.unwrap
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.CompletableFuture

class DALILocation {

    class Tim private constructor(inDALI: Boolean, inOffice: Boolean) {
        var inDALI: Boolean = inDALI
            private set

        var inOffice: Boolean = inOffice
            private set

        companion object {
            fun get(): CompletableFuture<Tim> {
                val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
                val urlString = "%s/api/location/tim".format(DALIapi.config.serverURL.toString())

                val future = CompletableFuture<Tim>()
                val request = object : JsonObjectRequest(urlString, null,
                    Response.Listener<JSONObject> {
                        future.complete(parse(it))
                    },
                    Response.ErrorListener {
                        future.completeExceptionally(it)
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        return mutableMapOf("authorization" to token)
                    }
                }

                DALIapi.requestQueue.add(request)

                return future
            }

            private fun parse(json: JSONObject): Tim? {
                val inDALI = json.getBoolean("inDALI").guard { return null }
                val inOffice = json.getBoolean("inOffice").guard { return null }

                return Tim(inDALI, inOffice)
            }
        }
    }

    class Shared private constructor() {
        companion object {
            var socket: Socket? = null
            fun assertSocket() {
                socket = DALIapi.socketManager.socket("/location")
                socket?.connect()
                socket?.emit("authenticate", listOf(DALIapi.config.token))
            }

            fun get(): CompletableFuture<List<DALIMember>> {
                val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())

                val future = CompletableFuture<List<DALIMember>>()
                val request = object : JsonArrayRequest(urlString, Response.Listener<JSONArray> {
                    var array = ArrayList<DALIMember>()
                    for (i in 0..(it.length() - 1)) {
                        val obj = it.getJSONObject(i).getJSONObject("user")
                        val member = DALIMember.parse(obj)
                        member?.let {
                            array.add(member)
                        }
                    }

                    future.complete(array.toList())
                }, Response.ErrorListener {
                    future.completeExceptionally(it)
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        return mutableMapOf("authorization" to token)
                    }
                }

                DALIapi.requestQueue.add(request)

                return future
            }

            fun observe(): Socket {
                assertSocket()
                return socket!!
            }

            fun submit(inDALI: Boolean, entering: Boolean): CompletableFuture<Void> {
                val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())

                val data = mapOf("inDALI" to inDALI, "entering" to entering, "sharing" to true)

                val future = CompletableFuture<Void>()
                val request = object : JsonObjectRequest(Request.Method.POST, urlString, JSONObject(data), Response.Listener {
                    future.complete(null)
                }, Response.ErrorListener {
                    future.completeExceptionally(it)
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        return mutableMapOf("authorization" to token)
                    }
                }

                DALIapi.requestQueue.add(request)
                return future
            }
        }
    }
}