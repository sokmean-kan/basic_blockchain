package basic_bc.example.basic_blockchain.dto.request

import jakarta.validation.constraints.NotBlank

data class DecryptRequest(
    @field:NotBlank
    val username: String,
    val privateKey: String,
    val encryptedData: String
)