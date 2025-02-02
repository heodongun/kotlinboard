package com.jiraynor.kotlinboard.domain.jwt.service

import com.jiraynor.kotlinboard.domain.jwt.entity.RefreshTokenEntity
import com.jiraynor.kotlinboard.domain.jwt.repository.RefreshTokenRedisRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class AddRefreshEntity(
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository
) {
    fun addRefreshToken(email: String,refresh:String,expiredMs:Long) {
        refreshTokenRedisRepository.save(
            RefreshTokenEntity(
                email = email,
                token=refresh,
                expiredAt= LocalDateTime.now().plus(Duration.ofMillis(expiredMs))
            )
        )
    }
}