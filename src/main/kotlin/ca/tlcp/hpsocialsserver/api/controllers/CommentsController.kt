package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.PostDetails
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.api.notifyAll
import ca.tlcp.hpsocialsserver.db.NotificationRepository
import ca.tlcp.hpsocialsserver.db.Post
import ca.tlcp.hpsocialsserver.db.PostRepository
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/comment")
class CommentsController(
    @Autowired
    private val postRepository: PostRepository? = null,

    @Autowired
    private val userRepository: UserRepository? = null
) {

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @PostMapping("/{pid}")
    fun postComment(@PathVariable pid: Long, @RequestParam body: String, @AuthenticationPrincipal user: Any): Boolean {
        if (body.isBlank()) return false
        return try {
            val sender: User = userRepository!!.getUserByEmail(getUserID(user)).get()
            val post: Post = postRepository!!.findById(pid).get()
            val theComment = Post(
                parent = post,
                body = body,
                user = sender
            )
            postRepository.save(theComment)
            notifyAll(
                sentBy = sender.firstName!!,
                body = body,
                ntfyRepo = notificationRepository,
                userRepo = userRepository,
                currentUser = sender
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @GetMapping(path = ["/{pid}"])
    fun getCommentsForPost(@PathVariable pid: Long): List<PostDetails> {
        val currentPost: Post = postRepository!!.findById(pid).get()

        return postRepository!!.getAllByParent(currentPost).map { comment: Post ->
            PostDetails(comment, userRepository!!)
        }.reversed()
    }

}
