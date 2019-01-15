package edu.dartmouth.dali.dalilab

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import org.altbeacon.beacon.*

class BeaconTracker constructor(context: Context): BeaconConsumer, MonitorNotifier {
    companion object {
        // Static members
        private val IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
    }

    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(context)
    val context: Context = context

    var serviceConnection: ServiceConnection? = null

    init {
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        beaconManager.foregroundScanPeriod = 500

        val regionUUIDs = context.resources.getStringArray(R.array.regions)
        val regionNames = context.resources.getStringArray(R.array.region_names)

        for (i in regionUUIDs.indices) {
            val name = regionNames[i]
            val region = Region(name ?: i.toString(), Identifier.parse(regionUUIDs[i]), null, null)
            beaconManager.startMonitoringBeaconsInRegion(region)
        }

        beaconManager.bind(this)
    }

    // MARK: - MonitorNotifier

    override fun didDetermineStateForRegion(state: Int, region: Region?) {

    }

    override fun didEnterRegion(region: Region?) {

    }

    override fun didExitRegion(region: Region?) {

    }

    // MARK: BeaconConsumer

    override fun getApplicationContext(): Context {
        return context
    }

    override fun unbindService(serviceConnection: ServiceConnection?) {
        this.serviceConnection = null
        serviceConnection?.let {
            context.unbindService(serviceConnection)
        }
    }

    override fun bindService(intent: Intent?, serviceConnection: ServiceConnection?, i: Int): Boolean {
        if (this.serviceConnection != null) {
            return false
        }

        serviceConnection?.let {
            context.bindService(intent, serviceConnection, i)
            this.serviceConnection = serviceConnection
            return true
        }
        return false
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(this)
    }
}