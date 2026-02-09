package ca.tlcp.hpsocialsserver

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HpSocialsServer

fun main(args: Array<String>) {
    val dotenv = dotenv()

    // Print out the environment variables to verify if they are being loaded
//    println("GOOGLE_CLIENT_ID: ${dotenv["GOOGLE_CLIENT_ID"]}")
//    println("GOOGLE_CLIENT_SECRET: ${dotenv["GOOGLE_CLIENT_SECRET"]}")
//    println("FACEBOOK_CLIENT_ID: ${dotenv["FACEBOOK_CLIENT_ID"]}")
//    println("FACEBOOK_CLIENT_SECRET: ${dotenv["FACEBOOK_CLIENT_SECRET"]}")
    runApplication<HpSocialsServer>(*args)
}
