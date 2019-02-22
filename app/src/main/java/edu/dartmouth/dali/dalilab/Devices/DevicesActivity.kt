package edu.dartmouth.dali.dalilab.Devices

import DALI.DALIEquipment
import DALI.DALIMember
import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.Toast
import edu.dartmouth.dali.dalilab.ActionBarActivity
import edu.dartmouth.dali.dalilab.R
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import kotlinx.android.synthetic.main.device_cell.view.*
import kotlinx.android.synthetic.main.device_header_cell.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture

const val CAMERA_PERMISSION_REQUEST_CODE = 56
const val QR_REQUEST_CODE = 32

class DevicesActivity : ActionBarActivity(), EquipmentDetailViewCheckOutListener {
    lateinit var devicesListView: ListView
    var equipment: List<DALIEquipment>? = null

    var availableEquipment: List<DALIEquipment>? = null
    var checkedOutEquipment: List<DALIEquipment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        devicesListView = this.findViewById(R.id.devicesList)
        devicesListView.adapter = Adapter(this)
    }

    override fun onResume() {
        super.onResume()

        this.reload()
    }

    fun reload() {
        DALIEquipment.getAll().onSuccess {
            runOnUiThread {
                equipment = it

                val availableEquipment = it.filter {
                    !it.isCheckedOut
                }
                val checkedOutEquipment = it.filter {
                    it.isCheckedOut
                }

                if (availableEquipment.count() != 0) {
                    this.availableEquipment = availableEquipment
                } else {
                    this.availableEquipment = null
                }

                if (checkedOutEquipment.count() != 0) {
                    this.checkedOutEquipment = checkedOutEquipment
                } else {
                    this.checkedOutEquipment = null
                }
                devicesListView.invalidateViews()
                devicesListView.invalidate()
            }
        }.onFailure {
            Log.d("DevicesActivity", "Failed! " + it.localizedMessage)
        }
    }

    fun qrButtonPressed(view: View) {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, QRScannerActivity::class.java)
            startActivityForResult(intent, QR_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    fun showDetailViewForEquipment(equipment: DALIEquipment) {
        Log.d("DevicesActivity", "Showing detail view for equipment " + equipment.name)
        equipmentDialog(equipment, this, this)
    }

    override fun checkOutButtonPressed(equipment: DALIEquipment): CompletableFuture<DALIEquipment> {
        val future = CompletableFuture<DALIEquipment>()

        val cal = Calendar.getInstance()
        cal.time = Date()

        var dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(year, month, dayOfMonth, 0, 0)
            val date = cal.time
            Log.d("DevicesActivity", date.toString())
            equipment.checkOut(date).onSuccess {
                this.reload()
                future.complete(it)
            }.onFailure {
                future.completeExceptionally(it)
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        dialog.datePicker.minDate = Date().time
        dialog.show()

        return future
    }

    override fun returnButtonPressed(equipment: DALIEquipment): CompletableFuture<DALIEquipment> {
        return equipment.returnEquipment().onSuccess { this.reload() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_REQUEST_CODE) {
            val equipment = unwrap(QRScannerActivity.result) or { return }
            this.showDetailViewForEquipment(equipment)
        }
    }

    override fun finishActivity(requestCode: Int) {
        super.finishActivity(requestCode)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, QRScannerActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to get camera permission", Toast.LENGTH_LONG)
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, DevicesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    class Adapter constructor(val devicesActivity: DevicesActivity): BaseAdapter() {
        val items: List<Any>
            get() {
                val list = ArrayList<Any>()
                if (devicesActivity.availableEquipment != null) {
                    list.add("Available")
                    list.addAll(devicesActivity.availableEquipment!!)
                }
                if (devicesActivity.checkedOutEquipment != null) {
                    list.add("Checked Out")
                    list.addAll(devicesActivity.checkedOutEquipment!!)
                }
                return list
            }

        override fun getCount(): Int {
            return items.count()
        }

        override fun getItemId(position: Int): Long {
            val item = getItem(position)
            return when (item) {
                is DALIEquipment -> item.id.hashCode().toLong()
                else -> item.hashCode().toLong()
            }
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val item = getItem(position)

            if (item is DALIEquipment) {
                val equipment = item
                val inflator = devicesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflator.inflate(R.layout.device_cell, null)
                view.deviceTitleLabel.text = equipment.name

                if (equipment.isCheckedOut) {
                    var userName = equipment.lastCheckOutRecord!!.user!!.name
                    if (equipment.lastCheckOutRecord?.user?.id == DALIMember.current?.id) {
                        userName = "You"
                    }
                    var dateFormat = SimpleDateFormat("EEEE, MMM d")
                    var dateString = dateFormat.format(equipment.lastCheckOutRecord!!.expectedReturnDate!!)


                    view.deviceDescriptionLabel.text = "Checked out by: %s".format(userName)
                    view.returnDate.text = "Expected return: %s".format(dateString)
                    view.returnDate.visibility = View.VISIBLE
                } else {
                    view.deviceDescriptionLabel.text = "Available"
                    view.returnDate.visibility = View.GONE
                }

                view.setOnClickListener {
                    this.devicesActivity.showDetailViewForEquipment(equipment)
                }

                return view
            } else if (item is String) {
                val inflator = devicesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflator.inflate(R.layout.device_header_cell, null)
                view.sectionHeader.text = item
                return view
            } else {
                return View(devicesActivity)
            }
        }
    }
}
