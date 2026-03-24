package basic_bc.example.basic_blockchain.dto.response

data class EncryptFileResponse(
    val status: Status,
    val encryptedKey: String,
    val encryptedFileLink: String
)