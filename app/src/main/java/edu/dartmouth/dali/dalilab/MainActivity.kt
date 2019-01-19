package edu.dartmouth.dali.dalilab

import DALI.DALIEvent
import EmitterKit.Event
import EmitterKit.EventListenerDelegate
import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.squareup.picasso.Picasso
import edu.dartmouth.dali.dalilab.Devices.DevicesActivity
import edu.dartmouth.dali.dalilab.Lights.LightsActivity
import edu.dartmouth.dali.dalilab.Location.BeaconTracker
import edu.dartmouth.dali.dalilab.Location.LocationTracker
import edu.dartmouth.dali.dalilab.People.PeopleActivity
import edu.dartmouth.dali.dalilab.Voting.VotingActivity
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import kotlinx.android.synthetic.main.event_cell.view.*
import kotlinx.android.synthetic.main.person_cell.view.*
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), EventListenerDelegate<Boolean> {
    lateinit var locationLabel: TextView
    lateinit var listView: ListView
    val adapter = EventsListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BeaconTracker.initialize(applicationContext)
        locationLabel = findViewById(R.id.daliLabLocationTextView)
        listView = findViewById(R.id.listView)
        updateLocationLabel()

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTracking()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                10)
        }

        adapter.context = this
        listView.adapter = adapter

        updateData()
    }

    fun updateData() {
        DALIEvent.get().onSuccess {
            System.out.println(it.toString())
            adapter.events = it
            runOnUiThread { listView.invalidateViews() }
        }.onFailure {
            System.out.println(it.localizedMessage)
        }
    }

    fun startTracking() {
        LocationTracker.shared.inDALIEvent.on(this)
    }

    fun updateLocationLabel() {
        if (LocationTracker.shared.inDALI) {
            locationLabel.text = resources.getString(R.string.inDALITextInside)
        } else {
            locationLabel.text = resources.getString(R.string.inDALITextOutside)
        }
        System.out.println(locationLabel.text!!)
    }

    override fun eventTriggered(event: Event<Boolean>, data: Boolean) {
        runOnUiThread {
            updateLocationLabel()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            10 -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking()
                }
            }
        }
    }

    override fun onBackPressed() {}

    fun launchEquipmentView(view: View) {
        startActivity(DevicesActivity.newIntent(this))
    }

    fun launchLightsView(view: View) {
        startActivity(LightsActivity.newIntent(this))
    }

    fun launchPeopleView(view: View) {
        startActivity(PeopleActivity.newIntent(this))
    }

    fun launchVotingView(view: View) {
        startActivity(VotingActivity.newIntent(this))
    }

    fun launchSettingView(view: View) {
        startActivity(SettingsActivity.newIntent(this))
    }

    class EventsListAdapter: BaseAdapter() {
        var events: List<DALIEvent> = ArrayList()
        var context: Context? = null

        override fun getCount(): Int {
            return events.count()
        }

        override fun getItem(position: Int): Any {
            return events[position]
        }

        override fun getItemId(position: Int): Long {
            return events[position].id.hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.event_cell, null)
            view.eventName.text = events[position].name

            val start = SimpleDateFormat("E h:mm")
            val end = SimpleDateFormat("h:mm a")

            view.time.text = "%s - %s".format(start.format(events[position].start), end.format(events[position].end))
            view.location.text = events[position].location
            return view
        }
    }
}
