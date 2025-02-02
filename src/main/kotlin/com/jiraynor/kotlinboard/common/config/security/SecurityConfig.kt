package com.jiraynor.kotlinboard.common.config.security

import com.jiraynor.kotlinboard.common.service.CreateCookie
import com.jiraynor.kotlinboard.domain.jwt.repository.RefreshTokenRedisRepository
import com.jiraynor.kotlinboard.domain.jwt.service.AddRefreshEntity
import com.jiraynor.kotlinboard.domain.jwt.service.JwtFilter
import com.jiraynor.kotlinboard.domain.jwt.service.JwtUtil
import com.jiraynor.kotlinboard.domain.login.service.LoginFilter
import com.jiraynor.kotlinboard.domain.logout.CustomLogoutFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val authenticationConfiguration: AuthenticationConfiguration,
    private val jwtUtil: JwtUtil,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
){
    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(): AuthenticationManager = authenticationConfiguration.authenticationManager


    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/email", "/join", "/login", "/refresh", "/verify").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/documents", "/users/search/{user-name}", "/documents/search", "/comments/{document-id}", "/favorites", "/announcements").permitAll()
                    .requestMatchers(HttpMethod.POST, "/logout", "/comments/{document-id}", "/favorites/{document-id}").hasAnyRole("USER", "STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.GET, "/users/{user-id}").hasAnyRole("USER", "STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/users").hasAnyRole("USER", "STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.POST, "/applications").hasRole("USER")
                    .requestMatchers(HttpMethod.GET, "/applications").hasAnyRole("ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/permissions").hasRole("SUPERADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/documents", "/comments/{document-id}/{comment-id}").hasAnyRole("STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/documents", "/likes/{document-id}").hasAnyRole("STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.POST, "/likes/{document-id}").hasAnyRole("STUDENT", "ADMIN", "SUPERADMIN")
                    .requestMatchers(HttpMethod.POST, "/S3").hasAnyRole("STUDENT", "ADMIN", "SUPERADMIN")
                    .anyRequest().denyAll()
            }
           // .exceptionHandling { it.accessDeniedHandler(customAccessDeniedHandler) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors {
                it.configurationSource(CorsConfigurationSource { request: HttpServletRequest ->
                    CorsConfiguration().apply {
                        addAllowedOriginPattern("*")
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                        exposedHeaders = listOf("Authorization")
                        maxAge = 3600L
                    }
                })
            }

        http.addFilterBefore(JwtFilter(jwtUtil), LoginFilter::class.java)
        http.addFilterAt(
            LoginFilter(
                authenticationManager = authenticationManager(),
                jwtUtil = jwtUtil,
                refreshRepository = refreshTokenRedisRepository,
                addRefreshEntity = AddRefreshEntity(refreshTokenRedisRepository),
                createCookie = CreateCookie()
            ),
            UsernamePasswordAuthenticationFilter::class.java
        )
        http.addFilterBefore(CustomLogoutFilter(jwtUtil, refreshTokenRedisRepository), LogoutFilter::class.java)

        return http.build()
    }
}