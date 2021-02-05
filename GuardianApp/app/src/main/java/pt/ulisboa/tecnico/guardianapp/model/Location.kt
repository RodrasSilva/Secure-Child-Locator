package pt.ulisboa.tecnico.guardianapp.model

import com.google.gson.Gson
import com.google.gson.JsonParser
import pt.ulisboa.tecnico.guardianapp.utils.SecurityUtils
import java.util.*

data class Location (val latitude: Double = 0.0, val longitude: Double = 0.0, var timestamp: String = "")

fun String.toLocation(): Location? {
    val jsonObject = JsonParser.parseString(this).asJsonObject

    val message = Base64.getDecoder().decode(jsonObject.get("message").asString.toByteArray())
    val signature = Base64.getDecoder().decode(jsonObject.get("signature").asString.toByteArray())
    val iv = Base64.getDecoder().decode(jsonObject.get("iv").asString.toByteArray())
    val key = Base64.getDecoder().decode(jsonObject.get("key").asString.toByteArray())

    val deciphered = SecurityUtils.hybridDecipher(message, key, iv)

    if (!SecurityUtils.verify(deciphered, signature)) return null

    return Gson().fromJson(String(deciphered), Location::class.java)
}

fun Location.toSecureLocation(): String {
    val messageJson = Gson().toJson(this)
    val signature = SecurityUtils.sign(messageJson.toByteArray())
    val iv = SecurityUtils.generateRandomIv()
    val (cipheredMsg, cipheredKey) = SecurityUtils.hybridCipher(messageJson.toByteArray(), iv)

    val jsonObject = JsonParser.parseString("{}").asJsonObject
    jsonObject.addProperty("message", Base64.getEncoder().encodeToString(cipheredMsg))
    jsonObject.addProperty("signature", Base64.getEncoder().encodeToString(signature))
    jsonObject.addProperty("iv", String(iv))
    jsonObject.addProperty("key", Base64.getEncoder().encodeToString(cipheredKey))
    return jsonObject.toString()
}