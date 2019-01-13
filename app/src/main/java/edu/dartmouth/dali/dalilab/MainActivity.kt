package edu.dartmouth.dali.dalilab

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
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
