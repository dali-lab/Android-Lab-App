package edu.dartmouth.dali.dalilab.Location

import DALI.DALILocation
import EmitterKit.Event
import EmitterKit.EventDelegate
import EmitterKit.EventListenerDelegate
import edu.dartmouth.dali.dalilab.R
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.R.id.message
import android.app.PendingIntent
import android.content.Intent
import edu.dartmouth.dali.dalilab.MainActivity
import android.app.Notification
import android.content.Context


class LocationTracker: EventListenerDelegate<Pair<Region, Int>>, EventDelegate {
    companion object {
        private var _shared: LocationTracker? = null
        val shared: LocationTracker
            get() {
                if (_shared == null) {
                    _shared =
                            LocationTracker()
                }
                return _shared!!
            }
    }

    enum class Location(val id: String) {
        DALI(BeaconTracker.shared.context.resources.getString(R.string.DALI)),
        CHECK_IN(BeaconTracker.shared.context.resources.getString(R.string.Checkin))
    }

    val listener = BeaconTracker.shared.determinedStateEvent.on(this)

    val inDALIEvent = Event<Boolean>(this)
    val inDALI: Boolean
        get() = inLocation(Location.DALI)

    val inCheckInEvent = Event<Boolean>(this)
    val inCheckIn: Boolean
        get() = inLocation(Location.CHECK_IN)

    fun inLocation(location: Location): Boolean {
        return BeaconTracker.shared.activeRegions.firstOrNull {
            return it.uniqueId == location.id
        } != null
    }

    // MARK: - EventListenerDelegate

    override fun eventTriggered(event: Event<Pair<Region, Int>>, data: Pair<Region, Int>) {
        if (data.first.uniqueId == Location.DALI.id) {
            inDALIEvent.emit(data.second == MonitorNotifier.INSIDE)
            DALILocation.Shared.submit(data.second == MonitorNotifier.INSIDE, true)

            val notifyIntent = Intent(BeaconTracker.shared.context, MainActivity::class.java)
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivities(
                BeaconTracker.shared.context, 0,
                arrayOf(notifyIntent), PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = Notification.Builder(BeaconTracker.shared.context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Welcome back!")
                .setContentText("Something draws near")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            val notificationManager = BeaconTracker.shared.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager!!.notify(1, notification)

        } else if (data.first.uniqueId == Location.CHECK_IN.id) {
            inCheckInEvent.emit(data.second == MonitorNotifier.INSIDE)
        }
    }

    // MARK: - EventDelegate

    override fun eventListenersStarted(forEvent: Event<*>) {
        listener.isListening = true
    }

    override fun eventListenersStopped(forEvent: Event<*>) {
        listener.isListening = !inCheckInEvent.listenersStopped || !inDALIEvent.listenersStopped
    }
}