package sample.service.dto

import sample.domain.User
import java.io.Serializable
/**
 * A DTO representing a user, with only the public attributes.
 */
open class UserDTO(
    var id: String? = null,
    var login: String? = null,
) : Serializable {

    constructor(user: User) : this(user.id, user.login)

    override fun toString() = "UserDTO{" +
        "login='" + login + '\'' +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
