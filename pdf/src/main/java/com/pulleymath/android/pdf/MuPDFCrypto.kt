package com.pulleymath.android.pdf

import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

object MuPDFCrypto {
    fun decrypt(ivAndEncryptedMessageBase64: String?, symKeyHex: String?): String? {
        val symKeyData = DatatypeConverter.parseHexBinary(symKeyHex)
        val ivAndEncryptedMessage = DatatypeConverter.parseBase64Binary(ivAndEncryptedMessageBase64)
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val blockSize = cipher.blockSize
            // create the key
            val symKey = SecretKeySpec(symKeyData, "AES")
            // retrieve random IV from start of the received message
            val ivData = ByteArray(blockSize)
            System.arraycopy(ivAndEncryptedMessage, 0, ivData, 0, blockSize)
            val iv = IvParameterSpec(ivData)
            // retrieve the encrypted message itself
            val encryptedMessage = ByteArray(ivAndEncryptedMessage.size - blockSize)
            System.arraycopy(ivAndEncryptedMessage, blockSize, encryptedMessage, 0, encryptedMessage.size)
            cipher.init(Cipher.DECRYPT_MODE, symKey, iv)
            val encodedMessage = cipher.doFinal(encryptedMessage)
            // concatenate IV and encrypted message
            String(encodedMessage, Charset.forName("UTF-8"))
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException("key argument does not contain a valid AES key")
        } catch (e: BadPaddingException) {
            null // you'd better know about padding oracle attacks
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("Unexpected exception during decryption", e)
        } catch (e:Exception) {
            throw e
        }
    }
}