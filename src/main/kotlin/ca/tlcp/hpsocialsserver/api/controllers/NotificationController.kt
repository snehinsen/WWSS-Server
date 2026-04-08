package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.NotificationDetails
import ca.tlcp.hpsocialsserver.api.getuserID
import ca.tlcp.hpsocialsserver.db.Notification
import ca.tlcp.hpsocialsserver.db.NotificationRepository
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/notification")
class NotificationController {

    data class NotificationCreationRequest(
        val user: Long,
        val body: String,
        val title: String,
    )

    @Autowired
    private var userRepository: UserRepository? = null

    @Autowired
    private var notificationRepository: NotificationRepository? = null

    @GetMapping
    fun list(
        @AuthenticationPrincipal user: Any
    ): List<NotificationDetails> {

        val email = getuserID(user)

        val selectedUser: User = userRepository!!.getUserByEmail(email)!!.get()

        return notificationRepository!!
            .getNotificationsByUser(selectedUser)
            .reversed()
            .map { notification ->
                NotificationDetails(notification)
            }
    }

    @GetMapping("{id}")
    fun clearItem(
        @PathVariable id: Long,
    ): Boolean {
        if (!notificationRepository!!.existsById(id)) {
            return false
        } else {
            notificationRepository!!.deleteById(id)
            return true
        }
    }
}