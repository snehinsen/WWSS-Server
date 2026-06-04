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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/post")
class PostController {

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val postRepository: PostRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @GetMapping(path = ["/feed"])
    fun feed(): List<PostDetails?> {
        return postRepository!!.findAll().filter { post: Post ->
            post.parent == null
        }.map { post: Post ->
            PostDetails(post, userRepository!!)
        }.reversed()
    }

    @GetMapping("user/{handle}")
    fun getPosts(@PathVariable handle: String): List<PostDetails> {
        val user = userRepository!!.getUserByHandle(handle).get()
        return postRepository!!.getAllByUser(user).map { post: Post ->
            PostDetails(post, userRepository)
        }
    }

    @GetMapping()
    fun getUserPosts(@AuthenticationPrincipal user: Any): List<PostDetails> {
        val currentUser = userRepository!!.getUserByEmail(getUserID(user)).get()
        return postRepository!!.getAllByUser(currentUser).map { comment: Post ->
            PostDetails(comment, userRepository!!)
        }.reversed()
    }

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long): PostDetails {
        println("ID: $id")
        return PostDetails(postRepository?.findById(id)!!.get(), userRepository!!)
    }

    @PostMapping(path = ["/add"])
    fun addPost(
        @RequestParam body: String?,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = getUserID(user)
        val sender: User = userRepository?.getUserByEmail(email)!!.get()
        postRepository?.save(
            Post(
                body = body,
                user = sender
            )
        )
        notifyAll(
            currentUser = sender!!,
            sentBy = sender.firstName!!,
            userRepo = userRepository,
            ntfyRepo = notificationRepository,
            body = body!!
        )
        return true
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Long, @AuthenticationPrincipal user: Any): Boolean {
        val email = getUserID(user)
        try {
            val post: Post = postRepository!!.findById(id)!!.get()
            if (post.user!!.email == email) {
                val childPosts: List<Post> = postRepository!!.getAllByParent(post)
                childPosts.forEach { childPost: Post ->
                    postRepository.delete(childPost)
                }
                postRepository.delete(post)
                return true
            } else {
                error("User: ${post.user!!.handle} tried to delete a post (${post.id}) they didn't make")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
