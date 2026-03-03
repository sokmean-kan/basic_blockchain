package basic_bc.example.basic_blockchain.controller.user

import basic_bc.example.basic_blockchain.dto.request.KeyGenerationRequest
import basic_bc.example.basic_blockchain.dto.request.ValidateKeyRequest
import basic_bc.example.basic_blockchain.dto.response.KeyPairResponse
import basic_bc.example.basic_blockchain.dto.response.SignatureResponse
import basic_bc.example.basic_blockchain.service.KeyPairService
import basic_bc.example.basic_blockchain.service.KeyValidationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/keys")
class KeyPairController(
    private val keyPairService: KeyPairService,
    private val keyValidationService: KeyValidationService
) {

    @PostMapping("/generate")
    fun generateKeyPair(@RequestBody @Valid request: KeyGenerationRequest): ResponseEntity<KeyPairResponse> {
        val result = keyPairService.generateAndStoreKeyPair(request)
        return ResponseEntity.ok(KeyPairResponse(
            username = result.username,
            publicKey = result.publicKey,
            privateKey = result.privateKey
        ))
    }

    @PostMapping("/validate")
    fun validatePrivateKey(
        @RequestBody @Valid request: ValidateKeyRequest
    ): ResponseEntity<SignatureResponse> {
        return try {
            val result = keyValidationService.validateAndSign(request)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .badRequest()
                .body(SignatureResponse(
                    message = "",
                    signature = "",
                ))
        }
    }
}

