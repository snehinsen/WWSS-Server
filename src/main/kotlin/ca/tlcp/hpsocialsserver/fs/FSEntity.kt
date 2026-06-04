package ca.tlcp.hpsocialsserver.fs

import java.io.File

const val fsRoot: String = "data"

interface FSEntity {
    fun getObject(label: String): Any
    fun saveObject(label: String, entity: Representation)
}

class PFPEntity: FSEntity {
    private final val PFPRoot: String = "${fsRoot}/pfp"

    constructor() {
        val rootFolder: File = File(PFPRoot)
        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }
    }

    override fun getObject(label: String): File {
        val dir = File(PFPRoot)

        val match = dir.listFiles()?.firstOrNull {
            it.name.startsWith("$label-pfp.")
        }

        return match ?: throw RuntimeException("PFP not found")
    }

    override fun saveObject(label: String, entity: Representation) {
        val tmp = entity as PFPRepresentation

        val saved = File(PFPRoot, "${label}-pfp.${entity.label}")

        // clear old file
        saved.parentFile.listFiles()?.forEach {
            it.delete()
        }

        saved.writeBytes(tmp.value )
    }
}

class LLMSystemEntity: FSEntity {
    private val AIRoot: String = "${fsRoot}/ai"

    constructor() {
        val rootFolder: File = File(AIRoot)
        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }
    }

    override fun getObject(label: String): File {
        val dir = File(AIRoot)

        val match = dir.listFiles()?.firstOrNull {
            it.name.contains("${label}/system.prompt")
        }

        return match ?: throw RuntimeException("Model Prompt file not found")
    }

    override fun saveObject(label: String, entity: Representation) {
        val tmp = entity as PFPRepresentation

        val saved = File(AIRoot, "${label}-pfp.${entity.label}")

        // clear old file
        saved.parentFile.listFiles()?.forEach {
            it.delete()
        }

        saved.writeBytes(tmp.value )
    }
}

class LLMTaskingEntity: FSEntity {
    private val AIRoot: String = "${fsRoot}/ai"

    constructor() {
        val rootFolder: File = File(AIRoot)
        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }
    }

    override fun getObject(label: String): File {
        val dir = File(AIRoot+"/${label}")
        val files = dir.listFiles()

        println(files.map {
            it.name
        })

        val match = files?.firstOrNull {
            it.name.contains("tasking.prompt")
        }
        println(match)
        return match ?: throw RuntimeException("Model Prompt file not found")
    }

    override fun saveObject(label: String, entity: Representation) {
        val tmp = entity as PFPRepresentation

        val saved = File(AIRoot, "${label}-pfp.${entity.label}")

        // clear old file
        saved.parentFile.listFiles()?.forEach {
            it.delete()
        }

        saved.writeBytes(tmp.value )
    }
}


class ComfyGraphEntity: FSEntity {
    private val ComfyGraphRoot: String = "tools/IMGGen/"

    constructor() {
        val rootFolder: File = File(ComfyGraphRoot)
        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }
    }

    override fun getObject(label: String): File {
        val dir = File(ComfyGraphRoot)

        val match = dir.listFiles()?.firstOrNull {
            it.name.contains("${label}.json")
        }

        return match ?: throw RuntimeException("Graph file not found in location \"${dir.absolutePath}\"")
    }

    // This function isn't used because this has no use, but I set the staderd
    override fun saveObject(label: String, entity: Representation) {

    }
}