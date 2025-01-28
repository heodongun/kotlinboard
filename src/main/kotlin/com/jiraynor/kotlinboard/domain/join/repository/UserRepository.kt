package com.jiraynor.kotlinboard.domain.join.repository

import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Users, Long>{
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Users?
}