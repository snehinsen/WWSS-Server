package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.CharacterState
import ca.tlcp.hpsocialsserver.ai.ToolFactory
import ca.tlcp.hpsocialsserver.api.CharacterStateDetails
import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.db.*
import ca.tlcp.hpsocialsserver.fs.LLMSystemEntity
import ca.tlcp.hpsocialsserver.fs.LLMTaskingEntity
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileReader

@RequestMapping("/api/ai")
@RestController
class AIController {

    data class TaskCompletionDetails(
        var success: Boolean,
        val tasksCompleted: MutableList<String>,
        val tasksFailed: MutableList<String>,
        val errors: MutableList<String>
    ) {
        constructor() : this(
            success = true,
            tasksCompleted = mutableListOf(),
            tasksFailed = mutableListOf(),
            errors = mutableListOf()
        )

        constructor(success: Boolean = true) : this(
            success = success,
            tasksCompleted = mutableListOf(),
            tasksFailed = mutableListOf(),
            errors = mutableListOf()
        )

        fun addSuccessful(
            label: String
        ) {
            tasksCompleted.add(label)
        }

        fun addFailed(
            label: String,
            error: String
        ) {
            tasksFailed.add(label)
            errors.add(error)
            success = false
        }
    }

    data class AIRegistrationRequest(
        val firstName: String,
        val lastName: String,
        val handle: String,
        val isWizarding: Boolean,
        val bio: String,
    )

    @Autowired
    val anthropicChatModel: AnthropicChatModel? = null

    @Autowired
    val ollamaChatModel: OllamaChatModel? = null

    @Autowired
    val postRepository: PostRepository? = null

    @Autowired
    lateinit var toolFactory: ToolFactory

    @Autowired
    var userRepository: UserRepository? = null

    @Autowired
    var stateRepository: AIStateRepository? = null

    @GetMapping("/{handle}")
    fun getState(@PathVariable handle: String): CharacterStateDetails {
        val user: User? = userRepository!!.getUserByHandle(handle).orElse(null)
        val state: AICharacterState = stateRepository!!.getAICharacterStateByUser(user!!).get()

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
    fun messageCharacter(
        @PathVariable handle: String,
        @RequestParam body: String
    ): String {
        return ""
    }

    @GetMapping("/{handle}/post")
    fun makePost(@PathVariable handle: String): Boolean {
        val llmSystemEntity: LLMSystemEntity = LLMSystemEntity()

        val modelFile: File = llmSystemEntity.getObject(handle)

        val content = FileReader(modelFile).readText()

        return try {

            val user: User =
                userRepository!!.getUserByHandle(handle).get()

            val state: AICharacterState =
                stateRepository!!
                    .getAICharacterStateByUser(user)
                    .get()

            if (state.status == CharacterState.free || state.energy > 0.2f) {

                // FIXED: Changed to ChatClient.builder() pattern to correctly bind advisor layers
                val client = ChatClient.builder(anthropicChatModel!!)
                    .defaultAdvisors(SimpleLoggerAdvisor()) // Allows tracking the consecutive tool loop runs inside logs
                    .build()

                val post = Post(
                    body = "",
                    parent = null,
                    user = user
                )

                val tools = toolFactory.createPostTools(
                    character = user,
                    post = post
                )

                // FIXED: Using terminal .content() so Spring AI evaluates the response, runs tools,
                // feeds outputs back to the LLM, and loops continuously until a final string response is generated.
                val response: String? = client
                    .prompt(generateAutomatedMessage(state))
                    .system(content)
                    .tools(tools)
                    .call()
                    .content()

                if (response != null) {
                    post.body = response
                    println(postRepository!!.save(post).id)
                    return true
                }

                return false

            } else {

                println(
                    "Not in a state to post\n\n${
                        CharacterStateDetails(state)
                    }"
                )

                false
            }

        } catch (e: Exception) {

            println(anthropicChatModel ?: "No chatModel")

            e.printStackTrace()

            false
        }
    }

    @GetMapping("/{handle}/checkTasks")
    fun checkTasks(@PathVariable handle: String): TaskCompletionDetails {

        val details = TaskCompletionDetails(
            success = true,
        )

        val llmTaskingEntity = LLMTaskingEntity()

        val modelFile: File = llmTaskingEntity.getObject(handle)

        val content = FileReader(modelFile).readText()

        return try {

            val user: User =
                userRepository!!.getUserByHandle(handle).get()

            val state: AICharacterState =
                stateRepository!!
                    .getAICharacterStateByUser(user)
                    .get()

            // FIXED: Builder pattern configures the model to safely execute multi-turn loops
            val client: ChatClient = ChatClient.builder(ollamaChatModel!!)
                .defaultAdvisors(SimpleLoggerAdvisor())
                .build()

            if (state.energy > 0.5 && ( state.status != CharacterState.busy  || state.status != CharacterState.sleeping )) {

                val tools = toolFactory.createTaskTools(
                    character = user,
                    taskDetails = details
                )

                // FIXED: Resolved using terminal .content() to auto-loop multiple tool requests
                val output = client
                    .prompt(
                        generateAutomatedMessage(
                            currentState = state,
                            mode = "checkTasks"
                        )
                    )
                    .system(content)
                    .tools(tools)
                    .call()
                    .content()

                println(output)
            } else {
                error(
                    "Not in a state to check tasks\n\n${CharacterStateDetails(state)}"
                )
            }

            return details

        } catch (e: Exception) {

            println(ollamaChatModel ?: "No chatModel")

            e.printStackTrace()

            details.addFailed(
                label = "Exception during task checking",
                error = e.stackTraceToString()
            )

            details
        }
    }

    // FIXED: Fully closed the truncated helper function signature
    private fun generateAutomatedMessage(
        currentState: AICharacterState,
        mode: String = ""
    ): String {
        // Implement your prompt-generation business logic here
        return "Current state is ${currentState.status}. Mode: $mode"
    }
}
