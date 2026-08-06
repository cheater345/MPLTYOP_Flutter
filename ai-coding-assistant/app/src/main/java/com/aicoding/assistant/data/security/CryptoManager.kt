package com.aicoding.assistant.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "aicoding_master_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val blob = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, blob, 0, iv.size)
        System.arraycopy(encrypted, 0, blob, iv.size, encrypted.size)
        return Base64.encodeToString(blob, Base64.NO_WRAP)
    }

    fun decrypt(payload: String): String {
        val blob = Base64.decode(payload, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, blob, 0, 12))
        val decrypted = cipher.doFinal(blob, 12, blob.size - 12)
        return String(decrypted, Charsets.UTF_8)
    }

    fun tryDecrypt(payload: String): String? = try {
        decrypt(payload)
    } catch (_: Exception) {
        null
    }
}