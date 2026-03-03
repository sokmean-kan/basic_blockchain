package basic_bc.example.basic_blockchain.dto.response

data class ErrorResponse(
    val status: Status,
    val data: List<FieldError> = emptyList(),
)

data class FieldError(
    val field: String,
    val message: String
)
data class Status(
    val errorCode: Int?= null,
    val errorMessage: String? = null
)


