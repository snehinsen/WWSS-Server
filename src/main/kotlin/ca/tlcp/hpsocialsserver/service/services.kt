package ca.tlcp.hpsocialsserver.service

import ca.tlcp.hpsocialsserver.db.User
import ca.tlcp.hpsocialsserver.db.UserRepository
import ca.tlcp.hpsocialsserver.toolchain.toUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class OAuthUserProvisioningService(
    private val repo: UserRepository
) {

    fun provisionUser(
        email: String,
        givenName: String?,
        lastName: String?,
        pfp: String?
    ) {
        if (repo.existsUserByEmail(email)) return

        println("Given Name: $givenName, Last Name: $lastName")

        val safeGiven = givenName?.replace(" ", "")?.takeIf { it.isNotBlank() }
        val safeLast = lastName?.replace(" ", "")?.takeIf { it.isNotBlank() } ?: ""

        val fallback = email.substringBefore("@")
        val handle = ((safeGiven ?: fallback) + safeLast).lowercase()

        val user = User(
            email = email,
            password = "oauth2",
            firstName = safeGiven,
            lastName = safeLast,
            bio = "",
            pfp = pfp,
            isBot = false,
            isWizarding = false,
            handle = handle
        )

        repo.save(user)
        println("✅ Created user via OAuth/OIDC: $email")
    }
}


@Service
class CustomUserDetailsService(val repo: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(usernameOrEmail: String): UserDetails {

        if (repo.existsUserByEmail(usernameOrEmail)) {
            val user = repo.getUserByEmail(usernameOrEmail).get().toUserDetails()
            println("User found with email: $usernameOrEmail")
          return user

        } else {
            throw UsernameNotFoundException("User not found: $usernameOrEmail")
        }
    }
}

@Service
class CustomOAuth2UserService(
    private val provisioningService: OAuthUserProvisioningService
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)

        val email = oauth2User.attributes["email"] as String
        println(oauth2User.attributes)
        provisioningService.provisionUser(
            email = email,
            givenName = oauth2User.attributes["given_name"] as? String,
            lastName = oauth2User.attributes["family_name"] as? String,
            pfp = oauth2User.attributes["picture"] as? String,
        )

        return oauth2User
    }
}
