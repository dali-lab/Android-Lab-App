package edu.dartmouth.dali.dalilab

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import edu.dartmouth.dali.dalilab.Devices.DevicesActivity
import edu.dartmouth.dali.dalilab.Lights.LightsActivity
import edu.dartmouth.dali.dalilab.Location.BeaconTracker
import edu.dartmouth.dali.dalilab.People.PeopleActivity
import edu.dartmouth.dali.dalilab.Voting.VotingActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            BeaconTracker.initialize(applicationContext)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                10)
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
                    BeaconTracker.initialize(this)
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
}
