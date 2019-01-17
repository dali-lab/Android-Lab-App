package edu.dartmouth.dali.dalilab.Location

import EmitterKit.Event
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import edu.dartmouth.dali.dalilab.R
import org.altbeacon.beacon.*
import kotlin.collections.HashSet

class BeaconTracker constructor(context: Context): BeaconConsumer, MonitorNotifier {
    companion object {
        // Static members
        private var _shared: BeaconTracker? = null
        val shared: BeaconTracker
            get() = _shared!!

        fun initialize(context: Context) {
            if (shared != null) return
            _shared =
                    BeaconTracker(context)
        }
    }

    internal val determinedStateEvent = Event<Pair<Region, Int>>()
    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(context)
    private var serviceConnection: ServiceConnection? = null
    val context: Context = context
    var activeRegions = HashSet<Region>()

    init {
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.bind(this)
    }

    private fun start() {
        val regionUUIDs = context.resources.getStringArray(R.array.regions)
        val regionNames = context.resources.getStringArray(R.array.region_names)

        beaconManager.addMonitorNotifier(this)
        for (i in regionUUIDs.indices) {
            val name = regionNames[i]
            val region = Region(name, Identifier.parse(regionUUIDs[i]), null, null)
            beaconManager.startMonitoringBeaconsInRegion(region)
        }
    }

    // MARK: - MonitorNotifier

    override fun didDetermineStateForRegion(status: Int, region: Region?) {
        if (status == MonitorNotifier.INSIDE) {
            Log.d("BeaconTracker", "Inside region named " + region?.uniqueId)
        } else {
            Log.d("BeaconTracker", "Outside region named " + region?.uniqueId)
        }
        region?.let { determinedStateEvent.emit(Pair(region, status)) }
    }

    override fun didEnterRegion(region: Region?) {
        region?.let { activeRegions.add(region) }
    }

    override fun didExitRegion(region: Region?) {
        region?.let { activeRegions.remove(region) }
    }

    // MARK: - BeaconConsumer

    override fun getApplicationContext(): Context {
        return context
    }

    override fun bindService(intent: Intent?, serviceConnection: ServiceConnection?, code: Int): Boolean {
        if (this.serviceConnection != null) {
            return false
        }

        serviceConnection?.let {
            this.serviceConnection = serviceConnection
            return context.bindService(intent, serviceConnection, code)
        }
        return false
    }

    override fun unbindService(serviceConnection: ServiceConnection?) {
        serviceConnection?.let {
            if (serviceConnection == this.serviceConnection) {
                this.serviceConnection = null
            }
            context.unbindService(serviceConnection)
        }
    }

    override fun onBeaconServiceConnect() {
        start()
    }
}