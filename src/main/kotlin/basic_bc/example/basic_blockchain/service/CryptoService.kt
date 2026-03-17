package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

@Service
class CryptoService(private val userKeyRepository: UserKeyRepository) {

    // ─── Load Keys ────────────────────────────────────────
    private fun loadPublicKey(base64PublicKey: String): PublicKey {
        val bytes = Base64.getDecoder().decode(base64PublicKey)
        return KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(bytes))
    }

    private fun loadPrivateKey(base64PrivateKey: String): PrivateKey {
        val bytes = Base64.getDecoder().decode(base64PrivateKey)
        return KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(bytes))
    }

    // ─── OAEP Params ──────────────────────────────────────
    private fun oaepParams() = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,  // ✅ Match Python bot
        PSource.PSpecified.DEFAULT
    )

    // ─── Pure RSA Encrypt ─────────────────────────────────
    fun rsaEncrypt(data: String, username: String): String {
        val user = userKeyRepository.findByUsernameIgnoreCase(username)
            ?: throw (ResourceNotFoundException("Not found username"))

        val publicKey = loadPublicKey(user.publicKey)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams())
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // ─── Pure RSA Decrypt ─────────────────────────────────
    fun rsaDecrypt(encryptedData: String, base64PrivateKey: String): String {
        val privateKey = loadPrivateKey(base64PrivateKey)
        println("This is privateKey= $privateKey")
        return decryptMessage(privateKey, encryptedData)
    }

    // ─── Hybrid Encrypt (AES + RSA) ───────────────────────
    fun hybridEncrypt(data: String, username: String): String {
        val user = userKeyRepository.findByUsernameIgnoreCase(username)
            ?: throw (ResourceNotFoundException("Not found username"))
        val publicKey = loadPublicKey(user.publicKey)

        // Step 1 — Generate random AES key and IV
        val aesKey = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) } //pass
        val iv = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }

        // Step 2 — Encrypt plain text with AES
        val secretKey = SecretKeySpec(aesKey, "AES")
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encryptedData = aesCipher.doFinal(data.toByteArray())

        // Step 3 — Encrypt AES key with RSA
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams())
        val encryptedAesKey = rsaCipher.doFinal(aesKey)

        // Step 4 — Combine: encryptedKey.iv.encryptedData
        return (
                Base64.getEncoder().encodeToString(encryptedAesKey) + "." +
                        Base64.getEncoder().encodeToString(iv) + "." +
                        Base64.getEncoder().encodeToString(encryptedData)
                )
    }

    // ─── Hybrid Decrypt (AES + RSA) ───────────────────────
    fun hybridDecrypt(encryptedCombined: String, base64PrivateKey: String): String {
        val privateKey = loadPrivateKey(base64PrivateKey)

        // Auto detect hybrid or pure RSA
        val parts = encryptedCombined.split(".")
        if (parts.size != 3) {
            return decryptMessage(privateKey, encryptedCombined)
        }

        val encryptedAesKey = Base64.getDecoder().decode(parts[0])
        val iv              = Base64.getDecoder().decode(parts[1])
        val encryptedData   = Base64.getDecoder().decode(parts[2])

        // Step 1 — Decrypt AES key with RSA
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams())
        val aesKey = rsaCipher.doFinal(encryptedAesKey)

        // Step 2 — Decrypt data with AES
        val secretKey = SecretKeySpec(aesKey, "AES")
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val decryptedBytes = aesCipher.doFinal(encryptedData)

        return String(decryptedBytes)
    }

    // ─── Base Decrypt Function ────────────────────────────
    fun decryptMessage(privateKey: PrivateKey, ciphertextBase64: String): String {
        val cipherBytes = Base64.getDecoder().decode(ciphertextBase64)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams())
        val plaintextBytes = cipher.doFinal(cipherBytes)
        return plaintextBytes.toString(Charsets.UTF_8)
    }
}