package ca.tlcp.hpsocialsserver.api

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class OAuthDebugResponse(
    val googleClientIdMasked: String,
    val googleClientSecretPresent: Boolean,
    val configuredRedirectUri: String,
    val serverPort: String
)

@RestController
class OAuthDebugController(private val env: Environment) {

    private fun maskClientId(id: String?): String {
        if (id.isNullOrBlank()) return "MISSING"
        val parts = id.split('@', '.', '-')
        // keep first 8 chars and last 8 chars if possible, otherwise partial mask
        return if (id.length > 16) "${id.take(8)}...${id.takeLast(8)}" else "${id.take(4)}...${id.takeLast(4)}"
    }

    @GetMapping("/debug")
    fun debug(): OAuthDebugResponse {
        val clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id")
        val secretPresent = env.getProperty("spring.security.oauth2.client.registration.google.client-secret") != null
        val configuredRedirect = env.getProperty("spring.security.oauth2.client.registration.google.redirect-uri")
            ?: "UNSET (using Spring default /login/oauth2/code/{registrationId})"
        val serverPort = env.getProperty("server.port") ?: "8080"
        return OAuthDebugResponse(
            googleClientIdMasked = maskClientId(clientId),
            googleClientSecretPresent = secretPresent,
            configuredRedirectUri = configuredRedirect,
            serverPort = serverPort
        )
    }
}
