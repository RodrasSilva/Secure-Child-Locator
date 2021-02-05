package pt.ulisboa.tecnico.childapp.view.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import pt.ulisboa.tecnico.childapp.App
import pt.ulisboa.tecnico.childapp.R
import pt.ulisboa.tecnico.childapp.view.viewmodel.PairViewModel
import java.util.*

private const val QR_CODE_WIDTH = 350
private const val QR_CODE_HEIGHT = 350

class PairFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences

    private val pairViewModel: PairViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pair, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val scanBtn = view.findViewById(R.id.scanBtn) as Button
        scanBtn.setOnClickListener { launchScanner() }
        val qrCodeView = view.findViewById(R.id.qr_code) as ImageView

        val app = activity?.applicationContext as App
        val encryptionPublicKeyString = Base64.getEncoder().encodeToString(app.encryptionPublicKey.encoded)
        val signaturePublicKeyString = Base64.getEncoder().encodeToString(app.signaturePublicKey.encoded)
        val qrCodeContent = "${app.uniqueId};$encryptionPublicKeyString;$signaturePublicKeyString"

        qrCodeView.setImageBitmap(generateQRCode(qrCodeContent))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedData = result.contents.split(';')
            registerPair(scannedData)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun launchScanner() {
        IntentIntegrator.forSupportFragment(this)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            .setOrientationLocked(false)
            .setPrompt("Scan the code")
            .initiateScan()
    }

    private fun generateQRCode(content: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder
            .encodeBitmap(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT)
    }

    private fun registerPair(scannedData: List<String>) {
        val childId = (context?.applicationContext as App).uniqueId
        val guardianId = scannedData[0]
        pairViewModel.registerGuardian(childId, guardianId).observe(this@PairFragment, Observer {
            if (it) {
                sharedPreferences
                    .edit()
                    .putBoolean("isPaired", true)
                    .putString("guardianId", guardianId)
                    .putString("guardianEncryptionPublicKey", scannedData[1])
                    .putString("guardianSignaturePublicKey", scannedData[2])
                    .apply()
                Toast.makeText(context, "Successfully paired!", Toast.LENGTH_LONG).show()
                activity?.supportFragmentManager?.popBackStack()
            } else {
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG).show()
            }
        })
    }
}