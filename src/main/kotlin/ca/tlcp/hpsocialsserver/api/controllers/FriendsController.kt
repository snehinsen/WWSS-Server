package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.api.getuserID
import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/friends"])
class FriendsController {

    @Autowired
    private var userRepository: UserRepository? = null

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

    @GetMapping(path = ["/add"])
    fun addFriend(@RequestParam id: Long, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getuserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val friendToAdd: User = userRepository!!.getUserById(id)!!.get()

            friendToAdd.friends.add(currentUser.id!!) // add current user to the other user's friend list
            currentUser.friends.add(friendToAdd.id!!) // add the other user to the current user's
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @GetMapping(path = ["/remove"])
    fun removeFriend(@RequestParam id: Long, @AuthenticationPrincipal user: Any): Boolean {
        return try {
            val email = getuserID(user)
            val currentUser: User = userRepository!!.getUserByEmail(email).get()
            val friendToRemove: User = userRepository!!.getUserById(id)!!.get()

            if (!currentUser.friends.contains(friendToRemove.id!!) || !friendToRemove.friends.contains(currentUser.id!!)) {
                // A safety check in case something goes wrong in the add function and the friendship state becomes inconsistent
                // This should NEVER happen.
                error("Inconsistent friendship state: one user does not have the other as a friend.")
                false
            } else {
                // This should always run without issue, as the friendship state should be consistent.
                friendToRemove.friends.remove(currentUser.id!!) // add current user to the other user's friend list
                currentUser.friends.remove(friendToRemove.id!!) // add the other user to the current user's
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
