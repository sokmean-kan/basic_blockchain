package basic_bc.example.basic_blockchain.dto.request

data class VerifySignatureRequest(
    val signature: String,   // Base64-encoded signature
    val data: String,        // the original message that was signed
    val username: String
)