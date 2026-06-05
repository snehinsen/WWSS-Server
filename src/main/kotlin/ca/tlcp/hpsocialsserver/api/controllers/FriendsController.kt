package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.FriendRequestDetails
import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/friends"])
class FriendsController {

    @Autowired
    private var userRepository: UserRepository? = null

    @Autowired
    private var friendRequestRepository: FriendRequestRepository? = null

    @Autowired
    private var notificationRepository: NotificationRepository? = null

    @GetMapping(value = ["/list/{handle}"])
    fun listFriends(@PathVariable handle: String): List<UserDetails> {
        try {
            val currentUser: User = userRepository!!.getUserByHandle(handle).get()

            return currentUser.friends.map { friendId ->
                UserDetails(userRepository!!.getUserById(friendId)!!.get(), userRepository!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    @GetMapping(path = ["/requests"])
    fun listFriendRequests(@AuthenticationPrincipal user: Any): List<FriendRequestDetails> {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val list: MutableList<FriendRequestDetails> = mutableListOf()

            friendRequestRepository!!.getAllByReceiver(currentUser).forEach {
                list.add(
                    FriendRequestDetails(it, userRepository!!)
                )
            }

            return list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @GetMapping(path = ["/check"]) // Check for friend request
    fun checkForFriendRequest(
        @AuthenticationPrincipal user: Any,
        @RequestParam(name = "handle") handle: String
    ): FriendRequestDetails? {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val selectedUser: User = userRepository!!.getUserByHandle(handle).get()

            return (friendRequestRepository!!.getFriendRequestBySenderAndReceiver(currentUser, selectedUser).get()
                ?: friendRequestRepository!!.getFriendRequestBySenderAndReceiver(selectedUser, currentUser).get())
                .let {
                return FriendRequestDetails(
                    request = it,
                    userRepo = userRepository!!
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @GetMapping(path = ["/send"])
    fun sendRequest(@RequestParam handle: String, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val friendToAdd: User = userRepository!!.getUserByHandle(handle)!!.get()

            if (
                !(friendRequestRepository!!.existsBySenderAndReceiver(
                    sender = currentUser,
                    receiver = friendToAdd
                )) &&
                !(friendRequestRepository!!.existsBySenderAndReceiver(
                    sender = friendToAdd,
                    receiver = currentUser
                ))
            ) { // making sure that the request doesn't already exist in either direction for safety checks.
                friendRequestRepository!!.save(
                    FriendRequest(
                        sender = currentUser,
                        receiver = friendToAdd
                    )
                )

                notificationRepository!!.save(
                    Notification(
                        tittle = "${currentUser.firstName} sent you a friend request",
                        body = "${currentUser.firstName} ${currentUser.lastName} (@${currentUser.handle}) wants you to be friends with them.",
                        user = friendToAdd
                    )
                )
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @GetMapping(path = ["/accept"])
    fun acceptRequest(@RequestParam id: Long, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()

            if (friendRequestRepository!!.existsById(id)) {
                val request = friendRequestRepository!!.findById(id).get()
                request.sender.friends.add(currentUser.id!!) // add current user to the other user's friend list
                currentUser.friends.add(request.sender.id!!) // add the other user to the current user's friend list

                notificationRepository!!.save(
                    Notification(
                        tittle = "${currentUser.firstName} accepted your friend request",
                        body = "${currentUser.firstName} does want to be friends with you.",
                        user = request.sender
                    )
                )

                friendRequestRepository!!.delete(request) // delete the friend request after declining.

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @GetMapping(path = ["/decline"])
    fun declineRequest(@RequestParam id: Long, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()

            if (friendRequestRepository!!.existsById(id)) {
                val request = friendRequestRepository!!.findById(id).get()

                notificationRepository!!.save(
                    Notification(
                        tittle = "${currentUser.firstName} declined your friend request",
                        body = "${currentUser.firstName} doesn't want to be friends with you.",
                        user = request.sender
                    )
                )

                friendRequestRepository!!.delete(request) // delete the friend request after accepting.

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @DeleteMapping(path = ["/rm"])
    fun removeFriend(@RequestParam handle: String, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getUserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val friendToRemove: User = userRepository!!.getUserByHandle(handle)!!.get()

            if (!currentUser.friends.contains(friendToRemove.id!!) || !friendToRemove.friends.contains(currentUser.id!!)) {
                // A safety check in case something goes wrong in the add function and the friendship state becomes inconsistent
                // This should NEVER happen.
                error("Inconsistent friendship state: one user does not have the other as a friend.")
            } else {
                // This should always run without issue, as the friendship state should be consistent.
                friendToRemove.friends.remove(currentUser.id!!) // Wipe current user from the other user's friend list
                currentUser.friends.remove(friendToRemove.id!!) // Wipe the other user from the current user's friend list
                userRepository!!.save(friendToRemove)
                userRepository!!.save(currentUser)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
