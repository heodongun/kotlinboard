package com.jiraynor.kotlinboard.common.config.email

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*


@Configuration
class EmailConfig {

    @Value("\${spring.mail.host:localhost}")  // Default value if not set
    private lateinit var host: String

    @Value("\${spring.mail.port:587}")  // Default value if not set
    private var port: Int = 0

    @Value("\${spring.mail.username:}")  // Default value if not set
    private lateinit var username: String

    @Value("\${spring.mail.password:}")  // Default value if not set
    private lateinit var password: String

    @Value("\${spring.mail.properties.mail.smtp.auth:true}")  // Default value if not set
    private var auth: Boolean = false

    @Value("\${spring.mail.properties.mail.smtp.starttls.enable:true}")  // Default value if not set
    private var starttlsEnable: Boolean = false

    @Value("\${spring.mail.properties.mail.smtp.timeout:25000}")  // Default value if not set
    private var timeout: Int = 0

    /**
     * JavaMailSender 빈을 생성하고 반환합니다.
     * 이메일 서버 설정을 기반으로 이메일 전송을 위한 JavaMailSender를 구성합니다.
     *
     * @return JavaMailSender 설정된 이메일 전송 객체
     */
    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password
        mailSender.defaultEncoding = "UTF-8"
        mailSender.javaMailProperties = getMailProperties()

        return mailSender
    }

    /**
     * 이메일 전송에 필요한 추가 속성들을 반환합니다.
     *
     * @return 설정된 메일 속성(Properties 객체)
     */
    private fun getMailProperties(): Properties {
        val properties = Properties()
        properties["mail.smtp.auth"] = auth
        properties["mail.smtp.starttls.enable"] = starttlsEnable
        properties["mail.smtp.timeout"] = timeout
        return properties
    }
}