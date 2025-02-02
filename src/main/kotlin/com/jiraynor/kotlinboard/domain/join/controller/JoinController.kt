package com.jiraynor.kotlinboard.domain.join.controller

import com.jiraynor.kotlinboard.domain.join.data.RoleEnum
import com.jiraynor.kotlinboard.domain.join.model.dto.JoinDto
import com.jiraynor.kotlinboard.domain.join.model.entity.Users
import com.jiraynor.kotlinboard.domain.join.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/join")
class JoinController(
    private val userRepository: UserRepository,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {


    @PostMapping
    fun join(
        @RequestBody joinDto: JoinDto
    ): ResponseEntity<Any> {
        val user = userRepository.save(Users(email = joinDto.email, password = joinDto.password, role = RoleEnum.ROLE_USER))

        return ResponseEntity.ok(user)
    }
}