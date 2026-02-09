package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import ca.tlcp.hpsocialsserver.toolchain.toUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/user")
class UserController {
    @Autowired
    private var userRepository: UserRepository? = null

    @Autowired
    private var passwordEncoder: PasswordEncoder? = null


    @PostMapping(path = ["/signup"])
    fun addUser(
        @RequestBody request: UserRegistrationRequest
    ): Boolean {
        if (userRepository!!.existsUserByEmail(request.email)) {
            return false
        } else {
            val tmpUser = User(
                name = request.name,
                email = request.email,
                password = passwordEncoder?.encode(request.password),
                isBot = false,
                isWizarding = false,
                bio = "",
                pfp = null,
                handle = ""
            )

            userRepository!!.save(tmpUser)

            val userDetails = tmpUser.toUserDetails()
            val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            SecurityContextHolder.getContext().authentication = auth

            return true
        }
    }

    @PostMapping(path = ["/configure"])
    fun configure(
        @RequestParam handle: String,
        @RequestParam isWizarding: Boolean,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = when (user) {
            is SpringUserDetails ->
                user.username

            is OAuth2User ->
                user.attributes["email"] as String

            else -> throw IllegalStateException("Unknown principal type")
        }

        val selectedUser: User? = userRepository?.getUserByEmail(email)?.orElse(null)
        selectedUser!!.handle = handle
        selectedUser!!.isWizarding = isWizarding
        return true
    }

    @PostMapping(path = ["/uploadPfp"])
    fun uploadPfp(
        @RequestParam(value = "file") pfp: MultipartFile,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = when (user) {
            is SpringUserDetails ->
                user.username

            is OAuth2User ->
                user.attributes["email"] as String

            else -> throw IllegalStateException("Unknown principal type")
        }

        val selectedUser: User? = userRepository?.getUserByEmail(email)?.orElse(null)
        selectedUser!!.pfp = pfp.bytes
        return true
    }

    @GetMapping(path = ["/handleCheck"])
    fun handleCheck(@RequestParam handle: String = ""): Boolean {
        return !userRepository!!.existsUserByHandle(handle)
    }

    @GetMapping(path = ["{handle}"])
    fun getProfile(@PathVariable handle: String?): ca.tlcp.hpsocialsserver.api.UserDetails {
        val selectedUser: User? = userRepository?.getUserByHandle(handle)?.orElse(null)
        val details = UserDetails(selectedUser!!)

        return details
    }

    @GetMapping(path = ["/id/{id}"])
    fun getProfileById(@PathVariable id: Long?): UserDetails {
        val selectedUser: User? = userRepository?.getUserById(id!!)?.orElse(null)
        val details = UserDetails(selectedUser!!)

        return details
    }

    @GetMapping(path = ["/"])
    fun allUsers(): MutableList<UserDetails?> {
        val userDetailsList: MutableList<UserDetails?> = ArrayList<UserDetails?>()
        for (user in userRepository?.findAll()!!) {
            userDetailsList.add(UserDetails(user!!))
        }
        return userDetailsList
    }

}