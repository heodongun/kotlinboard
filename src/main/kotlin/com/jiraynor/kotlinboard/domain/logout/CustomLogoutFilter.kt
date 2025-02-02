package com.jiraynor.kotlinboard.domain.logout

import com.jiraynor.kotlinboard.domain.jwt.repository.RefreshTokenRedisRepository
import com.jiraynor.kotlinboard.domain.jwt.service.JwtUtil
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.web.filter.GenericFilterBean


open class CustomLogoutFilter(
    private val jwtUtil: JwtUtil,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository
): GenericFilterBean() {


    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        doFilter(
            (request as HttpServletRequest),
            (response as HttpServletResponse), chain
        )
    }


        @Transactional
        open fun doFilter(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
            val requestUri = request.requestURI
            if (!requestUri.matches(Regex("^/logout$"))) {
                filterChain.doFilter(request, response)
                return
            }

            if (request.method != "POST") {
                filterChain.doFilter(request, response)
                return
            }

            val refresh = request.cookies?.find { it.name == "refresh" }?.value
            if (refresh == null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            try {
                jwtUtil.isExpired(refresh)
            } catch (e: ExpiredJwtException) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            if (jwtUtil.getCategory(refresh) != "refresh") {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            if (!refreshTokenRedisRepository.existsByToken(refresh)) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            refreshTokenRedisRepository.deleteByToken(refresh)

            val cookie = Cookie("refresh", null).apply {
                maxAge = 0
                path = "/"
            }

            response.addCookie(cookie)
            response.status = HttpServletResponse.SC_OK
        }
    }