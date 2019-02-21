package edu.dartmouth.dali.dalilab.Devices

import DALI.DALIEquipment
import DALI.DALIMember
import android.app.Activity
import android.app.AlertDialog
import android.view.View
import edu.dartmouth.dali.dalilab.R
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.onSuccess
import kotlinx.android.synthetic.main.equipment_detail_view.view.*
import java.util.concurrent.CompletableFuture

var alertDialog: AlertDialog? = null

fun equipmentDialog(equipment: DALIEquipment, activity: Activity, checkOutButtonListener: EquipmentDetailViewCheckOutListener) {
    val dialogBuilder = AlertDialog.Builder(activity)
    val inflater = activity.layoutInflater
    val dialogView = inflater.inflate(R.layout.equipment_detail_view, null)
    dialogBuilder.setView(dialogView)

    dialogView.checkOutButton.setOnClickListener {
        activity.runOnUiThread {
            alertDialog?.dismiss()
            alertDialog = null
        }
        val future = if (equipment.isCheckedOut) {
            checkOutButtonListener.returnButtonPressed(equipment)
        } else {
            checkOutButtonListener.checkOutButtonPressed(equipment)
        }
        future.onSuccess {
            activity.runOnUiThread {
                equipmentDialog(equipment, activity, checkOutButtonListener)
            }
        }
    }

    dialogView.titleLabel.text = equipment.name
    dialogView.idLabel.text = equipment.id
    if (equipment.password != null && equipment.password != "") {
        dialogView.passwordLabel.text = "Password: %s".format(equipment.password)
        dialogView.passwordLabel.visibility = View.VISIBLE
    } else {
        dialogView.passwordLabel.visibility = View.GONE
    }

    val userIsSame = equipment.lastCheckOutRecord?.user?.id == DALIMember.current?.id

    if (equipment.isCheckedOut && userIsSame) {
        dialogView.checkOutButton.isEnabled = true
        dialogView.checkOutButton.text = "Return"
    } else if (!equipment.isCheckedOut) {
        dialogView.checkOutButton.isEnabled = true
        dialogView.checkOutButton.text = "Check out"
    } else {
        dialogView.checkOutButton.isEnabled = false
        dialogView.checkOutButton.text = "Check out"
    }

    alertDialog = dialogBuilder.show()
}

interface EquipmentDetailViewCheckOutListener {
    fun checkOutButtonPressed(equipment: DALIEquipment): CompletableFuture<DALIEquipment>
    fun returnButtonPressed(equipment: DALIEquipment): CompletableFuture<DALIEquipment>
}