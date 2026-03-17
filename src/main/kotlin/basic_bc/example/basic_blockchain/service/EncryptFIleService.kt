package basic_bc.example.basic_blockchain.service

import basic_bc.example.basic_blockchain.exception.ResourceNotFoundException
import basic_bc.example.basic_blockchain.repository.UserKeyRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import java.security.spec.MGF1ParameterSpec
import java.nio.file.Files
import java.nio.file.Paths
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptService(private val userKeyRepository: UserKeyRepository) {

    private val storageDir = "encrypted-files"

    init {
        Files.createDirectories(Paths.get(storageDir))
    }

    fun encryptFile(username: String, file: MultipartFile): Map<String, String> {
        val user = userKeyRepository.findByUsernameIgnoreCase(username = username)
            ?: throw ResourceNotFoundException("User not found: $username")
        // 1. Decode public key
        val publicKeyBytes = Base64.getDecoder().decode(user.publicKey)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))

        // 2. Generate random AES key
        val aesKey = KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()

        // 3. Generate random IV
        val iv = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val ivSpec = IvParameterSpec(iv)

        // 4. Encrypt file with AES
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)
        val encryptedFileBytes = aesCipher.doFinal(file.bytes)

        // 5. Encrypt AES key with RSA public key
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaepParams = OAEPParameterSpec(
            "SHA-256", "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
        val encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)

        // 6. Save encrypted file to disk
        val fileName = "${UUID.randomUUID()}.enc"
        val filePath = Paths.get(storageDir, fileName)

        // Write IV + encrypted file bytes together
        val outputBytes = iv + encryptedFileBytes
        Files.write(filePath, outputBytes)

        // 7. Build response
        val encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedAesKey)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)  // ← convert IV to Base64 first

        return mapOf(
            "encryptedKey" to "$encryptedKeyBase64.$ivBase64",  // ← join with dot
            "encryptedFile_link" to "http://localhost:8988/api/file-encryption/files/$fileName"
        )
    }
    fun decryptFile(privateKeyBase64: String, encryptedKeyWithIv: String, encFile: MultipartFile): ByteArray {
        // 1. Decode private key
        val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)
        val privateKey = KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

        // 2. Split encryptedKey and IV
        val parts = encryptedKeyWithIv.split(".")
        val encryptedAesKey = Base64.getDecoder().decode(parts[0])
        val iv = Base64.getDecoder().decode(parts[1])

        // 3. Decrypt AES key with RSA private key
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaepParams = OAEPParameterSpec(
            "SHA-256", "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams)
        val aesKeyBytes = rsaCipher.doFinal(encryptedAesKey)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // 4. Read encrypted file bytes — SKIP first 16 bytes (IV)
        val allBytes = encFile.bytes
        val encryptedFileBytes = allBytes.copyOfRange(16, allBytes.size)

        // 5. Decrypt file with AES
        val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))

        return aesCipher.doFinal(encryptedFileBytes)
    }
}