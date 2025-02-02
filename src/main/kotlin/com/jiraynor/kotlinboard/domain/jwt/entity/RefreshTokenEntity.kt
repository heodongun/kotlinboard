package com.jiraynor.kotlinboard.domain.jwt.entity

import com.jiraynor.kotlinboard.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime

@RedisHash("RefreshToken")
class RefreshTokenEntity(
    @Id
    val email: String,
    val token: String,
    val expiredAt: LocalDateTime,
) : BaseEntity() {
    // Redis에서 값이 저장될 때, BaseEntity를 통해 자동으로 생성 및 수정 시간이 관리됩니다.
}