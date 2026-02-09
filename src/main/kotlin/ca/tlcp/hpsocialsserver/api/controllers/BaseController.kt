package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.PostDetails
import ca.tlcp.hpsocialsserver.db.PostRepository
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class BaseController {

    @Autowired
    private var postRepository: PostRepository? = null

    @Autowired
    private var userRepository: UserRepository? = null

    @GetMapping(path = ["/feed"])
    fun feed(): MutableList<PostDetails?> {
        val postDetails: MutableList<PostDetails?> = ArrayList()
        for (post in postRepository?.findAll()!!) {
            postDetails.add(PostDetails(post!!))
        }
        return postDetails
    }

}