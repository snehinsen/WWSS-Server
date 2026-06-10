package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.PostDetails
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.api.notifyAll
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.ai.ollama.OllamaChatModel
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

    @Autowired
    private val model: OllamaChatModel? = null

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

    @PostMapping(path = ["/add"])
    fun addPost(
        @RequestParam body: String?,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = getUserID(user)
        val sender: User = userRepository?.getUserByEmail(email)!!.get()
        val saved = postRepository?.save(
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
            body = body!!,
            model = model!!,
            pid = saved!!.id!!
        )
        return true
    }

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long): PostDetails {
        val post: Post = postRepository!!.findById(id)!!.get()
        return PostDetails(post, userRepository!!)
    }

    @PostMapping("/{id}/like")
    fun toggleLike(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentuser: Any
    ): Boolean {
        println(id)

        println("=== toggleLike START ===")
        println("Requested post id: $id")

        return try {
            val postOptional = postRepository!!.findById(id)

            if (postOptional.isEmpty) {
                println("Post not found: $id")
                return false
            }

            val post = postOptional.get()

            println("Found post:")
            println("  post.id = ${post.id}")
            println("  author = ${post.user!!.id}")
            println("  likedBy = ${post.likedBy}")

            val email = getUserID(currentuser)

            println("Current user email: $email")

            val userOptional = userRepository!!.getUserByEmail(email)

            if (userOptional.isEmpty) {
                println("User not found for email: $email")
                return false
            }

            val user = userOptional.get()

            println("Found user:")
            println("  id = ${user.id}")
            println("  handle = ${user.handle}")

            val userId = user.id

            if (userId == null) {
                println("User ID is null!")
                return false
            }

            println("Checking if post is liked by user...")

            var isLiked = false

            if (post.likedBy == null) {
                post.likedBy = mutableListOf(user.id!!)
                postRepository!!.save(post)
                return true
            }

            post.likedBy.forEach {
                if (it === user.id) {
                    isLiked = true
                }

            }

            println("isLiked = $isLiked")

            if (isLiked) {
                println("Removing like...")
                post.likedBy.remove(userId)
                println("User ${user.handle} unliked post ${post.id}")
            } else {
                println("Adding like...")
                post.likedBy.add(userId)
                println("User ${user.handle} liked post ${post.id}")
            }

            println("Saving post...")
            postRepository!!.save(post)

            println("Save complete.")
            println("Updated likedBy = ${post.likedBy}")
            println("=== toggleLike SUCCESS ===")

            true
        } catch (e: Exception) {
            println("=== toggleLike FAILED ===")
            e.printStackTrace()
            false
        }
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
