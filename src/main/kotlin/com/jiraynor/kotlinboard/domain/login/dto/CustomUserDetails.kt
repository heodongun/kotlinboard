package com.jiraynor.kotlinboard.domain.login.dto

import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val user:Users
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(GrantedAuthority { user.role.name })
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.email
    }
    fun getEmail(): String {
        return user.email
    }
}