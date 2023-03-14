package sample.repository

import org.springframework.data.domain.*
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sample.domain.User
import java.util.Optional

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
interface UserRepository : JpaRepository<User, String> {

    fun findOneByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = ["authorities"])
    fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

    fun findAllByIdNotNullAndActivatedIsTrue(pageable: Pageable): Page<User>
}
