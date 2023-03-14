package sample.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A ChildEntity.
 */

@Entity
@Table(name = "child_entity")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class ChildEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "child_field")
    var childField: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @ManyToOne(optional = false)
    @NotNull
    var user: User? = null

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = [
            "children",
        ],
        allowSetters = true
    )
    var parent: ParentEntity? = null

    fun user(user: User?): ChildEntity {
        this.user = user
        return this
    }
    fun parent(parentEntity: ParentEntity?): ChildEntity {
        this.parent = parentEntity
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChildEntity) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "ChildEntity{" +
            "id=" + id +
            ", childField='" + childField + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
