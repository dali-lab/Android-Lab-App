package edu.dartmouth.dali.dalilab.Devices

import DALI.DALIEquipment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.Result
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.onSuccess
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    companion object {
        var result: DALIEquipment? = null
    }

    private lateinit var scanner: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner = ZXingScannerView(this)
        setContentView(scanner)

        title = "Scan Device QR Code"
    }

    public override fun onResume() {
        super.onResume()
        scanner.setResultHandler(this)
        scanner.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        scanner.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val result = unwrap(result) or { return }

        DALIEquipment.equipment(result.toString()).onSuccess {
            Companion.result = it
            if (it != null) runOnUiThread { finish() }
            else scanner.resumeCameraPreview(this)
        }
    }
}
