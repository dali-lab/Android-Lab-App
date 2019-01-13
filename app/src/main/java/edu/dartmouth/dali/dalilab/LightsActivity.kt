package edu.dartmouth.dali.dalilab

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class LightsActivity : ActionBarActivity() {
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DevicesActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lights)
    }
}
