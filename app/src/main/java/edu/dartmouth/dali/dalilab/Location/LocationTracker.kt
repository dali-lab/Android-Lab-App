package edu.dartmouth.dali.dalilab.Location

import EmitterKit.Event
import EmitterKit.EventDelegate
import EmitterKit.EventListenerDelegate
import edu.dartmouth.dali.dalilab.R
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region

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