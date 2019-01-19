package edu.dartmouth.dali.dalilab.People

import DALI.DALILocation
import DALI.DALIMember
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ScrollView
import com.squareup.picasso.Picasso
import edu.dartmouth.dali.dalilab.ActionBarActivity
import edu.dartmouth.dali.dalilab.R
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import kotlinx.android.synthetic.main.person_cell.view.*
import java.net.URI
import java.net.URL

class PeopleActivity : ActionBarActivity() {
    private lateinit var gridView: GridView
    val adapter = Adapter()

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PeopleActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        gridView = findViewById(R.id.gridView)
        adapter.context = this
        gridView.adapter = adapter

        updateData()
    }

    fun updateData() {
        DALILocation.Shared.get().onSuccess {
            adapter.members.clear()
            adapter.members.addAll(it)
            runOnUiThread {
                gridView.invalidate()
                gridView.invalidateViews()
//                findViewById<ScrollView>(R.id.scrollView).invalidate()
            }
        }.onFailure {
            System.out.println(it.localizedMessage)
        }
    }

    class Adapter: BaseAdapter() {
        var members = ArrayList<DALIMember>()
        var context: Context? = null

        override fun getCount(): Int {
            return members.count()
        }

        override fun getItem(position: Int): Any {
            return members[position]
        }

        override fun getItemId(position: Int): Long {
            return members[position].id.hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.person_cell, null)
            val url: URL = members[position].photoURL
            view.name.text = members[position].name

            Picasso.get().load(url.toString()).into(view.image)
            return view
        }
    }
}
