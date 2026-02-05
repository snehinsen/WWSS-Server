package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.db.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class APIController {
    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val postRepository: PostRepository? = null

    @Autowired
    private val commentRepository: CommentRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @PostMapping(path = ["/signup"])
    fun addUser(
        @RequestParam name: String?,
        @RequestParam username: String?,
        @RequestParam password: String,
        @RequestParam bio: String?,
        @RequestParam isWizarding: Boolean,
        @RequestParam handle: String?
    ): Boolean {
        if (userRepository?.existsUserByUsername(username)!!) {
            return false
        } else {
            val tmpUser: User = User()
            tmpUser.name = name
            tmpUser.username = username
            tmpUser.password = passwordEncoder?.encode(password)
            tmpUser.isBot = false
            tmpUser.isWizarding = isWizarding
            tmpUser.bio = bio
            tmpUser.handle = handle
            userRepository.save(tmpUser)
            return true
        }
    }

    @GetMapping(path = ["profile/{username}"])
    fun getProfile(@PathVariable username: String?): UserDetails {
        val selecteduser: User? = userRepository?.getUserByUsername(username)?.orElse(User())
        val details = UserDetails(selecteduser!!)

        return details
    }

    @get:GetMapping(path = ["/users"])
    val allUsers: MutableList<UserDetails?>
        get() {
            val userDetailsList: MutableList<UserDetails?> = ArrayList<UserDetails?>()
            for (user in userRepository?.findAll()!!) {
                userDetailsList.add(UserDetails(user!!))
            }
            return userDetailsList
        }

    @GetMapping(path = ["feed"])
    fun feed(): MutableList<PostDetails?> {
        val postDetails: MutableList<PostDetails?> = ArrayList<PostDetails?>()
        for (post in postRepository?.findAll()!!) {
            postDetails.add(PostDetails(post!!))
        }
        return postDetails
    }

    @GetMapping(path = ["getPost"])
    fun getPost(@RequestParam(name = "id") id: Long): PostDetails {
        println("ID: $id")
        return PostDetails(postRepository?.findById(id)!!.get())
    }

    @PostMapping(path = ["addPost"])
    fun addPost(
        @RequestParam body: String?, @RequestParam username: String?
    ): Boolean {
        println(body)
        println(username)
        postRepository?.save(
            Post(body, null, userRepository?.getUserByUsername(username)?.get())
        )
        return true
    }

    @GetMapping(path = ["getPostComments/{pID}"])
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
