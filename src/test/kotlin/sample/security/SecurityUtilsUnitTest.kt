package sample.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import java.time.Instant

/**
 * Test class for the Security Utility methods.
 */
class SecurityUtilsUnitTest {
    @BeforeEach
    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun testgetCurrentUserLogin() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val login = getCurrentUserLogin()
        assertThat(login).contains("admin")
    }

    @Test
    fun testGetCurrentUserLoginForOAuth2() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val claims = mapOf(
            "groups" to USER,
            "sub" to 123,
            "preferred_username" to "admin"
        )
        val idToken = OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims)
        val authorities = listOf(SimpleGrantedAuthority(USER))
        val user = DefaultOidcUser(authorities, idToken)
        val auth2AuthenticationToken = OAuth2AuthenticationToken(user, authorities, "oidc")
        securityContext.authentication = auth2AuthenticationToken
        SecurityContextHolder.setContext(securityContext)

        val login = getCurrentUserLogin()

        assertThat(login).contains("admin")
    }

    @Test
    fun testExtractAuthorityFromClaims() {
        val claims = mapOf(
            "groups" to listOf(ADMIN, USER)
        )
        val expectedAuthorities = listOf(SimpleGrantedAuthority(ADMIN), SimpleGrantedAuthority(USER))

        val authorities = extractAuthorityFromClaims(claims)

        assertThat(authorities).isNotNull().isNotEmpty().hasSize(2)
            .containsAll(expectedAuthorities)
    }

    @Test
    fun testExtractAuthorityFromClaims_NamespacedRoles() {
        val claims = mapOf(
            CLAIMS_NAMESPACE + "roles" to listOf(ADMIN, USER)
        )
        val expectedAuthorities = listOf(SimpleGrantedAuthority(ADMIN), SimpleGrantedAuthority(USER))

        val authorities = extractAuthorityFromClaims(claims)

        assertThat(authorities).isNotNull().isNotEmpty().hasSize(2)
            .containsAll(expectedAuthorities)
    }

    @Test
    fun testIsAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = isAuthenticated()
        assertThat(isAuthenticated).isTrue
    }

    @Test
    fun testAnonymousIsNotAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(ANONYMOUS))
        securityContext.authentication = UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities)
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = isAuthenticated()
        assertThat(isAuthenticated).isFalse
    }

    @Test
    fun testHasCurrentUserThisAuthority() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(USER))
        securityContext.authentication = UsernamePasswordAuthenticationToken("user", "user", authorities)
        SecurityContextHolder.setContext(securityContext)

        assertThat(hasCurrentUserThisAuthority(USER)).isTrue
        assertThat(hasCurrentUserThisAuthority(ADMIN)).isFalse
    }

    @Test
    fun testHasCurrentUserAnyOfAuthorities() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(USER))
        securityContext.setAuthentication(UsernamePasswordAuthenticationToken("user", "user", authorities))
        SecurityContextHolder.setContext(securityContext)

        assertThat(hasCurrentUserAnyOfAuthorities(USER, ADMIN)).isTrue
        assertThat(hasCurrentUserAnyOfAuthorities(ANONYMOUS, ADMIN)).isFalse
    }

    @Test
    fun testHasCurrentUserNoneOfAuthorities() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(USER))
        securityContext.setAuthentication(UsernamePasswordAuthenticationToken("user", "user", authorities))
        SecurityContextHolder.setContext(securityContext)

        assertThat(hasCurrentUserNoneOfAuthorities(USER, ADMIN)).isFalse
        assertThat(hasCurrentUserNoneOfAuthorities(ANONYMOUS, ADMIN)).isTrue
    }
}
