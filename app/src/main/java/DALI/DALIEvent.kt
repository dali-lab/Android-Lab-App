package DALI

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class DALIEvent private constructor(val name: String, val description: String?, val location: String?, val start: Date, val end: Date, val id: String) {
    companion object {
        fun get(): CompletableFuture<List<DALIEvent>> {
            val token = DALIapi.config.token?.guard { return CompletableFuture.completedFuture(null) }!!
            val urlString = "%s/api/events/week".format(DALIapi.config.serverURL.toString())

            val completableFuture = CompletableFuture<List<DALIEvent>>()
            val request = object : JsonArrayRequest(urlString,
                Response.Listener { response ->
                    val array = ArrayList<DALIEvent>()
                    for (i in 0..(response.length() - 1)) {
                        val obj = response.getJSONObject(i)
                        DALIEvent.parse(obj)?.let { array.add(it) }
                    }

                    array.toList()
                    completableFuture.complete(array)
                },
                Response.ErrorListener { error ->
                    completableFuture.completeExceptionally(error)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf("authorization" to token)
                }
            }

            DALIapi.requestQueue.add(request)
            return completableFuture
        }

        internal fun parse(json: JSONObject): DALIEvent? {
            val name = json.optString("name").guard { return null }
            val startString = json.optString("startTime").guard { return null }
            val endString = json.optString("endTime").guard { return null }
            val id = json.optString("id").guard { return null }
            val description = json.optString("description")
            val location = json.optString("location")

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("0")
            val start = format.parse(startString)
            val end = format.parse(endString)

            return DALIEvent(name, description, location, start, end, id)
        }
    }
}