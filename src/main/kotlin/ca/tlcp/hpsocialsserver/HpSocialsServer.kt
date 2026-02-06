package ca.tlcp.hpsocialsserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HpSocialsServer

fun main(args: Array<String>) {
    runApplication<HpSocialsServer>(*args)
}
