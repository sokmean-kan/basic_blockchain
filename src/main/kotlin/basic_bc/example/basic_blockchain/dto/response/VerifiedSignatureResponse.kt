package basic_bc.example.basic_blockchain.dto.response

data class VerifiedSignatureResponse(
    val verify: Boolean,
    val data: String
)