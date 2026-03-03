package basic_bc.example.basic_blockchain.dto.response

data class KeyPairResponse(
    val username: String,
    val publicKey: String,
    val privateKey: String
)