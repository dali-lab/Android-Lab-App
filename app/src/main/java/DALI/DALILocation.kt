package DALI

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onSuccess
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
            fun get(): CompletableFuture<Tim?> {
                val urlString = "%s/api/location/tim".format(DALIapi.config.serverURL.toString())

                return ServerCommunicator.get(urlString).map {
                    parse(it.obj)
                }
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
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())
                return ServerCommunicator.get(urlString).map {
                    val array = ArrayList<DALIMember>()

                    for (i in 0..(it.arr.length() - 1)) {
                        val obj = it.arr.getJSONObject(i).getJSONObject("user")
                        val member = DALIMember.parse(JSONAny(obj))
                        member?.let {
                            array.add(member)
                        }
                    }

                    array
                }
            }

            fun observe(): Socket {
                assertSocket()
                return socket!!
            }

            fun submit(inDALI: Boolean, entering: Boolean): CompletableFuture<Any> {
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())
                val data = JSONObject(mapOf("inDALI" to inDALI, "entering" to entering, "sharing" to true))

                return ServerCommunicator.post(urlString, JSONAny(data)).map {}
            }
        }
    }
}