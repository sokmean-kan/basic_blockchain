package basic_bc.example.basic_blockchain.mapper

import basic_bc.example.basic_blockchain.dto.request.KeyGenerationRequest
import basic_bc.example.basic_blockchain.model.User

fun KeyGenerationRequest.toUser(publicKey: String): User {
    return User(
        username = username,
        publicKey = publicKey,
    )
}