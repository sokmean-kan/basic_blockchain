package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
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

    fun encrypt(username: String, data: String): String {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found: $username")

        val publicKeyBytes = Base64.getDecoder().decode(user.publicKey)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(username: String, privateKeyStr: String, encryptedData: String): String {
        userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found: $username")

        try {
            val privateKeyBytes = Base64.getDecoder().decode(privateKeyStr)
            val privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
            return String(decryptedBytes)

        }catch (e : Exception){
            throw ResourceNotFoundException(e.message)
        }
    }
}