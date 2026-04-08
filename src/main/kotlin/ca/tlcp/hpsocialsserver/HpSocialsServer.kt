package ca.tlcp.hpsocialsserver

import ca.tlcp.hpsocialsserver.fs.fsRoot
import io.github.cdimascio.dotenv.dotenv
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class HpSocialsServer

fun main(args: Array<String>) {

    val rootFolder = File(fsRoot)
    if (!rootFolder.exists()) {
        rootFolder.mkdir()
    }

    runApplication<HpSocialsServer>(*args)
}
