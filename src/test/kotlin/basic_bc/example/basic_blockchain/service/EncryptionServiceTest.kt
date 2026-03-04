package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.dto.request.EncryptRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class EncryptionServiceTest {

    private val encryptionService: EncryptionService = mock(EncryptionService::class.java)

    @Test
    fun myFirstTest() {
        encryptionService.encrypt(EncryptRequest("monalisa", "hello world"))
    }
}