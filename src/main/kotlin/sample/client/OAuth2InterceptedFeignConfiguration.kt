package sample.client

import org.springframework.context.annotation.Bean
import sample.security.oauth2.AuthorizationHeaderUtil

class OAuth2InterceptedFeignConfiguration {

    @Bean(name = ["oauth2RequestInterceptor"])
    fun getOAuth2RequestInterceptor(authorizationHeaderUtil: AuthorizationHeaderUtil) =
        TokenRelayRequestInterceptor(authorizationHeaderUtil)
}
