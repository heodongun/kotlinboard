package com.jiraynor.kotlinboard.domain.login.service
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jiraynor.kotlinboard.common.service.CreateCookie
import com.jiraynor.kotlinboard.domain.join.data.RoleEnum
import com.jiraynor.kotlinboard.domain.jwt.entity.RefreshTokenEntity
import com.jiraynor.kotlinboard.domain.jwt.repository.RefreshTokenRedisRepository
import com.jiraynor.kotlinboard.domain.jwt.service.AddRefreshEntity
import com.jiraynor.kotlinboard.domain.jwt.service.JwtUtil
import com.jiraynor.kotlinboard.domain.login.dto.CustomUserDetails
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

open class LoginFilter(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val refreshRepository: RefreshTokenRedisRepository,
    private val addRefreshEntity: AddRefreshEntity,
    private val createCookie: CreateCookie
) : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val (email, password) = try {
            if (request.contentType != null && request.contentType.equals("application/json", ignoreCase = true)) {
                // JSON 요청 처리
                val objectMapper = ObjectMapper()
                val requestBody = objectMapper.readValue<Map<String, String>>(
                    request.inputStream,
                    object : TypeReference<Map<String, String>>() {}
                )
                requestBody["email"] to requestBody["password"]
            } else {
                // 기존 form-data 방식 처리
                request.getParameter("email") to request.getParameter("password")
            }
        } catch (e: IOException) {
            throw AuthenticationServiceException("요청 본문을 읽는 중 오류 발생", e)
        }

        println(email)
        println(password)
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            throw BadCredentialsException("이메일 또는 비밀번호가 제공되지 않았습니다.")
        }

        val authToken = UsernamePasswordAuthenticationToken(email, password)
        return authenticationManager.authenticate(authToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authentication: Authentication
    ) {
        val customUserDetails = authentication.principal as CustomUserDetails
        val email = customUserDetails.getEmail()
        val role = RoleEnum.valueOf(authentication.authorities.first().authority)

        // JWT 생성
        val access = jwtUtil.createJwt(category = "access", email = email, role= role, 1800000L)
        val refresh = jwtUtil.createJwt(category = "refresh", email=email, role=role, 1209600000L)

        // Refresh 토큰 저장
        addRefreshEntity.addRefreshToken(email=email,refresh=refresh, expiredMs =1209600000L )

        // 응답에 Access 토큰 추가, Refresh 토큰을 쿠키로 설정
        response.setHeader("Authorization", access)
        response.addCookie(createCookie.createCookie("refresh", refresh))
        response.status = HttpStatus.OK.value()
    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val jsonResponse = """
            {
                "timestamp":"${LocalDateTime.now()}",
                "status":401,
                "error":"Unauthorized",
                "message":"INVALID_CREDENTIALS",
                "path":"${request.requestURI}"
            }
        """.trimIndent()

        response.writer.use { it.write(jsonResponse) }
    }


    open fun addRefreshEntity(uid: String, email: String, refresh: String, expiredMs: Long) {
        val expiryDateTime = LocalDateTime.now().plusNanos(expiredMs * 1_000_000)
        refreshRepository.save(
            RefreshTokenEntity(
                email = email,
                token = refresh,
                expiredAt = expiryDateTime
            )
        )
    }
}