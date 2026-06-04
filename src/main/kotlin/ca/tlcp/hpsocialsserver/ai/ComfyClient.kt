package ca.tlcp.hpsocialsserver.ai

import ca.tlcp.hpsocialsserver.fs.ComfyGraphEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.FileReader

@Component
class ComfyClient {

    private val restClient = RestClient.builder()
        .baseUrl("http://localhost:8000")
        .build()

    fun generateImage(prompt: String, people: List<String>): String {

        val workflow: String = FileReader(ComfyGraphEntity().getObject("image")).readText()
        val mapper = jacksonObjectMapper()

        val workflowJson: Map<String, Any> = mapper.readValue(
            workflow
                .replace("{{PROMPT}}", prompt)
                .replace("{{PEOPLE}}", people.joinToString(", "))
        )

        val request = mapOf(
            "prompt" to workflowJson,
            "client_id" to "spring-server"
        )

        val response = restClient.post()
            .uri("prompt")
            .body(request)
            .retrieve()
            .body(Map::class.java)
            ?: throw RuntimeException("No response from ComfyUI")

        val promptId = response["prompt_id"]?.toString()
            ?: throw RuntimeException("Missing prompt_id")

        println("ComfyUI queued: $promptId")

        // BLOCK until finished (polling)
        repeat(120) {
            Thread.sleep(1000)

            val history = restClient.get()
                .uri("/history/$promptId")
                .retrieve()
                .body(Map::class.java)

            val job = history?.get(promptId)
            if (job != null) {
                val outputs = (job as Map<*, *>)["outputs"] as? Map<*, *> ?: return@repeat

                outputs.values.forEach { node ->
                    val nodeMap = node as Map<*, *>
                    val images = nodeMap["images"] as? List<*>

                    if (!images.isNullOrEmpty()) {
                        val image = images[0] as Map<*, *>
                        val filename = image["filename"]?.toString()

                        if (filename != null) {
                            println("ComfyUI done: $filename")
                            return "https://comfyui.sanjaysen.me/view?filename=$filename"
                        }
                    }
                }
            }
        }

        throw RuntimeException("ComfyUI timeout")
    }
}