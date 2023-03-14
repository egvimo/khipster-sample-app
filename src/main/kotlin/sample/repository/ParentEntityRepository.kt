package sample.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sample.domain.ParentEntity

/**
 * Spring Data JPA repository for the ParentEntity entity.
 */
@Suppress("unused")
@Repository
interface ParentEntityRepository : JpaRepository<ParentEntity, Long>
