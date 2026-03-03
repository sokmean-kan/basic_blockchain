package basic_bc.example.basic_blockchain.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class KeyGenerationRequest(
    @field:NotBlank(message = "Username cannot be blanks")
    @field:Size(max = 200, message = "Name is too long")
    val username: String = "",
)