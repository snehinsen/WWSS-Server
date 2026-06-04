package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.ChatType
import ca.tlcp.hpsocialsserver.api.ChatThreadDetails
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dms")
class DMController {

    data class CreateDMThreadRequest(
        val handles: List<String>,
        val tittle: String
    )

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val chatThreadRepository: ChatThreadRepository? = null

    @Autowired
    private val notificationRepository: NotificationRepository? = null

    @GetMapping
    fun getDMThreadsForUser(@AuthenticationPrincipal loggedInUser: Any): List<ChatThreadDetails> {
        return try {
            val email: String = getUserID(loggedInUser)
            val user: User = userRepository!!.getUserByEmail(email).get()

            chatThreadRepository!!.findAllUserThreads(user).map { thread ->
                ChatThreadDetails(thread, userRepository!!)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @GetMapping("/{threadId}/info")
    fun getThreadInfo(
        @AuthenticationPrincipal loggedInUser: Any,
        @RequestParam threadId: Long
    ): ChatThreadDetails? {
        return try {
            val email: String = getUserID(loggedInUser)
            val user: User = userRepository!!.getUserByEmail(email).get()
            val thread = chatThreadRepository!!.findById(threadId).get()

            // Verify user is part of the thread
            val isMember = thread.owner.id == user.id || thread.otherMembers.any { it.id == user.id }
            if (!isMember) {
                return null
            }

            ChatThreadDetails(thread, userRepository!!)
        } catch (e: Exception) {
            null
        }
    }

    @PostMapping("/create")
    fun createDMThread(
        @AuthenticationPrincipal loggedInUser: Any,
        @RequestBody request: CreateDMThreadRequest,
    ): Boolean {

        return try {

            val email: String = getUserID(loggedInUser)

            val user = userRepository!!
                .getUserByEmail(email)
                .get()

            val members: MutableList<User> = mutableListOf()

            println(request)

            for (handle in request.handles) {

                val member = userRepository
                    .getUserByHandle(handle)
                    .get()

                members.add(member)
            }

            val isDM = members.size == 1

            /*
                Prevent duplicate DMs
            */
            if (isDM) {

                val otherUser = members.first()

                // Prevent self DMs
                if (otherUser.id == user.id) {

                    println("Cannot create DM with yourself.")

                    return false
                }

                val existingThreads = chatThreadRepository!!
                    .findAllUserThreads(user)

                val duplicateDM = existingThreads.any { thread ->

                    // Must be a DM
                    if (thread.threadType != ChatType.DM) {
                        return@any false
                    }

                    // DM should only contain one other member
                    if (thread.otherMembers.size != 1) {
                        return@any false
                    }

                    val existingOther = thread.otherMembers.first()

                    /*
                        Match either direction:

                        user -> otherUser
                        OR
                        otherUser -> user
                    */
                    (
                            thread.owner.id == user.id &&
                                    existingOther.id == otherUser.id
                            ) || (
                            thread.owner.id == otherUser.id &&
                                    existingOther.id == user.id
                            )
                }

                if (duplicateDM) {

                    println("Duplicate DM prevented.")

                    return false
                }
            }

            println(request.tittle)

            /*
                Generate final thread title
            */
            var finalTitle = request.tittle.ifBlank {

                if (members.size == 1) {

                    "Chat with ${members.first().handle}"

                } else {

                    "Chat with ${
                        members.joinToString(", ") { member ->
                            member.firstName!!
                        }
                    }"
                }
            }

            /*
                Add Windows-style suffixes for duplicate
                GROUP CHAT names only
            */
            if (!isDM) {

                val existingThreads = chatThreadRepository!!
                    .findAllUserThreads(user)

                val existingTitles = existingThreads
                    .map { it.title.trim() }
                    .toSet()

                if (existingTitles.contains(finalTitle)) {

                    var counter = 1

                    var candidate = "$finalTitle ($counter)"

                    while (existingTitles.contains(candidate)) {

                        counter++

                        candidate = "$finalTitle ($counter)"
                    }

                    finalTitle = candidate
                }
            }

            val newThread = ChatThread(
                owner = user,
                otherMembers = members,
                title = finalTitle,
                threadType = if (isDM) ChatType.DM else ChatType.GC,
            )

            chatThreadRepository!!.save(newThread)

            true

        } catch (e: Exception) {

            e.printStackTrace()

            false
        }
    }


    @PostMapping("/join")
    fun joinThread(
        @AuthenticationPrincipal loggedInUser: Any,
        @RequestParam threadId: Long,
    ): Boolean {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()
            val thread = chatThreadRepository!!.findById(threadId).get()
            thread.otherMembers.add(user)
            chatThreadRepository!!.save(thread)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @DeleteMapping("/leave")
    fun leaveThread(
        @AuthenticationPrincipal loggedInUser: Any,
        @RequestParam threadId: Long,
    ): Boolean {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()
            val thread = chatThreadRepository!!.findById(threadId).get()
            if (thread.owner == user || !thread.otherMembers.contains(user)) {
                System.err.println("User is not the owner or part of this thread.")
                return false
            } else {
                thread.otherMembers.remove(user)
                chatThreadRepository!!.save(thread)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @DeleteMapping("/delete")
    fun deleteThread(
        @AuthenticationPrincipal loggedInUser: Any,
        @RequestParam threadId: Long,
    ): Boolean {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()
            val thread = chatThreadRepository!!.findById(threadId).get()
            if (thread.owner != user) {
                System.err.println("User is not the owner of this thread.")
                false
            } else {
                thread.otherMembers.forEach { member ->
                    notificationRepository!!.save(
                        Notification(
                            tittle = "Chat Deleted",
                            body = "The chat thread '${thread.title}' was deleted by the owner.",
                            user = member
                        )
                    )
                }
                chatThreadRepository.delete(thread)

                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
