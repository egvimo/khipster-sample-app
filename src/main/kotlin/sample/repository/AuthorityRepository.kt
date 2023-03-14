package sample.repository

import org.springframework.data.jpa.repository.JpaRepository
import sample.domain.Authority

/**
 * Spring Data JPA repository for the [Authority] entity.
 */

interface AuthorityRepository : JpaRepository<Authority, String>
