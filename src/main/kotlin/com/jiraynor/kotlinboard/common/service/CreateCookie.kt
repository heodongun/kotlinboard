package com.jiraynor.kotlinboard.common.service


import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Service

@Service
class CreateCookie {
    fun createCookie(key: String, value: String): Cookie {
        val cookie = Cookie(key, value).apply {
            maxAge = 14 * 24 * 60 * 60
            isHttpOnly = true
            secure = true
        }
        return cookie
    }
}
