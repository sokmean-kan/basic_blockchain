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
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@Service
class EncryptionService(
    private val userRepository: UserKeyRepository
) {

    fun encrypt(request: EncryptRequest): ResponseEntity<EncryptResponse> {
        val user = userRepository.findByUsername(request.username)
            ?: throw ResourceNotFoundException("User not found: ${request.username}")

        val publicKeyBytes = Base64.getDecoder().decode(user.publicKey)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(request.data.toByteArray())
        val result = Base64.getEncoder().encodeToString(encryptedBytes)
        return ResponseEntity.ok(EncryptResponse(
            username = request.username,
            encryptedData = result
        ))
    }

    fun decrypt(request: DecryptRequest): ResponseEntity<DecryptResponse> {
        userRepository.findByUsername(request.username)
            ?: throw ResourceNotFoundException("User not found: ${request.username}")
        try {
            val privateKeyBytes = Base64.getDecoder().decode(request.privateKey)
            val privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(request.encryptedData))
            val result =  String(decryptedBytes)
            return ResponseEntity.ok(DecryptResponse(request.username,result))

        }catch (e : Exception){
            throw ResourceNotFoundException(e.message)
        }
    }
}