package basic_bc.example.basic_blockchain.dto.request


// EncryptRequest.kt
data class EncryptRequest1(
    val data: String,
    val username: String  // Base64 encoded public key
)

// EncryptResponse.kt
data class EncryptResponse1(
    val encryptedData: String
)

// DecryptRequest.kt
data class DecryptRequest1(
    val encryptedData: String,
    val privateKey: String  // Base64 encoded private key
)

// DecryptResponse.kt
data class DecryptResponse1(
    val plainText: String
)