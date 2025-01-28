package com.jiraynor.kotlinboard.domain.jwt.service

import com.jiraynor.kotlinboard.domain.join.data.RoleEnum
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.SignatureException
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil(
    @Value("\${spring.jwt.secret}") private val secretKey: String
) {
    private val key: SecretKey = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")

    fun getEmail(token: String): String {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body["email", String::class.java]
        } catch (e: ExpiredJwtException) {
            throw RuntimeException("토큰이 만료되었습니다.")
        } catch (e: MalformedJwtException) {
            throw RuntimeException("유효하지 않은 토큰입니다.")
        } catch (e: SignatureException) {
            throw RuntimeException("서명이 유효하지 않습니다.")
        } catch (e: JwtException) {
            throw RuntimeException("JWT 파싱 중 오류가 발생했습니다.")
        }
    }

    fun getRole(token:String):RoleEnum{
        try{
            return RoleEnum.valueOf(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body["role", String::class.java] )
        }catch(e:JwtException){
            throw RuntimeException("토큰에서 역할 정보를 추출하는 중 오류가 발생했습니다")
        }
    }

    fun isExpired(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            true
        } catch (e: JwtException) {
            throw RuntimeException("토큰 만료 여부 확인 중 오류가 발생했습니다.")
        }
    }

    fun getCategory(token: String): String {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body["category", String::class.java]
        } catch (e: JwtException) {
            throw RuntimeException("토큰에서 카테고리를 추출하는 중 오류가 발생했습니다.")
        }
    }

    fun createJwt(category: String, email: String, role: RoleEnum, expiredMs: Long): String {
        try {
            return Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)
                .compact()
        } catch (e: Exception) {
            throw RuntimeException("JWT 생성 중 오류가 발생했습니다.")
        }
    }
}
