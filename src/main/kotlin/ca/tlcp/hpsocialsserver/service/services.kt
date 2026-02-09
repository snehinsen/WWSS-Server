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
    ) {
        if (repo.existsUserByEmail(email)) return

        println("Given Name: $givenName, Last Name: $lastName")

        val user = User(
            email = email,
            password = "",
            name = "$givenName $lastName",
            bio = "",
            pfp = null,
            isBot = false,
            isWizarding = false,
            handle = givenName!!.replace(" ", "") + lastName!!.replace(" ", "")
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
            println("password: ${user.password}")

            // For debugging: Manually check the password match
            val encoder = BCryptPasswordEncoder(12)
            val isPasswordValid = encoder.matches("Password", user.password)  // replace with actual entered password
            println("Password matches: $isPasswordValid")

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

        provisioningService.provisionUser(
            email = email,
            givenName = oauth2User.attributes["given_name"] as? String,
            lastName = oauth2User.attributes["family_name"] as? String
        )

        return oauth2User
    }
}

// For OpenID implementations if that ever becomes a part of the login sequence.
//@Service
//class CustomOidcUserService(
//    private val provisioningService: OAuthUserProvisioningService
//) : OidcUserService() {
//
//    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
//        val oidcUser = super.loadUser(userRequest)
//
//        val email = oidcUser.email
//            ?: throw IllegalStateException("OIDC user has no email")
//
//        provisioningService.provisionUser(
//            email = email,
//            givenName = oidcUser!!.givenName
//        )
//
//        return oidcUser
//    }
//}
