package com.jiraynor.kotlinboard.common.entity

import java.time.LocalDateTime

open class BaseEntity(
    var createdAt: LocalDateTime = LocalDateTime.now(), // 생성 시간
    var updatedAt: LocalDateTime = LocalDateTime.now()  // 수정 시간
) {
    // 생성 시점에 자동으로 현재 시간을 설정
    fun updateTimestamp() {
        updatedAt = LocalDateTime.now()  // 업데이트 시점을 기록
    }
}