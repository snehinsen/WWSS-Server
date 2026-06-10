package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import ca.tlcp.hpsocialsserver.fs.PFPEntity
import ca.tlcp.hpsocialsserver.fs.PFPRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

typealias SpringUserDetails = org.springframework.security.core.userdetails.UserDetails


@RestController
@RequestMapping("/api/user")
class UserController {


    data class UserRegistrationRequest(
        val firstName: String,
        val lastName: String,
        val email: String,
        val password: String,
    )

    class UserCustomizerRequest(
        val handle: String,
        val isWizarding: Boolean,
    )

    @Autowired
    private lateinit var authManager: AuthenticationManager

    @Autowired
    private var userRepository: UserRepository? = null

    @Autowired
    private var passwordEncoder: PasswordEncoder? = null

    @GetMapping
    fun me(
        @AuthenticationPrincipal user: Any
    ): UserDetails {
        val email = getUserID(user)

        val selectedUser: User = userRepository!!.getUserByEmail(email)!!.get()

        return UserDetails(selectedUser, userRepository!!)
    }


    @GetMapping(path = ["/search"])
    fun getFiltered(@AuthenticationPrincipal user: Any, @RequestParam(name = "q") query: String): List<UserDetails> {
        val email = getUserID(user)
        var users: List<User> = userRepository!!.findAll().filter {
            !it.email.equals(email)
        }
        if (query.isNotEmpty() || query.isNotBlank()) {
            users = users.filter {
                it.handle!!.contains(query) || it.firstName!!.contains(query) || it.lastName!!.contains(query)
            }
        } else {
            return users.map { user ->
                UserDetails(user, userRepository!!)
            }
        }


        println("Found ${users.size} users matching query '$query' for user with email '$email'")

        return users.map { user: User ->
            UserDetails(user, userRepository!!)
        }
    }

    @PostMapping(path = ["/signup"])
    fun addUser(
        @RequestBody request: UserRegistrationRequest,
    ): Boolean {
        if (userRepository!!.existsUserByEmail(request.email)) {
            return false
        } else {
            try {
                val tmpUser = User(
                    firstName = request.firstName,
                    lastName = request.lastName,
                    email = request.email,
                    password = passwordEncoder?.encode(request.password),
                    isBot = false,
                    isWizarding = false,
                    bio = "",
                    pfp = null,
                    isSetup = false,
                )
                val saved = userRepository!!.save(tmpUser)
                println("User saved: $saved")

                val authToken = UsernamePasswordAuthenticationToken(request.email, request.password)
                val auth = authManager.authenticate(authToken)
                SecurityContextHolder.getContext().authentication = auth

                return true
            } catch (e: Exception) {
                println("Error during user registration: ${e.message}")
                e.printStackTrace()
                return false
            }
        }
    }


    @PostMapping(path = ["/configure"])
    fun configure(
        @RequestBody request: UserCustomizerRequest,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = getUserID(user)

        println("Configuring user with email: $email, handle: ${request.handle}, isWizarding: ${request.isWizarding}")
        try {
            val selectedUser: User? = userRepository?.getUserByEmail(email)?.orElse(null)
            selectedUser!!.handle = request.handle
            selectedUser!!.isWizarding = request.isWizarding
            selectedUser!!.isSetup = true
            userRepository!!.save(selectedUser)
            return true
        } catch (e: Exception) {
            println("Error during user configuration: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    @PostMapping(path = ["/pfp"])
    fun uploadPfp(
        @RequestParam(value = "file") pfp: MultipartFile,
        @AuthenticationPrincipal user: Any
    ): Boolean {
        val email = getUserID(user)
        try {
            val selectedUser: User? = userRepository?.getUserByEmail(email)?.orElse(null)

            PFPEntity().saveObject(
                label = selectedUser!!.handle!!,
                entity = PFPRepresentation(
                    label = pfp.originalFilename!!.split(".").last(),
                    value = pfp.bytes
                )
            )

            selectedUser!!.pfp =
                "/api/user/pfp/${selectedUser.handle}"

            userRepository!!.save(selectedUser)

            return true
        } catch (e: Exception) {
            println("Error during profile picture upload: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    @GetMapping(path = ["/handleCheck"])
    fun handleCheck(@RequestParam handle: String = "", @AuthenticationPrincipal user: Any): Boolean {
        val currentUser: User = userRepository!!.getUserByEmail(getUserID(user)).get()

        return if (currentUser.handle != handle) !userRepository!!.existsUserByHandle(handle) else true

    }

    @GetMapping(path = ["{handle}"])
    fun getProfile(@PathVariable handle: String?): UserDetails? {
        println("Getting profile for handle: $handle")
        try {
            val selectedUser: User = userRepository!!.getUserByHandle(handle)!!.get()
            val details = UserDetails(selectedUser!!, userRepository!!)

            return details
        } catch (e: Exception) {
            println("Error getting profile for handle '$handle': ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    @GetMapping("/pfp/{handle}")
    fun getPicture(@PathVariable handle: String): ResponseEntity<FileSystemResource> {
        try {
            val selectedUser = userRepository!!.getUserByHandle(handle).get()

            val file = PFPEntity().getObject(selectedUser.handle!!)

            val resource = FileSystemResource(file)

            val contentType = Files.probeContentType(file.toPath())

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource)
        } catch (e: Exception) {
            println("Error getting profile for handle '$handle': ${e.message}")
            e.printStackTrace()
            return ResponseEntity.badRequest().body(FileSystemResource(""))
        }
    }


    @GetMapping(path = ["/id/{id}"])
    fun getProfileById(@PathVariable id: Long?): UserDetails {
        val selectedUser: User? = userRepository?.getUserById(id!!)?.orElse(null)
        return UserDetails(selectedUser!!, userRepository!!)
    }

    @GetMapping(path = ["/all"])
    fun allUsers(): MutableList<UserDetails?> {
        val userDetailsList: MutableList<UserDetails?> = ArrayList<UserDetails?>()
        for (user in userRepository?.findAll()!!) {
            userDetailsList.add(UserDetails(user!!, userRepository!!))
        }
        return userDetailsList
    }

}