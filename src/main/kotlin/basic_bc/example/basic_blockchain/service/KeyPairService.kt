package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.KeyGenerationRequest
import basic_bc.example.basic_blockchain.dto.response.KeyPairResponse
import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.mapper.toUser
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.KeyPairGenerator
import java.util.*

@Service
class KeyPairService(
    private val userKeyRepository: UserKeyRepository
) {

    @Transactional
    fun generateAndStoreKeyPair(userRequest: KeyGenerationRequest): KeyPairResponse {
        if (userKeyRepository.existsByUsername(userRequest.username)) {
            throw ResourceNotFoundException("Username already exists")
        }

        // Generate RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        // Encode keys to Base64
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded) //encoded in X.509
        val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded) //encoded in PKCS#8

        val user = userRequest.toUser(publicKey)
        userKeyRepository.save(user)

        return KeyPairResponse(
            username = user.username,
            publicKey = publicKey,
            privateKey = privateKey
        )
    }
}