package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.DecryptRequest
import basic_bc.example.basic_blockchain.dto.request.EncryptRequest
import basic_bc.example.basic_blockchain.dto.response.DecryptResponse
import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@Service
class EncryptionService(
    private val userRepository: UserKeyRepository

) {

    fun encrypt(request: EncryptRequest):String {
        val user = userRepository.findByUsernameIgnoreCase(request.username)
            ?: throw ResourceNotFoundException("User not found: ${request.username}")

        val publicKeyBytes = Base64.getDecoder().decode(user.publicKey)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(request.data.toByteArray())
//        val result =
        return Base64.getEncoder().encodeToString(encryptedBytes)
//        return ResponseEntity.ok(EncryptResponse(
//            username = request.username,
//            encryptedData = result
//        ))
    }

    fun decrypt(request: DecryptRequest): ResponseEntity<DecryptResponse> {
        val user = userRepository.findByUsernameIgnoreCase(request.username)
            ?: throw ResourceNotFoundException("User not found: ${request.username}")
        val publicKey =  getPublicKeyFromBase64(user.publicKey)
        val privateKey = getPrivateKeyFromBase64(request.privateKey)
        val isValid = verifyPrivateKeyForUser(privateKey, publicKey)
        if (!isValid) throw ResourceNotFoundException("Private key mismatch with user's public key")

        try {
            val privateKeyBytes = Base64.getDecoder().decode(request.privateKey)
            val privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(request.encryptedData))
            val result =  String(decryptedBytes)
            return ResponseEntity.ok(DecryptResponse(user.username,result))

        }catch (e : Exception){
            throw ResourceNotFoundException(e.message)
        }
    }
    private fun getPublicKeyFromBase64(base64PublicKey: String): PublicKey {
        val bytes = Base64.getDecoder().decode(base64PublicKey)
        val spec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }
//
    private fun getPrivateKeyFromBase64(base64PrivateKey: String): PrivateKey {
        try {
            val bytes = Base64.getDecoder().decode(base64PrivateKey)
            val spec = PKCS8EncodedKeySpec(bytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePrivate(spec)
        } catch (e: Exception) {
            throw ResourceNotFoundException("Private key invalid format")
        }
    }
    private fun verifyPrivateKeyForUser(privateKey: PrivateKey, publicKey: PublicKey): Boolean {
        val testMessage = "test".toByteArray()

        // Sign with private key
        val signedBytes = try {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(testMessage)
            signature.sign()
        } catch (e: Exception) {
            throw ResourceNotFoundException("Cannot sign with the provided private key (invalid or corrupted)")
        }
        // Verify with public key
        val verifySignature = Signature.getInstance("SHA256withRSA")
        verifySignature.initVerify(publicKey)
        verifySignature.update(testMessage)
        return verifySignature.verify(signedBytes)
    }

    //////////////Hybrid AES+RSA
//    fun decrypt(request: DecryptRequest): ResponseEntity<DecryptResponse> {
//        val user = userRepository.findByUsernameIgnoreCase(request.username)
//            ?: throw ResourceNotFoundException("User not found: ${request.username}")
//
//        val publicKey = getPublicKeyFromBase64(user.publicKey)
//        val privateKey = getPrivateKeyFromBase64(request.privateKey)
//
//        val isValid = verifyPrivateKeyForUser(privateKey, publicKey)
//        if (!isValid) throw ResourceNotFoundException("Private key mismatch with user's public key")
//
//        try {
//            val result = if (request.encryptedData.contains(".")) {
//                // ✅ Hybrid AES+RSA decrypt
//                hybridDecrypt(request.encryptedData, privateKey)
//            } else {
//                // ✅ Pure RSA decrypt (short messages)
//                rsaDecrypt(request.encryptedData, privateKey)
//            }
//            return ResponseEntity.ok(DecryptResponse(user.username, result))
//
//        } catch (e: Exception) {
//            throw ResourceNotFoundException(e.message)
//        }
//    }
//
//    // ─── Pure RSA Decrypt ─────────────────────────────────────
//    private fun rsaDecrypt(encryptedData: String, privateKey: PrivateKey): String {
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
//        return String(decryptedBytes)
//    }

    // ─── Hybrid AES+RSA Decrypt ───────────────────────────────
//    private fun hybridDecrypt(encryptedData: String, privateKey: PrivateKey): String {
//        // Split: encryptedAesKey.iv.encryptedData
//        val parts = encryptedData.split(".")
//        if (parts.size != 3) throw IllegalArgumentException("Invalid hybrid encrypted format!")
//
//        val encryptedAesKey = Base64.getDecoder().decode(parts[0])
//        val iv              = Base64.getDecoder().decode(parts[1])
//        val encryptedBytes  = Base64.getDecoder().decode(parts[2])
//
//        // Step 1 — Decrypt AES key with RSA private key
//        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val aesKey = rsaCipher.doFinal(encryptedAesKey)
//
//        // Step 2 — Decrypt data with AES key
//        val secretKey = javax.crypto.spec.SecretKeySpec(aesKey, "AES")
//        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        aesCipher.init(Cipher.DECRYPT_MODE, secretKey, javax.crypto.spec.IvParameterSpec(iv))
//        val decryptedBytes = aesCipher.doFinal(encryptedBytes)
//
//        return String(decryptedBytes)
//    }

}