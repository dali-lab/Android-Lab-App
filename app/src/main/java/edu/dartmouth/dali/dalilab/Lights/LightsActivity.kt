package edu.dartmouth.dali.dalilab.Lights

import android.content.Context
import android.content.Intent
import android.os.Bundle
import edu.dartmouth.dali.dalilab.ActionBarActivity
import edu.dartmouth.dali.dalilab.R

class LightsActivity : ActionBarActivity() {
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LightsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lights)
    }
}
