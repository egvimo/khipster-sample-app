@file:JvmName("SecurityUtils")

package sample.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

const val CLAIMS_NAMESPACE = "https://www.jhipster.tech/"

/**
 * Get the login of the current user.
 *
 * @return the login of the current user.
 */
fun getCurrentUserLogin(): Optional<String> =
    Optional.ofNullable(extractPrincipal(SecurityContextHolder.getContext().authentication))

fun extractPrincipal(authentication: Authentication?): String? {

    if (authentication == null) {
        return null
    }

    return when (val principal = authentication.principal) {
        is UserDetails -> principal.username
        is JwtAuthenticationToken -> (authentication as JwtAuthenticationToken).token.claims as String
        is DefaultOidcUser -> {
            if (principal.attributes.containsKey("preferred_username")) {
                principal.attributes["preferred_username"].toString()
            } else {
                null
            }
        }
        is String -> principal
        else -> null
    }
}

/**
 * Check if a user is authenticated.
 *
 * @return true if the user is authenticated, false otherwise.
 */
fun isAuthenticated(): Boolean {
    val authentication = SecurityContextHolder.getContext().authentication

    if (authentication != null) {
        val isAnonymousUser = getAuthorities(authentication)?.none { it == ANONYMOUS }
        if (isAnonymousUser != null) {
            return isAnonymousUser
        }
    }

    return false
}

/**
* Checks if the current user has any of the authorities.
*
* @param authorities the authorities to check.
* @return true if the current user has any of the authorities, false otherwise.
*/
fun hasCurrentUserAnyOfAuthorities(vararg authorities: String): Boolean {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication != null && getAuthorities(authentication)?.any { authorities.contains(it) } ?: false
}

/**
* Checks if the current user has none of the authorities.
*
* @param authorities the authorities to check.
* @return true if the current user has none of the authorities, false otherwise.
*/
fun hasCurrentUserNoneOfAuthorities(vararg authorities: String): Boolean {
    return !hasCurrentUserAnyOfAuthorities(*authorities)
}

/**
* Checks if the current user has a specific authority.
*
* @param authority the authority to check.
* @return true if the current user has the authority, false otherwise.
*/
fun hasCurrentUserThisAuthority(authority: String): Boolean {
    return hasCurrentUserAnyOfAuthorities(authority)
}

fun getAuthorities(authentication: Authentication): List<String>? {
    val authorities = when (authentication) {
        is JwtAuthenticationToken ->
            extractAuthorityFromClaims(authentication.token.claims)
        else ->
            authentication.authorities
    }
    return authorities?.map(GrantedAuthority::getAuthority)
}

fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority>? {
    return mapRolesToGrantedAuthorities(getRolesFromClaims(claims))
}

@Suppress("UNCHECKED_CAST")
fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
    return when (val test = claims.getOrDefault("groups", claims.getOrDefault("roles", claims.getOrDefault("${CLAIMS_NAMESPACE}roles", listOf<String>())))) {
        is String -> listOf(test)
        else -> test as Collection<String>
    }
}

fun mapRolesToGrantedAuthorities(roles: Collection<String>): List<GrantedAuthority> {
    return roles
        .filter { it.startsWith("ROLE_") }
        .map { SimpleGrantedAuthority(it) }
}
