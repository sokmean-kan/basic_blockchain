package basic_bc.example.basic_blockchain.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "user_seq", sequenceName = "users_seq", allocationSize = 50, initialValue = 1)
    var id: Long = 0,
    @Column( nullable = false)
    var username: String = "" ,
    @Column(nullable = false)
    var publicKey: String = "",
)