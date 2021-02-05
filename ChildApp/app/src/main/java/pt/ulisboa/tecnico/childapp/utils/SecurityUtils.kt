package pt.ulisboa.tecnico.childapp.utils

import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec



object SecurityUtils {

    /**
     * Symmetric Cipher Parameters
     */
    private const val SYMMETRIC_CIPHERING_ALGORITHM = "AES"
    private const val SYMMETRIC_CIPHERING_ALGORITHM_MODE = "CBC"
    private const val SYMMETRIC_CIPHERING_ALGORITHM_PADDING = "PKCS5Padding"
    private const val SYMMETRIC_CIPHER_PARAMS =
        "$SYMMETRIC_CIPHERING_ALGORITHM/$SYMMETRIC_CIPHERING_ALGORITHM_MODE/$SYMMETRIC_CIPHERING_ALGORITHM_PADDING"
    //NEW

    private const val SYMMETRIC_CIPHERING_ALGORITHM_KEY_SIZE = 256

    /**
     * Asymmetric Cipher Parameters
     */
    private const val ASYMMETRIC_CIPHERING_ALGORITHM = "RSA"
    private const val ASYMMETRIC_CIPHERING_ALGORITHM_MODE = "ECB"
    private const val ASYMMETRIC_CIPHERING_ALGORITHM_PADDING = "PKCS1PADDING"
    private const val ASYMMETRIC_CIPHER_PARAMS =
        "$ASYMMETRIC_CIPHERING_ALGORITHM/$ASYMMETRIC_CIPHERING_ALGORITHM_MODE/$ASYMMETRIC_CIPHERING_ALGORITHM_PADDING"


    private lateinit var childCipherPrivate: PrivateKey
    private lateinit var childSignaturePrivate: PrivateKey
    private lateinit var guardianCipherPublicKey: PublicKey
    private lateinit var guardianSignaturePublic: PublicKey


    fun init(
        childCipherPrivate: PrivateKey,
        childSignaturePrivate: PrivateKey,
        guardianCipherPublicKey: PublicKey,
        guardianSignaturePublic: PublicKey
    ) {
        this.childCipherPrivate = childCipherPrivate
        this.childSignaturePrivate = childSignaturePrivate
        this.guardianCipherPublicKey = guardianCipherPublicKey
        this.guardianSignaturePublic = guardianSignaturePublic
    }


    fun hybridCipher(plainData: ByteArray, iv : ByteArray): Pair <ByteArray, ByteArray> {
        val  symmetricKey:Key= generateSymmetricKey()
        val symmetricKeyBytes = symmetricKey.encoded

        val symmetricCipher = Cipher.getInstance(SYMMETRIC_CIPHER_PARAMS)
        symmetricCipher.init(Cipher.ENCRYPT_MODE, symmetricKey, IvParameterSpec(iv))

        val asymmetricCipher = Cipher.getInstance(ASYMMETRIC_CIPHER_PARAMS)
        asymmetricCipher.init(Cipher.ENCRYPT_MODE, guardianCipherPublicKey)

        val cipheredData = symmetricCipher.doFinal(plainData)
        val cipheredSymmetricKey = asymmetricCipher.doFinal(symmetricKeyBytes)

        return Pair(cipheredData, cipheredSymmetricKey);
    }

    fun hybridDecipher(messageCiphered: ByteArray, keyCiphered: ByteArray, iv : ByteArray): ByteArray {

        val asymmetricCipher: Cipher = Cipher.getInstance(ASYMMETRIC_CIPHER_PARAMS)
        asymmetricCipher.init(Cipher.DECRYPT_MODE, childCipherPrivate)

        val keyBytes = asymmetricCipher.doFinal(keyCiphered)
        val symmetricKey: Key = SecretKeySpec(keyBytes, 0, SYMMETRIC_CIPHERING_ALGORITHM_KEY_SIZE/8, SYMMETRIC_CIPHERING_ALGORITHM)
        val symmetricCipher = Cipher.getInstance(SYMMETRIC_CIPHER_PARAMS)
        symmetricCipher.init(Cipher.DECRYPT_MODE, symmetricKey, IvParameterSpec(iv))
        return  symmetricCipher.doFinal(messageCiphered);
    }

    fun generateSymmetricKey(): Key {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(SYMMETRIC_CIPHERING_ALGORITHM_KEY_SIZE)
        return keyGen.generateKey()
    }

    fun sign(data: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(childSignaturePrivate)
            update(data)
        }
        return signature.sign()
    }

    fun verify(messageBytes: ByteArray, base64Signature: ByteArray): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(guardianSignaturePublic);
        signature.update(messageBytes);
        val decodedSignature: ByteArray = Base64.getDecoder().decode(base64Signature)
        return (signature.verify(decodedSignature))
    }


    fun generateRandomIv() : ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

}