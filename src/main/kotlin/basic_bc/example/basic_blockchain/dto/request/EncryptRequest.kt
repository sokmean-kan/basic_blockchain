package basic_bc.example.basic_blockchain.dto.request

import jakarta.validation.constraints.NotBlank

data class EncryptRequest(
    @field:NotBlank("username is required")
    val username: String,
    @field:NotBlank("data is required")
    val data: String
)

