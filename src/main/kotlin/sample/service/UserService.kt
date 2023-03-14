package sample.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sample.config.DEFAULT_LANGUAGE
import sample.domain.Authority
import sample.domain.User
import sample.repository.AuthorityRepository
import sample.repository.UserRepository
import sample.security.getCurrentUserLogin
import sample.service.dto.AdminUserDTO
import sample.service.dto.UserDTO
import java.time.Instant
import java.util.Optional

/**
 * Service class for managing users.
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val authorityRepository: AuthorityRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    fun updateUser(firstName: String?, lastName: String?, email: String?, langKey: String?, imageUrl: String?) {
        getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent {

                it.firstName = firstName
                it.lastName = lastName
                it.email = email?.toLowerCase()
                it.langKey = langKey
                it.imageUrl = imageUrl
                log.debug("Changed Information for User: $it")
            }
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Page<AdminUserDTO> {
        return userRepository.findAll(pageable).map { AdminUserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getAllPublicUsers(pageable: Pageable): Page<UserDTO> {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map { UserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Optional<User> =
        userRepository.findOneWithAuthoritiesByLogin(login)

    /**
     * @return a list of all the authorities
     */
    @Transactional(readOnly = true)
    fun getAuthorities() =
        authorityRepository.findAll().asSequence().map { it.name }.filterNotNullTo(mutableListOf())

    private fun syncUserWithIdP(details: Map<String, Any>, user: User): User {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        val dbAuthorities = getAuthorities()
        val userAuthorities = user.authorities.asSequence().mapTo(mutableListOf(), Authority::name)
        for (authority in userAuthorities) {
            if (!dbAuthorities.contains(authority)) {
                log.debug("Saving authority '$authority' in local database")
                val authorityToSave = Authority(name = authority)
                authorityRepository.save(authorityToSave)
            }
        }
        // save account in to sync users between IdP and JHipster's local database
        val existingUser = userRepository.findOneByLogin(user.login!!)
        if (existingUser.isPresent) {
            // if IdP sends last updated information, use it to determine if an update should happen
            if (details["updated_at"] != null) {
                val dbModifiedDate = existingUser.get().lastModifiedDate
                val idpModifiedDate: Instant = if (details["updated_at"] is Instant) details["updated_at"] as Instant else Instant.ofEpochSecond(details["updated_at"] as Long)
                if (idpModifiedDate.isAfter(dbModifiedDate)) {
                    log.debug("Updating user '${user.login}' in local database")
                    updateUser(user.firstName, user.lastName, user.email, user.langKey, user.imageUrl)
                }
                // no last updated info, blindly update
            } else {
                log.debug("Updating user '${user.login}' in local database")
                updateUser(user.firstName, user.lastName, user.email, user.langKey, user.imageUrl)
            }
        } else {
            log.debug("Saving user '${user.login}' in local database")
            userRepository.save(user)
        }
        return user
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    @Transactional
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): AdminUserDTO {
        val attributes: Map<String, Any> =
            when (authToken) {
                is OAuth2AuthenticationToken -> authToken.principal.attributes
                is JwtAuthenticationToken -> authToken.tokenAttributes
                else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!")
            }

        val user = getUser(attributes)
        user.authorities = authToken.authorities.asSequence()
            .map(GrantedAuthority::getAuthority)
            .map { Authority(name = it) }
            .toMutableSet()
        return AdminUserDTO(syncUserWithIdP(attributes, user))
    }

    companion object {

        @JvmStatic
        private fun getUser(details: Map<String, Any>): User {
            var activated = true
            val sub = details["sub"] as String
            val username = details["preferred_username"]?.let { it as String } ?: null
            val user = User()
            // handle resource server JWT, where sub claim is email and uid is ID
            if (details["uid"] != null) {
                user.id = details["uid"] as String
                user.login = sub
            } else {
                user.id = sub
            }
            if (username != null) {
                user.login = username.toLowerCase()
            } else if (user.login == null) {
                user.login = user.id
            }
            if (details["given_name"] != null) {
                user.firstName = details["given_name"] as String
            } else if (details.get("name") != null) {
                user.firstName = details.get("name") as String
            }
            if (details["family_name"] != null) {
                user.lastName = details["family_name"] as String
            }
            if (details["email_verified"] != null) {
                activated = details["email_verified"] as Boolean
            }
            if (details["email"] != null) {
                user.email = (details["email"] as String).toLowerCase()
            } else if (sub.contains("|") && (username != null && username.contains("@"))) {
                // special handling for Auth0
                user.email = username
            } else {
                user.email = sub
            }
            if (details["langKey"] != null) {
                user.langKey = details["langKey"] as String
            } else if (details["locale"] != null) {
                // trim off country code if it exists
                var locale = details["locale"] as String
                if (locale.contains("_")) {
                    locale = locale.substring(0, locale.indexOf("_"))
                } else if (locale.contains("-")) {
                    locale = locale.substring(0, locale.indexOf("-"))
                }
                user.langKey = locale.toLowerCase()
            } else {
                // set langKey to default if not specified by IdP
                user.langKey = DEFAULT_LANGUAGE
            }
            if (details["picture"] != null) {
                user.imageUrl = details["picture"] as String
            }
            user.activated = activated
            return user
        }
    }
}
