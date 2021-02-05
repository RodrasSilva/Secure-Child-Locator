package pt.ulisboa.tecnico.childapp

import android.app.Application
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.preference.PreferenceManager
import pt.ulisboa.tecnico.childapp.utils.SecurityUtils
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val ENCRYPTION_KEYS_ALIAS = "EncryptionKeys"
private const val SIGNATURE_KEYS_ALIAS = "SignatureKeys"

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences

    internal lateinit var uniqueId: String
    private lateinit var encryptionPrivateKey: PrivateKey
    internal lateinit var encryptionPublicKey: PublicKey
    private lateinit var signaturePrivateKey: PrivateKey
    internal lateinit var signaturePublicKey: PublicKey

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        var uuid = sharedPreferences.getString("uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPreferences.edit().putString("uuid", uuid).apply()
        }
        uniqueId = uuid

        setupKeys()

        if(sharedPreferences.getBoolean("isPaired",false)){
            val guardianEncryptionPublicKeyString = sharedPreferences.getString("guardianEncryptionPublicKey", null)
            val guardianEncryptionPublicKey = readPublicKey(guardianEncryptionPublicKeyString!!)

            val guardianSignaturePublicKeyString = sharedPreferences.getString("guardianSignaturePublicKey", null)
            val guardianSignaturePublicKey = readPublicKey(guardianSignaturePublicKeyString!!)

            SecurityUtils.init(encryptionPrivateKey, signaturePrivateKey, guardianEncryptionPublicKey, guardianSignaturePublicKey)
        }
    }

    private fun checkIfKeysExist(): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.containsAlias(ENCRYPTION_KEYS_ALIAS) && keyStore.containsAlias(
            SIGNATURE_KEYS_ALIAS)
    }

    private fun setupKeys() {
        if (!checkIfKeysExist()) {
            // Generates RSA keys to cipher and decipher data
            var generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
            var spec = KeyGenParameterSpec
                .Builder(ENCRYPTION_KEYS_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()
            generator.initialize(spec)
            var generatedKeyPair = generator.generateKeyPair()
            encryptionPrivateKey = generatedKeyPair.private
            encryptionPublicKey = generatedKeyPair.public
            // Generates RSA keys to sign and verify data
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
            spec = KeyGenParameterSpec
                .Builder(SIGNATURE_KEYS_ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()
            generator.initialize(spec)
            generatedKeyPair = generator.generateKeyPair()
            signaturePrivateKey = generatedKeyPair.private
            signaturePublicKey = generatedKeyPair.public
        } else {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            encryptionPrivateKey = keyStore.getKey(ENCRYPTION_KEYS_ALIAS, null) as PrivateKey
            encryptionPublicKey = keyStore.getCertificate(ENCRYPTION_KEYS_ALIAS).publicKey as PublicKey
            signaturePrivateKey = keyStore.getKey(SIGNATURE_KEYS_ALIAS, null) as PrivateKey
            signaturePublicKey = keyStore.getCertificate(SIGNATURE_KEYS_ALIAS).publicKey as PublicKey
        }
    }

    private fun readPublicKey (stringPublicKey: String): PublicKey {
        val publicKeyDecoded = Base64.getDecoder().decode(stringPublicKey)
        val pubSpec = X509EncodedKeySpec(publicKeyDecoded)
        return KeyFactory.getInstance("RSA").generatePublic(pubSpec)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        when(key) {
            "isPaired" -> {
                val guardianEncryptionPublicKeyString = prefs.getString("guardianEncryptionPublicKey", null)
                val guardianEncryptionPublicKey = readPublicKey(guardianEncryptionPublicKeyString!!)

                val guardianSignaturePublicKeyString = prefs.getString("guardianSignaturePublicKey", null)
                val guardianSignaturePublicKey = readPublicKey(guardianSignaturePublicKeyString!!)

                SecurityUtils.init(encryptionPrivateKey, signaturePrivateKey, guardianEncryptionPublicKey, guardianSignaturePublicKey)
            }
        }
    }

    override fun onTerminate() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onTerminate()
    }
}