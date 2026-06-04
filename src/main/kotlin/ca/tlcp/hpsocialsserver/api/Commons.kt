package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.api.controllers.SpringUserDetails
import ca.tlcp.hpsocialsserver.db.Notification
import ca.tlcp.hpsocialsserver.db.NotificationRepository
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import com.nimbusds.jose.util.StandardCharset
import org.jsoup.Jsoup
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import kotlin.math.abs


// Any function that is used accross multiple controllers are put here to avoid code duplication

fun getUserID(user: Any): String {
    return when (user) {
        is SpringUserDetails ->
            user.username

        is OAuth2User -> {
            val name = user.attributes["email"] as String
            name
        }

        is SpringUserDetails ->
            user.username

        is UsernamePasswordAuthenticationToken -> {
            val name = user.name
            name
        }

        is OAuth2AuthenticationToken -> {
            val name = user.principal!!.attributes["email"] as String
            name
        }

        is ByteArray -> {
            val decoded = String(user, StandardCharset.UTF_8)
            println("Decoded ${decoded}")
            decoded
        }

        else -> {

            println(user::class)
            println(user)
            throw IllegalStateException("Unknown principal type")
        }
    }
}

fun listMentionsFromPostBody(body: String): List<String> {
    val doc = Jsoup.parse(body)

    return doc.select("span[data-type=mention]")
        .mapNotNull { it.attr("data-label").takeIf { label -> label.isNotBlank() } }
        .distinct()
}

fun notifyAll(currentUser: User, sentBy: String, userRepo: UserRepository, ntfyRepo: NotificationRepository, body: String) {
     listMentionsFromPostBody(body)
        .filter { userHandle ->
            userRepo.existsUserByHandle(userHandle) && currentUser.handle != userHandle
        }.forEach { userHandle ->
        val user = userRepo.getUserByHandle(userHandle).get()
        val notification = Notification(
            tittle = "$sentBy mentioned you",
            body = "$sentBy mentioned you in a post\n\n\n${body.substring(0, abs(body.length - sentBy.length))}",
            user = user
        )
        ntfyRepo.save(notification)
    }
}