package basic_bc.example.basic_blockchain.dto.response

data class SignatureResponse(
    val message: String,           // the challenge that was signed
    val signature: String,         // Base64-encoded signature
)