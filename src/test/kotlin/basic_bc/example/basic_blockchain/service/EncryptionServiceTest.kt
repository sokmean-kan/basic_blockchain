package basic_bc.example.basic_blockchain.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class EncryptionServiceTest {

    private val encryptionService: EncryptionService = mock(EncryptionService::class.java)

    @Test
    fun myFirstTest() {
        encryptionService.encrypt("monalisa", "hello world")
    }
}