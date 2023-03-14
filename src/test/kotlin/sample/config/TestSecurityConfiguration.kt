package sample.config

import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.jwt.JwtDecoder

/**
 * This class allows you to run unit and integration tests without an IdP.
 */
@TestConfiguration
class TestSecurityConfiguration {
    @Bean
    fun clientRegistration(): ClientRegistration {
        return clientRegistrationBuilder().build()
    }

    @Bean
    fun clientRegistrationRepository(clientRegistration: ClientRegistration): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(clientRegistration)
    }

    private fun clientRegistrationBuilder(): ClientRegistration.Builder {
        val metadata = mutableMapOf<String, Any>()
        metadata["end_session_endpoint"] = "https://jhipster.org/logout"

        return ClientRegistration.withRegistrationId("oidc")
            .issuerUri("{baseUrl}")
            .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("read:user")
            .authorizationUri("https://jhipster.org/login/oauth/authorize")
            .tokenUri("https://jhipster.org/login/oauth/access_token")
            .jwkSetUri("https://jhipster.org/oauth/jwk")
            .userInfoUri("https://api.jhipster.org/user")
            .providerConfigurationMetadata(metadata)
            .userNameAttributeName("id")
            .clientName("Client Name")
            .clientId("client-id")
            .clientSecret("client-secret")
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return mock(JwtDecoder::class.java)
    }

    @Bean
    fun authorizedClientService(clientRegistrationRepository: ClientRegistrationRepository): OAuth2AuthorizedClientService {
        return InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
    }
}
