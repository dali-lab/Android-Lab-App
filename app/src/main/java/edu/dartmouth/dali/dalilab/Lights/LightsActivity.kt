package edu.dartmouth.dali.dalilab.Lights

import DALI.DALILights
import EmitterKit.Event
import EmitterKit.EventListenerDelegate
import EmitterKit.Listener
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import edu.dartmouth.dali.dalilab.ActionBarActivity
import edu.dartmouth.dali.dalilab.R
import edu.dartmouth.dali.dalilab.unwrap
import kotlinx.android.synthetic.main.person_cell.view.*
import kotlinx.android.synthetic.main.scene_cell.view.*
import java.net.URL
import android.widget.TextView



class LightsActivity : ActionBarActivity(), EventListenerDelegate<List<DALILights.Group>>, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {
    lateinit var imageView: ImageView
    lateinit var imageViewOverlay: ImageView
    lateinit var selectedGroupTextView: TextView
    lateinit var onSwitch: Switch
    lateinit var listView: ListView
    lateinit var overlay: View
    lateinit var adapter: Adapter

    var selectedGroup: DALILights.Group? = null
    var groups: List<DALILights.Group>? = null
    var listener: Listener? = null

    val buttonsForGroups = HashMap<String, Button>()
    val groupsForButtons = HashMap<Int, DALILights.Group>()

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, LightsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lights)

        imageView = findViewById(R.id.imageViewMain)
        imageViewOverlay = findViewById(R.id.imageViewOverlay)
        selectedGroupTextView = findViewById(R.id.groupNameTextView)
        onSwitch = findViewById(R.id.onSwitch)
        onSwitch.visibility = View.INVISIBLE
        listView = findViewById(R.id.listView)
        overlay = findViewById(R.id.progress_overlay)
        overlay.visibility = View.VISIBLE

        listView.divider = null
        listView.dividerHeight = 0
        listView.onItemClickListener = this

        adapter = Adapter()
        adapter.context = this
        listView.adapter = adapter

        val fakeHeader = layoutInflater.inflate(R.layout.lights_list_header, listView, false) as TextView
        listView.addHeaderView(fakeHeader)

        onSwitch.setOnCheckedChangeListener(this)

        listener = DALILights.shared.observeEvent.on(this)

        buttonsForGroups["kitchen"] = findViewById(R.id.kitchenButton)
        buttonsForGroups["conference"] = findViewById(R.id.conferenceButton)
        buttonsForGroups["tvspace"] = findViewById(R.id.tvSpaceButton)
        buttonsForGroups["workstations"] = findViewById(R.id.workstationsButton)
        buttonsForGroups["pod:appa"] = findViewById(R.id.podAppaButton)
        buttonsForGroups["pod:momo"] = findViewById(R.id.podMomoButton)
        buttonsForGroups["pod:pabu"] = findViewById(R.id.podPabuButton)
        buttonsForGroups["pods"] = findViewById(R.id.podsButton)
        buttonsForGroups["all"] = findViewById(R.id.allButton)
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.isListening = false
    }

    override fun eventTriggered(event: Event<List<DALILights.Group>>, data: List<DALILights.Group>) {
        groups = data

        data.forEach {
            val button = buttonsForGroups[it.name] ?: return
            groupsForButtons[button.id] = it
        }
        groupsForButtons[R.id.podsButton] = DALILights.Group.pods
        groupsForButtons[R.id.allButton] = DALILights.Group.all
        runOnUiThread {
            overlay.visibility = View.GONE
            updateCardView()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        selectedGroup?.setIsOn(isChecked)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedGroup = unwrap(selectedGroup) or {
            return
        }
        if (position == 0) {
            return
        }

        val scene = selectedGroup.scenes[position - 1]
        selectedGroup.setScene(scene)
    }

    fun selectGroup(group: DALILights.Group?) {
        selectedGroup = group
        val group = unwrap(group) or {
            imageViewOverlay.visibility = View.INVISIBLE
            updateCardView()
            return
        }

        val id = when (group.name) {
            "kitchen" -> R.drawable.lights_kitchen
            "conference" -> R.drawable.lights_conference_overlay
            "tvspace" -> R.drawable.lights_tvspace_overlay
            "workstations" -> R.drawable.lights_workstations_overlay
            "pod:appa" -> R.drawable.lights_pod_appa_overlay
            "pod:momo" -> R.drawable.lights_pod_momo_overlay
            "pod:pabu" -> R.drawable.lights_pod_pabu_overlay
            "all" -> R.drawable.lights_all_overlay
            "pods" -> R.drawable.lights_pods_overlay
            else -> R.drawable.lights_none_overlay
        }

        imageViewOverlay.visibility = View.VISIBLE
        imageViewOverlay.setImageResource(id)
        updateCardView()
    }

    fun updateCardView() {
        val selectedGroup = unwrap(selectedGroup) or {
            selectedGroupTextView.text = resources.getText(R.string.noGroupSelected)
            onSwitch.visibility = View.INVISIBLE
            listView.visibility = View.INVISIBLE
            return
        }

        selectedGroupTextView.text = selectedGroup.formattedName
        onSwitch.visibility = View.VISIBLE
        onSwitch.setOnCheckedChangeListener(null)
        onSwitch.isChecked = selectedGroup.isOn
        onSwitch.setOnCheckedChangeListener(this)

        listView.visibility = View.VISIBLE
        adapter.scenes = selectedGroup.scenes
        adapter.selectedScene = selectedGroup.scene
        listView.invalidateViews()
    }

    fun buttonPressed(view: View) {
        val group = groupsForButtons[view.id]

        if (selectedGroup == group) selectGroup(null)
        else selectGroup(groupsForButtons[view.id])
    }

    class Adapter: BaseAdapter() {
        var context: Context? = null
        var scenes: List<String> = emptyList()
        var selectedScene: String? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.scene_cell, null)
            view.textView.text = scenes[position].capitalize()

            if (selectedScene != null && scenes[position] == selectedScene) {
                view.setBackgroundColor(context!!.resources.getColor(R.color.selectedRow))
            }
            return view
        }

        override fun getCount(): Int {
            return scenes.count()
        }

        override fun getItem(position: Int): Any {
            return scenes[position]
        }

        override fun getItemId(position: Int): Long {
            return scenes[position].hashCode().toLong()
        }
    }
}
