package basic_bc.example.basic_blockchain.dto.request


data class ValidateKeyRequest(
        val username: String,
        val data: String,
        val privateKey: String
)
