package sample.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import sample.domain.ChildEntity
import java.util.Optional

/**
 * Spring Data JPA repository for the ChildEntity entity.
 */
@Repository
interface ChildEntityRepository : JpaRepository<ChildEntity, Long> {

    @Query("select childEntity from ChildEntity childEntity where childEntity.user.login = ?#{principal.preferredUsername}")
    fun findByUserIsCurrentUser(): MutableList<ChildEntity>

    @JvmDefault fun findOneWithEagerRelationships(id: Long): Optional<ChildEntity> {
        return this.findOneWithToOneRelationships(id)
    }

    @JvmDefault fun findAllWithEagerRelationships(): MutableList<ChildEntity> {
        return this.findAllWithToOneRelationships()
    }

    @JvmDefault fun findAllWithEagerRelationships(pageable: Pageable): Page<ChildEntity> {
        return this.findAllWithToOneRelationships(pageable)
    }

    @Query(
        value = "select distinct childEntity from ChildEntity childEntity left join fetch childEntity.user left join fetch childEntity.parent",
        countQuery = "select count(distinct childEntity) from ChildEntity childEntity"
    )
    fun findAllWithToOneRelationships(pageable: Pageable): Page<ChildEntity>

    @Query("select distinct childEntity from ChildEntity childEntity left join fetch childEntity.user left join fetch childEntity.parent")
    fun findAllWithToOneRelationships(): MutableList<ChildEntity>

    @Query("select childEntity from ChildEntity childEntity left join fetch childEntity.user left join fetch childEntity.parent where childEntity.id =:id")
    fun findOneWithToOneRelationships(@Param("id") id: Long): Optional<ChildEntity>
}
