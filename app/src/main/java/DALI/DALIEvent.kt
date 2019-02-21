package DALI

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import io.github.vjames19.futures.jdk8.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class DALIEvent private constructor(val name: String, val description: String?, val location: String?, val start: Date, val end: Date, val id: String) {
    companion object {
        fun get(): CompletableFuture<List<DALIEvent>> {
            val urlString = "%s/api/events/week".format(DALIapi.config.serverURL.toString())

            return ServerCommunicator.get(urlString).map {
                val array = ArrayList<DALIEvent>()
                for (i in 0..(it.arr.length() - 1)) {
                    val obj = it.arr.getJSONObject(i)
                    DALIEvent.parse(obj)?.let { array.add(it) }
                }

                array.toList()
                array
            }
        }

        internal fun parse(json: JSONObject): DALIEvent? {
            val name = json.optString("name").guard { return null }
            val startString = json.optString("startTime").guard { return null }
            val endString = json.optString("endTime").guard { return null }
            val id = json.optString("id").guard { return null }
            val description = json.optString("description")
            val location = json.optString("location")

            val start = dateFormatter().parse(startString)
            val end = dateFormatter().parse(endString)

            return DALIEvent(name, description, location, start, end, id)
        }

        fun dateFormatter(): SimpleDateFormat {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("0")
            return format
        }
    }
}