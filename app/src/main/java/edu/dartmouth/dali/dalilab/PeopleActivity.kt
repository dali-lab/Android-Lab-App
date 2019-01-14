package edu.dartmouth.dali.dalilab

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class PeopleActivity : ActionBarActivity() {
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PeopleActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)
    }
}
