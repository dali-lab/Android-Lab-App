package DALI

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import edu.dartmouth.dali.dalilab.unwrap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Error
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture

internal class ServerCommunicator {
    companion object {
        val requestQueue: RequestQueue by lazy {
            Volley.newRequestQueue(DALIapi.config.context)
        }

        fun get(urlString: String): CompletableFuture<JSONAny> {
            return doRequest(urlString)
        }

        fun post(urlString: String, data: JSONAny?, headers: Map<String, String>? = null): CompletableFuture<JSONAny> {
           return doRequest(urlString, Request.Method.POST, headers, data)
        }

        fun delete(urlString: String, data: JSONAny? = null, headers: Map<String, String>? = null): CompletableFuture<JSONAny> {
            return doRequest(urlString, Request.Method.DELETE, headers, data)
        }


        fun doRequest(urlString: String,
                      method: Int = Request.Method.GET,
                      headers: Map<String, String>? = null,
                      data: JSONAny? = null): CompletableFuture<JSONAny> {
            val future = CompletableFuture<JSONAny>()

            val request = JSONAny.Request(method, urlString, data, Response.Listener<JSONAny> {
                future.complete(it)
            }, Response.ErrorListener {
                future.completeExceptionally(it)
            })
            request.addHeader("authorization", DALIapi.config.token)
            request.addHeader("apiKey", DALIapi.config.apiKey)

            if (headers != null) {
                for (entry in headers) {
                    request.addHeader(entry.key, entry.value)
                }
            }

            this.requestQueue.add(request)

            return future
        }
    }
}


class JSONAny private constructor(private val jsonObject: JSONObject?, private val jsonArray: JSONArray?) {
    val obj: JSONObject
        get() = jsonObject!!
    val arr: JSONArray
        get() = jsonArray!!

    val optObject: JSONObject?
        get() = jsonObject
    val optArray: JSONArray?
        get() = jsonArray

    override fun toString(): String {
        if (jsonObject != null) {
            return jsonObject.toString()
        } else {
            return jsonArray!!.toString()
        }
    }

    val type: Type
        get() {
            if (jsonObject != null) {
                return Type.Object
            } else {
                return Type.Array
            }
        }

    constructor(jsonArray: JSONArray): this(null, jsonArray)
    constructor(jsonObject: JSONObject): this(jsonObject, null)

    enum class Type {
        Array,
        Object
    }

    class Request constructor(method: Int,
                              url: String,
                              data: JSONAny?,
                              listener: Response.Listener<JSONAny>,
                              errorListener: Response.ErrorListener):
        JsonRequest<JSONAny>(method, url, if (data != null) data.toString() else null, listener, errorListener) {
        val customHeaders = HashMap<String, String>()

        fun addHeader(header: String, value: String?) {
            val value = unwrap(value) or {return}
            customHeaders[header] = value
        }

        override fun getHeaders(): MutableMap<String, String> {
            return customHeaders
        }

        override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONAny> {
            val response = unwrap(response) or { return Response.error(VolleyError()) }
            val jsonString = String(response.data, Charset.defaultCharset())
            var any: JSONAny

            try {
                any = JSONAny(JSONObject(jsonString))
            } catch (e: JSONException) {
                try {
                    any = JSONAny(JSONArray(jsonString))
                } catch (e: JSONException) {
                    return Response.error(VolleyError(e))
                }
            }

            return Response.success(any, HttpHeaderParser.parseCacheHeaders(response))
        }
    }
}