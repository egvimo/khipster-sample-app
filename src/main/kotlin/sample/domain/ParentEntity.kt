package sample.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A ParentEntity.
 */

@Entity
@Table(name = "parent_entity")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class ParentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @get: NotNull

    @Column(name = "parent_required_field", nullable = false)
    var parentRequiredField: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @OneToMany(mappedBy = "parent")
    @JsonIgnoreProperties(
        value = [
            "user",
            "parent",
        ],
        allowSetters = true
    )
    var children: MutableSet<ChildEntity>? = mutableSetOf()

    fun addChild(childEntity: ChildEntity): ParentEntity {
        this.children?.add(childEntity)
        childEntity.parent = this
        return this
    }
    fun removeChild(childEntity: ChildEntity): ParentEntity {
        this.children?.remove(childEntity)
        childEntity.parent = null
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParentEntity) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "ParentEntity{" +
            "id=" + id +
            ", parentRequiredField='" + parentRequiredField + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
