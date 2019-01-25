package DALI

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

            return CompletableFuture.supplyAsync {
                val response = khttp.get(urlString, headers = mapOf("authorization" to token))
                val json = response.jsonArray

                val array = ArrayList<DALIEvent>()
                for (i in 0..(json.length() - 1)) {
                    val obj = json.getJSONObject(i)
                    DALIEvent.parse(obj)?.let { array.add(it) }
                }

                array.toList()
            }
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