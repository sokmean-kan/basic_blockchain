package basic_bc.example.basic_blockchain.exception

class ResourceNotFoundException(message: String? = null) : RuntimeException(message)

class ErrorExceptionResponse(message: String? = null) : RuntimeException(message)
