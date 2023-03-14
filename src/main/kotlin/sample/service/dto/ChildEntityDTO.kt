package sample.service.dto

import java.io.Serializable
import java.util.Objects
import javax.validation.constraints.*

/**
 * A DTO for the [sample.domain.ChildEntity] entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
data class ChildEntityDTO(

    var id: Long? = null,

    var childField: String? = null,

    var user: UserDTO? = null,

    var parent: ParentEntityDTO? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChildEntityDTO) return false
        val childEntityDTO = other
        if (this.id == null) {
            return false
        }
        return Objects.equals(this.id, childEntityDTO.id)
    }

    override fun hashCode() = Objects.hash(this.id)
}
