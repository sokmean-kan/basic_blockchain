package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.DecryptRequest
import basic_bc.example.basic_blockchain.dto.request.EncryptRequest
import basic_bc.example.basic_blockchain.dto.response.DecryptResponse
import basic_bc.example.basic_blockchain.dto.response.EncryptResponse
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
}