package com.shabinder.common.providers.saavn.requests

import android.annotation.SuppressLint
import io.ktor.util.*
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

@SuppressLint("GetInstance")
@OptIn(InternalAPI::class)
actual suspend fun decryptURL(url: String): String {
    val dks = DESKeySpec("38346591".toByteArray())
    val keyFactory = SecretKeyFactory.getInstance("DES")
    val key: SecretKey = keyFactory.generateSecret(dks)

    val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding").apply {
        init(Cipher.DECRYPT_MODE, key, SecureRandom())
    }

    return cipher.doFinal(url.decodeBase64Bytes())
        .decodeToString()
        .replace("_96.mp4", "_320.mp4")
}
