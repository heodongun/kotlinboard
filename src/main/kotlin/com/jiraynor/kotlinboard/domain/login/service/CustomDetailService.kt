package com.jiraynor.kotlinboard.domain.login.service

import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import com.jiraynor.kotlinboard.domain.join.repository.UserRepository
import com.jiraynor.kotlinboard.domain.login.dto.CustomUserDetails
import jakarta.transaction.Transactional
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomDetailService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional
    override fun loadUserByUsername(username: String?): UserDetails {
        val userData: Users? =userRepository.findByEmail(username!!)
        if(userData != null){
            return CustomUserDetails(userData)
        }
        throw UsernameNotFoundException("유저가 읎노")
    }

}