package ca.tlcp.hpsocialsserver.config

import ca.tlcp.hpsocialsserver.service.CustomOAuth2UserService
import ca.tlcp.hpsocialsserver.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
) {


    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/assets/**",
                "/favicon.ico",
            )
        }
    }


    @Bean
    fun oauth2LoginAuthenticationSuccessHandler(): AuthenticationSuccessHandler {
        return SimpleUrlAuthenticationSuccessHandler("/app/feed")  // Redirect to the feed after successful login
    }

    @Bean
    fun oauth2LoginAuthenticationFailureHandler(): AuthenticationFailureHandler {
        return SimpleUrlAuthenticationFailureHandler("/app/login?error=true")  // Handle failure
    }

    @Bean
    fun authProvider(customUserDetailsService: CustomUserDetailsService): AuthenticationProvider {
        val provider = DaoAuthenticationProvider(customUserDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder(12)

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3002",
            "http://localhost:8589",
            "http://localhost:5173"
        )
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }


    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        customOAuth2UserService: CustomOAuth2UserService
    ): SecurityFilterChain {
        println("Configuring OAuth2 login and checking flow...")

        http
            .securityMatcher("/**")
            .cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .sessionManagement { sm ->
                sm
                    .sessionCreationPolicy(
                        SessionCreationPolicy
                            .IF_REQUIRED
                    )
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/oauth2",
                        "/oauth2/**"
                    ).permitAll()
                    .requestMatchers(
                        "/",
                        "/app",
                        "/app/privacy",
                        "/app/tos",
                        "/api/signup",
                        "/login",
                        "/app/login",
                        "/app/login/**",
                        "/assets/**",   // 👈 THIS
                        "/static/**",
                        "/favicon.ico",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.png",
                        "/**/*.svg",
                        "/**/*.html",
                        "/favicon.ico"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            // For frontend dev only for now.
            .httpBasic(Customizer.withDefaults())
            .formLogin { login ->
                login
                    .loginPage("/app/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/app/feed", true)
                    .failureUrl("/app/login?error=true")
                    .permitAll()
            }
            .oauth2Login { login ->
                println("Configuring OAuth2 login...")
                login
                    .loginPage("/app/login")
                    .authorizationEndpoint { endpoint ->
                        endpoint.baseUri("/oauth2")
                        println("OAuth2 authorization endpoint configured.")
                    }
                    .successHandler(oauth2LoginAuthenticationSuccessHandler())
                    .failureHandler(oauth2LoginAuthenticationFailureHandler())
                    .userInfoEndpoint { userInfoEndpoint ->
                        userInfoEndpoint
                            .userService(customOAuth2UserService)
                        println("UserInfo endpoint set with CustomOAuth2UserService.")
                    }
                    .redirectionEndpoint { redirect ->
                        redirect.baseUri("/oauth2/code/**")
                    }
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/app/login?logout=true")
                    .permitAll()
            }

        return http.build()
    }

}
