package DALI

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.CompletableFuture

class DALIEquipment private constructor(val name: String,
                                        var lastCheckOutRecord: DALIEquipmentCheckOutRecord?,
                                        val id: String,
                                        val password: String?) {

    val isCheckedOut: Boolean
        get() {
            return lastCheckOutRecord != null && lastCheckOutRecord?.endDate == null
        }

    companion object {
        fun getAll(): CompletableFuture<List<DALIEquipment>> {
            val urlString = "%s/api/equipment".format(DALIapi.config.serverURL.toString())
            val future = CompletableFuture<List<DALIEquipment>>()

            ServerCommunicator.get(urlString).map {
                val array = ArrayList<DALIEquipment>()

                for (i in 0..(it.arr.length() - 1)) {
                    val obj = it.arr.getJSONObject(i)
                    val equipment = parse(JSONAny(obj))
                    if (equipment != null) {
                        array.add(equipment)
                    }
                }

                allOf(array.map { it.retreiveRequirements() }).onSuccess {
                    future.complete(it)
                }.onFailure {
                    future.completeExceptionally(it)
                }
            }

            return future
        }

        fun equipment(id: String): CompletableFuture<DALIEquipment?> {
            val url = "%s/api/equipment/%s".format(DALIapi.config.serverURL.toString(), id)

            return ServerCommunicator.get(url).map {
                parse(it)
            }
        }

        private fun parse(jsonAny: JSONAny?): DALIEquipment? {
            val json = unwrap(jsonAny?.optObject) or { return null }
            val name = json.getString("name")
            val id = json.getString("id")
            val password = json.optString("password")

            val checkOutRecordObject = json.optJSONObject("lastCheckOut")
            var checkOutRecord: DALIEquipmentCheckOutRecord? = null
            if (checkOutRecordObject != null) {
                checkOutRecord = DALIEquipmentCheckOutRecord.parse(JSONAny(checkOutRecordObject))
            }

            return DALIEquipment(name, checkOutRecord, id, password)
        }
    }

    fun retreiveRequirements(): CompletableFuture<DALIEquipment> {
        if (lastCheckOutRecord != null) {
            return lastCheckOutRecord!!.retreiveRequirements().map { this }
        } else {
            return CompletableFuture.completedFuture(this)
        }
    }

    fun checkOut(expectedEndDate: Date): CompletableFuture<DALIEquipment> {
        val url = "%s/api/equipment/%s/checkout".format(DALIapi.config.serverURL, id)

        val dict = mapOf("projectedEndDate" to DALIEvent.dateFormatter().format(expectedEndDate))
        val obj = JSONObject(dict)

        return ServerCommunicator.post(url, JSONAny(obj)).map {
            this.lastCheckOutRecord = DALIEquipmentCheckOutRecord.parse(it)

            this.lastCheckOutRecord!!.retreiveRequirements().join()
            this
        }
    }

    fun returnEquipment(): CompletableFuture<DALIEquipment> {
        val url = "%s/api/equipment/%s/return".format(DALIapi.config.serverURL, id)

        return ServerCommunicator.post(url, null).map {
            this.lastCheckOutRecord = DALIEquipmentCheckOutRecord.parse(it)
            this.lastCheckOutRecord!!.retreiveRequirements().join()
            this
        }
    }
}