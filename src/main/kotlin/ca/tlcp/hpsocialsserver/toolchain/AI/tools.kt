package ca.tlcp.hpsocialsserver.toolchain.AI

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

object ComfyImageTool {

    private val mapper = jacksonObjectMapper()

    private val client = HttpClient(CIO)

    private const val COMFY_URL = "http://127.0.0.1:8188"

    private const val WORKFLOW_FILE = "IMGGenFlow.json"

    suspend fun generateImage(prompt: String): ByteArray {

        val workflow = loadWorkflow()

        populateWorkflow(
            workflow,
            prompt,
            Random.nextLong()
        )

        val promptId = queuePrompt(workflow)

        val imageMeta = awaitImage(promptId)

        return downloadImage(
            imageMeta.filename,
            imageMeta.subfolder,
            imageMeta.type
        )
    }

    private fun loadWorkflow(): ObjectNode {
        val file = File(WORKFLOW_FILE)
        return mapper.readTree(file) as ObjectNode
    }

    private fun populateWorkflow(
        workflow: ObjectNode,
        prompt: String,
        seed: Long
    ) {

        workflow
            .with("57:27")
            .with("inputs")
            .put("text", prompt)

        workflow
            .with("57:3")
            .with("inputs")
            .put("seed", seed)
    }

    private suspend fun queuePrompt(workflow: ObjectNode): String {

        val payload = mapper.createObjectNode()
        payload.set<ObjectNode>("prompt", workflow)

        val response = client.post("$COMFY_URL/prompt") {
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }

        val json = mapper.readTree(response.bodyAsText())

        return json["prompt_id"].asText()
    }

    private suspend fun awaitImage(promptId: String): ImageMeta {

        while (true) {

            val response = client.get("$COMFY_URL/history/$promptId")

            val json = mapper.readTree(response.bodyAsText())

            if (json.has(promptId)) {

                val outputs = json[promptId]["outputs"]

                val image = outputs["9"]["images"][0]

                return ImageMeta(
                    image["filename"].asText(),
                    image["subfolder"].asText(),
                    image["type"].asText()
                )
            }

            delay(1000)
        }
    }

    private suspend fun downloadImage(
        filename: String,
        subfolder: String,
        type: String
    ): ByteArray {

        val response = client.get("$COMFY_URL/view") {

            parameter("filename", filename)
            parameter("subfolder", subfolder)
            parameter("type", type)

        }

        return response.readBytes()
    }

    private data class ImageMeta(
        val filename: String,
        val subfolder: String,
        val type: String
    )
}