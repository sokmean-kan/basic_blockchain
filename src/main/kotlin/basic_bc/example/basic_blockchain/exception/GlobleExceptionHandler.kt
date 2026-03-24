package basic_bc.example.basic_blockchain.exception

import basic_bc.example.basic_blockchain.dto.response.ErrorResponse
import basic_bc.example.basic_blockchain.dto.response.FieldError
import basic_bc.example.basic_blockchain.dto.response.Status
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.HandlerMethodValidationException

@ControllerAdvice
class GlobalExceptionHandler {

    // Handles @Valid failures in @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(
                field = it.field,
                message = it.defaultMessage ?: "Invalid value"
            )
        }

        val response = ErrorResponse(
            status = Status(errorCode = 1, errorMessage = "Invalid values bruh!"),
            data = errors//.first()
        )
        return ResponseEntity.badRequest().body(response)
    }

    // Optional: for other validation exceptions
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.map {
            FieldError(
                field = it.propertyPath.toString(),
                message = it.message
            )
        }
        return ResponseEntity.badRequest().body(ErrorResponse(
            status = Status(errorCode = 2, errorMessage = "Invalid value"),
            data = errors
        ))
    }
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = Status(errorCode = 3, errorMessage = ex.message ?: "Ruk ot see"),
//            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(ErrorExceptionResponse::class)
    fun errorExceptionResponse(ex: ErrorExceptionResponse): ResponseEntity<Status> {
        val errorResponse = Status(errorCode = 3, errorMessage = ex.message ?: "Ruk ot see")
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    // Handles @RequestParam validation failures
    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidation(ex: HandlerMethodValidationException): ResponseEntity<ErrorResponse> {
        val errors = ex.parameterValidationResults.flatMap { result ->
            result.resolvableErrors.map { error ->
                FieldError(
                    field = result.methodParameter.parameterName ?: "unknown",
                    message = error.defaultMessage ?: "Invalid value"
                )
            }
        }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = Status(errorCode = 1, errorMessage = "Invalid values in form-data!"),
                data = errors
            )
        )
    }
    //Validate file
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = Status(errorCode = 1, errorMessage = "Invalid values in form-data!"),
                data = listOf(FieldError(
                    field = "file",
                    message = ex.message ?: "Invalid values file in form-data!"
                ))
            )
        )
    }
}