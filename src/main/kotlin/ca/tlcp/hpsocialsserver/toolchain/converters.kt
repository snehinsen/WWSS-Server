package ca.tlcp.hpsocialsserver.toolchain

import ca.tlcp.hpsocialsserver.db.User
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.security.core.userdetails.UserDetails

// I'll put any converters that I need in this file, to keep them all in one place. Anything that needs to be converted between my implementation and a Spring implementation and vise versa

fun User.toUserDetails(): UserDetails {
    val email = this.email ?: throw IllegalStateException("User.email is null")
    val pw = this.password ?: throw IllegalStateException("User.password is null")
    return CustomUserDetails(email, pw)
}