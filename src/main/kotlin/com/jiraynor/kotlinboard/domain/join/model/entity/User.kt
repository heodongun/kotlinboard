package com.jiraynor.kotlinboard.domain.join.model.entity

import com.jiraynor.kotlinboard.domain.join.data.RoleEnum
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
data class Users (
    @Id
    @GeneratedValue
    val id: UUID = UUID.randomUUID(), // UUID 타입으로 설정


    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: RoleEnum,
    )