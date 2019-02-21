package DALI

import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.map
import io.reactivex.Completable
import java.util.*
import java.util.concurrent.CompletableFuture

class DALIEquipmentCheckOutRecord internal constructor(val startDate: Date,
                                                       val endDate: Date?,
                                                       val expectedReturnDate: Date?,
                                                       var user: DALIMember? = null,
                                                       val userID: String? = null) {

    companion object {
        internal fun parse(jsonAny: JSONAny): DALIEquipmentCheckOutRecord? {
            val dict = unwrap(jsonAny.optObject) or { return null }
            val startDateString = unwrap(dict.optString("startDate")) or { return null }
            val endDateString = dict.optString("endDate")
            val expectedReturnDateString = dict.optString("projectedEndDate")

            val startDate = DALIEvent.dateFormatter().parse(startDateString)
            var endDate: Date? = null
            var expectedReturnDate: Date? = null

            if (endDateString != null && !endDateString.isEmpty()) {
                endDate = DALIEvent.dateFormatter().parse(endDateString)
            }
            if (expectedReturnDateString != null && !expectedReturnDateString.isEmpty()) {
                expectedReturnDate = DALIEvent.dateFormatter().parse(expectedReturnDateString)
            }

            if ((endDate == null && expectedReturnDate == null) || (endDate != null && expectedReturnDate != null)) {
                return null
            }

            var user: DALIMember? = null
            var userID: String? = null
            val userJSON = dict.optJSONObject("user")
            if (userJSON != null) {
                user = DALIMember.parse(JSONAny(userJSON))
            }
            if (user == null) {
                userID = unwrap(dict.optString("user")) or { return null }
            }

            return DALIEquipmentCheckOutRecord(startDate, endDate, expectedReturnDate, user = user, userID = userID)
        }
    }

    fun retreiveRequirements(): CompletableFuture<DALIEquipmentCheckOutRecord> {
        if (user != null || userID == null) {
            return CompletableFuture.completedFuture(null)
        }

        return  DALIMember.getWithID(userID).map {
            this.user = it
            this
        }
    }
}