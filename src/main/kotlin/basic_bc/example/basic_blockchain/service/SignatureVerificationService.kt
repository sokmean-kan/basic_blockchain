package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.VerifySignatureRequest
import basic_bc.example.basic_blockchain.dto.response.VerifiedSignatureResponse
import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.Signature
import java.security.SignatureException
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Service
class SignatureVerificationService(
    private val userRepository: UserKeyRepository
) {

    fun verifyAndGetUsername(request: VerifySignatureRequest): VerifiedSignatureResponse {

        val user = userRepository.findByUsername(request.username)
            ?: throw ResourceNotFoundException("User not found: ${request.username}")

        val storedPublicKeyBase64 = user.publicKey

        val publicKeyBytes = Base64.getDecoder().decode(storedPublicKeyBase64)
        val kf = KeyFactory.getInstance("RSA")
        val publicKey = kf.generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val signatureBytes = try {
            Base64.getDecoder().decode(request.signature)
        } catch (e: Exception) {
            throw ResourceNotFoundException("Invalid signature format (not valid Base64)")
        }

//         Verify signature
        val verifier = Signature.getInstance("SHA256withRSA")
        try {
            verifier.initVerify(publicKey)
            verifier.update(request.data.toByteArray(Charsets.UTF_8)) //verify with data that was sign
            val isValid = verifier.verify(signatureBytes)

            if (!isValid) {
                throw ResourceNotFoundException("Signature verification failed - does not match the public key or data")
            }

        } catch (e: SignatureException) {
            throw ResourceNotFoundException("Signature verification failed: invalid signature")
        } catch (e: Exception) {
            throw ResourceNotFoundException("Verification process failed: ${e.message}")
        }

        return VerifiedSignatureResponse(
            username = request.username,
            data = request.data
        )
    }
}