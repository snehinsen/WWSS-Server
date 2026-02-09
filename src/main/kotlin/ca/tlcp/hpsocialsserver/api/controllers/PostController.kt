package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.CommentDetails
import ca.tlcp.hpsocialsserver.api.PostDetails
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*


typealias SpringUserDetails = org.springframework.security.core.userdetails.UserDetails

data class UserRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
)

@RestController
@RequestMapping("/api/post")
class PostController {

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val postRepository: PostRepository? = null

    @Autowired
    private val commentRepository: CommentRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null



    @GetMapping
    fun getPost(@RequestParam(name = "id") id: Long): PostDetails {
        println("ID: $id")
        return PostDetails(postRepository?.findById(id)!!.get())
    }

    @PostMapping(path = ["/add"])
    fun addPost(
        @RequestParam body: String?,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        println(body)

        val email = when (user) {
            is SpringUserDetails ->
                user.username

            is OAuth2User ->
                user.attributes["email"] as String

            else -> throw IllegalStateException("Unknown principal type")
        }

        postRepository?.save(
            Post(body, null, userRepository?.getUserByEmail(email)?.get())
        )
        return true
    }

    @GetMapping(path = ["/getComments/{pID}"])
    fun getComments(@PathVariable pID: Long): MutableList<CommentDetails?> {
        val details: MutableList<CommentDetails?> = ArrayList<CommentDetails?>()
        try {
            for (comment in commentRepository?.getAllByPost(
                postRepository?.findById(pID)!!.get()
            )!!) {
                details.add(CommentDetails(comment!!))
            }
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
        }
        return details
    }

//    private fun addTMPPost() {
//        val user: User? = userRepository.getUserByUsername("harrypotter").get()
//        val post = Post()
//        post.body = "Hello Everyone! How's life everything good? The Ministry finally chucked Umbridge in prison"
//        post.user = user
//        postRepository.save(post)
//        println("Temp post added.")
//    }
}
