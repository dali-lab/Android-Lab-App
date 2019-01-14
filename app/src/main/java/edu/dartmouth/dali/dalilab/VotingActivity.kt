package edu.dartmouth.dali.dalilab

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class VotingActivity : ActionBarActivity() {
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, VotingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)
    }
}
