package basic_bc.example.basic_blockchain.controller.user

import basic_bc.example.basic_blockchain.dto.response.EncryptFileResponse
import basic_bc.example.basic_blockchain.service.EncryptFileService
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping(value = ["/api/file-encryption"])
class EncryptFileController(
    private val encryptService: EncryptFileService,
) {

    // Encrypt endpoint
    @PostMapping("/encrypt")
    fun encrypt(
        @RequestParam("username") @NotBlank(message = "Username is required") username: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<EncryptFileResponse> {
        val result = encryptService.encryptFile(username, file)
        val linkDownload = "http://localhost:8988/api/file-encryption/files/"
        return ResponseEntity.ok(
            EncryptFileResponse(
                status = result.status,
                encryptedKey = result.encryptedKey,
                encryptedFileLink = linkDownload + result.encryptedFileLink
            )
        )
    }

    // Download endpoint
    @GetMapping("/files/{fileName}")
    fun downloadFile(@PathVariable fileName: String): ResponseEntity<ByteArrayResource> {
        val filePath = Paths.get("encrypted-files", fileName)
        val fileBytes = Files.readAllBytes(filePath)
        val resource = ByteArrayResource(fileBytes)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$fileName")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    @PostMapping("/decrypt")
    fun decrypt(
        @RequestParam("private_key") privateKey: String,
        @RequestParam("encrypted_key") encryptedKey: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ByteArrayResource> {
        val decryptedBytes = encryptService.decryptFile(privateKey, encryptedKey, file)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=decrypted_${file.originalFilename?.replace(".enc", "")}"
            )
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(ByteArrayResource(decryptedBytes))
    }

}