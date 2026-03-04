package basic_bc.example.basic_blockchain.controller.user

import basic_bc.example.basic_blockchain.dto.request.DecryptRequest
import basic_bc.example.basic_blockchain.dto.request.EncryptRequest
import basic_bc.example.basic_blockchain.dto.response.DecryptResponse
import basic_bc.example.basic_blockchain.dto.response.EncryptResponse
import basic_bc.example.basic_blockchain.service.EncryptionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/encryption")
class EncryptionController(private val encryptionService: EncryptionService) {

    @PostMapping("/encrypt")
    fun encrypt(@Valid @RequestBody request: EncryptRequest): ResponseEntity<EncryptResponse> {
        val encryptedData = encryptionService.encrypt(request)
        return ResponseEntity.ok(EncryptResponse(request.username, encryptedData))
    }

    @PostMapping("/decrypt")
    fun decrypt(@Valid @RequestBody request: DecryptRequest) = encryptionService.decrypt(request) //: ResponseEntity<DecryptResponse> {
//        val decryptedData = encryptionService.decrypt(request)
//        return ResponseEntity.ok(DecryptResponse(request.username, decryptedData))
//    }
}