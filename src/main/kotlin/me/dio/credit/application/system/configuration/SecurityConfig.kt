package me.dio.credit.application.system.configuration

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
  private val jwtTokenProvider: JwtTokenProvider,
  private val userDetailsService: UserDetailsService
) {

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
    authConfig.authenticationManager

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .csrf().disable()
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/**"
          ).permitAll()
          .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
          .anyRequest().authenticated()
      }
      .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter::class.java)
      .httpBasic(Customizer.withDefaults())

    return http.build()
  }
}

/**
 * Simple JWT provider using HS256. Adjust secret and expiration via environment or properties in production.
 */
@Component
class JwtTokenProvider(
  // In production, inject via configuration properties
  private val secret: String = "replace_this_with_a_strong_secret_key_change_in_production",
  private val validityInMilliseconds: Long = 3600_000 // 1 hour
) {

  private val key: SecretKey
    get() {
      val bytes = Base64.getEncoder().encode(secret.toByteArray(Charsets.UTF_8))
      return SecretKeySpec(bytes, SignatureAlgorithm.HS256.jcaName)
    }

  fun createToken(username: String, roles: List<String>): String {
    val claims: Claims = Jwts.claims().setSubject(username)
    claims["roles"] = roles
    val now = Date()
    val validity = Date(now.time + validityInMilliseconds)
    return Jwts.builder()
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(validity)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact()
  }

  fun getUsername(token: String): String = Jwts.parserBuilder().setSigningKey(key).build()
    .parseClaimsJws(token).body.subject

  @Suppress("UNCHECKED_CAST")
  fun getRoles(token: String): List<String> {
    val body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    val roles = body["roles"]
    return when (roles) {
      is List<*> -> roles.filterIsInstance<String>()
      is String -> listOf(roles)
      else -> emptyList()
    }
  }

  fun validateToken(token: String): Boolean {
    return try {
      val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
      !claims.body.expiration.before(Date())
    } catch (ex: Exception) {
      false
    }
  }
}

/**
 * Filter that extracts Bearer token and sets Authentication in SecurityContext.
 */
class JwtAuthenticationFilter(
  private val jwtTokenProvider: JwtTokenProvider,
  private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    try {
      val token = resolveToken(request)
      if (token != null && jwtTokenProvider.validateToken(token)) {
        val username = jwtTokenProvider.getUsername(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val authorities = jwtTokenProvider.getRoles(token).map { SimpleGrantedAuthority(it) } + userDetails.authorities
        val auth: Authentication = UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
      }
    } catch (ex: Exception) {
      // If token invalid, clear context and continue; handler will return 401 for protected endpoints
      org.springframework.security.core.context.SecurityContextHolder.clearContext()
    }
    filterChain.doFilter(request, response)
  }

  private fun resolveToken(request: HttpServletRequest): String? {
    val bearer = request.getHeader("Authorization")
    if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
      return bearer.substring(7)
    }
    return null
  }
}
