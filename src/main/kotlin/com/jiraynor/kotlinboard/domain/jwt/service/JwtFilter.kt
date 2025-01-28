package com.jiraynor.kotlinboard.domain.jwt.service

import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import com.jiraynor.kotlinboard.domain.login.dto.CustomUserDetails
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var accessToken: String? = request.getHeader("Authorization")

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        accessToken = accessToken.substring("Bearer ".length)

        try {
            jwtUtil.isExpired(accessToken) // 토큰 만료 여부 확인
        } catch (e: ExpiredJwtException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.apply {
                print("엑세스 토큰 만료됨")
                flush()
            }
            return
        }

        val category: String = jwtUtil.getCategory(accessToken)
        if (category != "access") {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.apply {
                print("이게 뭔 토큰이노")
                flush()
            }
            return
        }

        val user = Users(
            email = jwtUtil.getEmail(accessToken),
            password = "password",
            role = jwtUtil.getRole(accessToken)
        )

        val customUserDetails = CustomUserDetails(user)

        val authToken = UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.authorities)
        SecurityContextHolder.getContext().authentication = authToken

        filterChain.doFilter(request, response)
    }
}
