package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.ValidateKeyRequest
import basic_bc.example.basic_blockchain.dto.response.SignatureResponse
import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.stereotype.Service
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class KeyValidationService(
    private val userRepository: UserKeyRepository
) {
    fun validateAndSign(request: ValidateKeyRequest): SignatureResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw ResourceNotFoundException()

        val publicKey =  getPublicKeyFromBase64(user.publicKey)
        val privateKey = getPrivateKeyFromBase64(request.privateKey)

        val isValid = verifyPrivateKeyForUser(privateKey, publicKey)
        if (!isValid) throw ResourceNotFoundException("Private key mismatch with the public key of the user")

        val signature = signData(privateKey, request.data)
        return SignatureResponse(
            message = "Successfully signed",
            signature = signature
        )
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

    private fun signData(privateKey: PrivateKey, data: String): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray())
        val signedBytes = signature.sign()
        return Base64.getEncoder().encodeToString(signedBytes)
    }
}

//    fun validateAndSign(request: ValidateKeyRequest): SignatureResponse {
//
//        val user = userRepository.findByUsername(request.username)
//            ?: throw ResourceNotFoundException("User not found: ${request.username}")
//        val storedPublicKeyBase64 = user.publicKey
//
//        // Decode stored public key, Converts the Base64 string back to raw bytes
//        val publicKeyBytes = try {
//            Base64.getDecoder().decode(storedPublicKeyBase64)
//        } catch (e: Exception) {
//            throw ResourceNotFoundException("Stored public key is corrupted or invalid")
//        }
//        //Converts the raw bytes into a Java PublicKey object using X.509 format
//        val kf = KeyFactory.getInstance("RSA")
//        val publicKey = try {
//            kf.generatePublic(X509EncodedKeySpec(publicKeyBytes))
//        } catch (e: Exception) {
//            throw ResourceNotFoundException("Stored public key format is invalid")
//        }
//
//        //Decode client private key
//        val clientPrivateKeyBytes = try {
//            Base64.getDecoder().decode(request.privateKey)
//        } catch (e: Exception) {
//            throw ResourceNotFoundException("Invalid private key format (not valid Base64 or PKCS#8)")
//        }
//
//        val clientPrivateKey = try {
//            kf.generatePrivate(PKCS8EncodedKeySpec(clientPrivateKeyBytes))
//        } catch (e: InvalidKeySpecException) {
//            throw ResourceNotFoundException("The provided key is not a valid RSA private key")
//        }
//
//        // Test ownership
//        val testMessage = "ownership-check-${Instant.now().epochSecond}".toByteArray(Charsets.UTF_8)
//
//        val testSigBytes = try {
//            val sig = Signature.getInstance("SHA256withRSA")
//            sig.initSign(clientPrivateKey)
//            sig.update(testMessage)
//            sig.sign()
//        } catch (e: Exception) {
//            throw ResourceNotFoundException("Cannot sign with the provided private key (invalid or corrupted)")
//        }
//
//        val isMatching = try {
//            val verifier = Signature.getInstance("SHA256withRSA")
//            verifier.initVerify(publicKey)
//            verifier.update(testMessage)
//            verifier.verify(testSigBytes)
//        } catch (e: Exception) {
//            false
//        }
//
//        if (!isMatching) {
//            throw ResourceNotFoundException("The provided private key does not match the public key of this user")
//        }
//
//        // Sign real challenge (only if we reach here)
//        val realSigBytes = try {
//            val sig = Signature.getInstance("SHA256withRSA")
//            sig.initSign(clientPrivateKey)
//            sig.update(request.data.toByteArray(Charsets.UTF_8))
//            sig.sign()
//        } catch (e: Exception) {
//            throw ResourceNotFoundException("Failed to generate signature after validation")
//        }
//
//        return SignatureResponse(
//            message = "Successfully signed with the user",
//            signature = Base64.getEncoder().encodeToString(realSigBytes),
//        )
//    }
//}
