package com.jiraynor.kotlinboard.domain.join.repository

import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<Users, UUID>{
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Users?
}