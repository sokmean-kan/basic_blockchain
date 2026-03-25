package basic_bc.example.basic_blockchain.controller.user

import basic_bc.example.basic_blockchain.dto.request.DecryptRequest1
import basic_bc.example.basic_blockchain.dto.request.DecryptResponse1
import basic_bc.example.basic_blockchain.dto.request.EncryptRequest1
import basic_bc.example.basic_blockchain.dto.request.EncryptResponse1
import basic_bc.example.basic_blockchain.service.CryptoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/api/crypto")
class CryptoController(
    private val cryptoService: CryptoService
) {

    @PostMapping("/rsa/encrypt")
    fun rsaEncrypt(@RequestBody request: EncryptRequest1): ResponseEntity<EncryptResponse1> {
        val encrypted = cryptoService.rsaEncrypt(
            request.data,
            request.username
        )
        return ResponseEntity.ok(EncryptResponse1(encryptedData = encrypted))
    }

    @PostMapping("/rsa/decrypt")
    fun rsaDecrypt(@RequestBody request: DecryptRequest1): ResponseEntity<DecryptResponse1> {
        val decrypted = cryptoService.rsaDecrypt(
            request.encryptedData,
            request.privateKey
        )
        return ResponseEntity.ok(DecryptResponse1(plainText = decrypted))
    }

    @PostMapping("/hybrid/encrypt")
    fun hybridEncrypt(@RequestBody request: EncryptRequest1): ResponseEntity<EncryptResponse1> {
        val encrypted = cryptoService.hybridEncrypt(
            request.data,
            request.username
        )
        return ResponseEntity.ok(EncryptResponse1(encryptedData = encrypted))
    }

    @PostMapping("/hybrid/decrypt")
    fun hybridDecrypt(@RequestBody request: DecryptRequest1): ResponseEntity<DecryptResponse1> {
        val decrypted = cryptoService.hybridDecrypt(
            request.encryptedData,
            request.privateKey
        )
        return ResponseEntity.ok(DecryptResponse1(plainText = decrypted))
    }
}