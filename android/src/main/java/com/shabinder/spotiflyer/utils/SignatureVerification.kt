package com.shabinder.spotiflyer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import com.shabinder.spotiflyer.App
import java.security.MessageDigest

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
fun checkAppSignature(context: Context): Boolean {
    try {
        val packageInfo: PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in packageInfo.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val currentSignature: String = Base64.encodeToString(md.digest(), Base64.DEFAULT)
            // Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:$currentSignature")
            // Log.d("REMOVE_ME HEX", "Include this string as a value for SIGNATURE Hex:${currentSignature.toByteArray().toHEX()}")

            // compare signatures
            if (App.SIGNATURE_HEX == currentSignature.toByteArray().toHEX()) {
                return true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // assumes an issue in checking signature., but we let the caller decide on what to do.
    }
    return false
}

fun ByteArray.toHEX(): String {
    val builder = StringBuilder()
    for (aByte in this) {
        builder.append(String.format("%02x", aByte))
    }
    return builder.toString()
}
