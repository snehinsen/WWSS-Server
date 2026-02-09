package ca.tlcp.hpsocialsserver.toolchain

import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.security.core.userdetails.UserDetails

// I'll put any converters that I need in this file, to keep them all in one place. Anything that needs to be converted between my implementation and a Spring implementation and vise versa

fun CustomUserDetails.toUser(userRepo: UserRepository): User {
    return userRepo.getUserByEmail(username).get()
}

fun User.toUserDetails(): UserDetails {
    val email = this.email ?: throw IllegalStateException("User.email is null")
    val pw = this.password ?: throw IllegalStateException("User.password is null")
    return CustomUserDetails(email, pw)
}