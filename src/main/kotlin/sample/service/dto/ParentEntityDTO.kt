package sample.service.dto

import java.io.Serializable
import java.util.Objects
import javax.validation.constraints.*

/**
 * A DTO for the [sample.domain.ParentEntity] entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
data class ParentEntityDTO(

    var id: Long? = null,

    @get: NotNull
    var parentRequiredField: String? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParentEntityDTO) return false
        val parentEntityDTO = other
        if (this.id == null) {
            return false
        }
        return Objects.equals(this.id, parentEntityDTO.id)
    }

    override fun hashCode() = Objects.hash(this.id)
}
