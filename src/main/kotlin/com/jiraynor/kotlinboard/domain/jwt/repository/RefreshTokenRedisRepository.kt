package com.jiraynor.kotlinboard.domain.jwt.repository

import com.jiraynor.kotlinboard.domain.jwt.entity.RefreshTokenEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRedisRepository : CrudRepository<RefreshTokenEntity, String> {
    fun existsByToken(token: String): Boolean
    fun deleteByToken(token: String)
}