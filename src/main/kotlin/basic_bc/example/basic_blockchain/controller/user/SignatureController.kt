package basic_bc.example.basic_blockchain.controller.user

import basic_bc.example.basic_blockchain.dto.request.VerifySignatureRequest
import basic_bc.example.basic_blockchain.dto.response.VerifiedSignatureResponse
import basic_bc.example.basic_blockchain.service.SignatureVerificationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/signature")
class SignatureController(
    private val verificationService: SignatureVerificationService
) {
    @PostMapping("/verify")
    fun verifySignature(
        @RequestBody request: VerifySignatureRequest
    ): VerifiedSignatureResponse {
        return verificationService.verifyAndGetUsername(request)
    }
}