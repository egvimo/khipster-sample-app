package sample.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

/**
 * Base abstract class for entities which will hold definitions for created, last modified by, created by,
 * last modified by attributes.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(value = [ "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate" ], allowGetters = true)
abstract class AbstractAuditingEntity<T>(
    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    open var createdBy: String? = null,

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    open var createdDate: Instant? = Instant.now(),

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    open var lastModifiedBy: String? = null,

    @LastModifiedDate
    @Column(name = "last_modified_date")
    open var lastModifiedDate: Instant? = Instant.now()
) : Serializable {

    abstract val id: T?

    companion object {
        private const val serialVersionUID = 1L
    }
}
