package sample.security.oauth2

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import sample.security.extractAuthorityFromClaims

@Component
class JwtGrantedAuthorityConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt) = extractAuthorityFromClaims(jwt.claims)
}
