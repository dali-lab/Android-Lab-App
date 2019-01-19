package DALI

import edu.dartmouth.dali.dalilab.unwrap
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
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

                return CompletableFuture.supplyAsync {
                    val response = khttp.get(urlString, headers = mapOf("authorization" to token))
                    val json = response.jsonObject
                    parse(json)
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
                val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())

                return CompletableFuture.supplyAsync {
                    val response = khttp.get(urlString, headers = mapOf("authorization" to token))
                    val json = response.jsonArray

                    var array = ArrayList<DALIMember>()
                    for (i in 0..(json.length() - 1)) {
                        val obj = json.getJSONObject(i).getJSONObject("user")
                        val member = DALIMember.parse(obj)
                        member?.let {
                            array.add(member)
                        }
                    }

                    array.toList()
                }
            }

            fun observe(): Socket {
                assertSocket()
                return socket!!
            }

            fun submit(inDALI: Boolean, entering: Boolean): CompletableFuture<Void> {
                val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
                val urlString = "%s/api/location/shared".format(DALIapi.config.serverURL.toString())

                val data = mapOf("inDALI" to inDALI, "entering" to entering, "sharing" to true)

                return CompletableFuture.supplyAsync {
                    khttp.post(urlString, headers = mapOf("authorization" to token), json = data)
                    null
                }
            }
        }
    }
}