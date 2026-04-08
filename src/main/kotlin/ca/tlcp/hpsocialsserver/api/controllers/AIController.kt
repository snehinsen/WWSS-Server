package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.CharacterState
import ca.tlcp.hpsocialsserver.api.CharacterStateDetails
import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.db.AICharacterState
import ca.tlcp.hpsocialsserver.db.AIStateRepository
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RequestMapping("/api/ai")
@RestController
class AIController {

    data class AIRegistrationRequest(
        val firstName: String,
        val lastName: String,
        val handle: String,
        val isWizarding: Boolean,
        val bio: String,
    )

    @Autowired
    var userRepository: UserRepository? = null

    @Autowired
    var stateRepository: AIStateRepository? = null

    @GetMapping("/{handle}")
    fun getState(@PathVariable handle: String): CharacterStateDetails {
        val user: User? = userRepository!!.getUserByHandle(handle).orElse(null)
        val state: AICharacterState = stateRepository!!.getAICharacterStateByUser(user!!).orElse(null)

        return CharacterStateDetails(
            state = state
        )
    }

    @PostMapping
    fun register(@RequestBody request: AIRegistrationRequest): Boolean {
        return try {
            val user = User(
                firstName = request.firstName,
                lastName = request.lastName,
                handle = request.handle,
                email = "${request.firstName}.${request.lastName}@wwss.ai",
                password = "agent",
                isBot = true,
                isWizarding = request.isWizarding,
                pfp = "",
                bio = request.bio
            )
            userRepository!!.save(user)
            val state = AICharacterState(
                user = user,
                status = CharacterState.free,
                energy = 1.0f,
                mood = "Happy",
                currentlyDoing = ""
            )
            stateRepository!!.save(state)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @GetMapping
    fun listAll(): List<UserDetails> {
        return try {
            userRepository!!.getAllByIsBot(true).map { user ->
                UserDetails(user, userRepository!!)
            }
        } catch (e: Exception) {
            listOf()
        }
    }

    @GetMapping("/message/{handle}")
    fun messageCharacter(@PathVariable handle: String, @RequestParam body: String): String {
        return ""
    }

    @GetMapping("/post/{handle}")
    fun makePost(@PathVariable handle: String): String {
        println("Generating post for $handle")
        return try {
            val user: User = userRepository!!.getUserByHandle(handle).get()
            println(user)
            val state: AICharacterState = stateRepository!!.getAICharacterStateByUser(user).get()
            println(state)
//        if (state.status == CharacterState.free && state.energy > 0.7f) {
//            val message = UserMessage("")
//            OllamaClient().ask(mutableListOf(message))
//        }
            return generateAutomatedMessage(
                state
            )
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating post"
        }
    }

    private fun generateAutomatedMessage(currentState: AICharacterState): String {
        return CharacterStateDetails(currentState).toString()
    }
}
