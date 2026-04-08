package ca.tlcp.hpsocialsserver.fs

import java.io.File

const val fsRoot: String = "data"

interface FSEntity {
    fun getObject(label: String): Any
    fun saveObject(label: String, entity: Representation)
}

class PFPEntity: FSEntity {
    private final val objectRoot: String = "${fsRoot}/pfp"

    constructor() {
        val rootFolder: File = File(objectRoot)
        if (!rootFolder.exists()) {
            rootFolder.mkdir()
        }
    }

    override fun getObject(label: String): File {
        val dir = File(objectRoot)

        val match = dir.listFiles()?.firstOrNull {
            it.name.startsWith("$label-pfp.")
        }

        return match ?: throw RuntimeException("PFP not found")
    }

    override fun saveObject(label: String, entity: Representation) {
        val tmp = entity as PFPRepresentation

        val saved = File(objectRoot, "${label}-pfp.${entity.label}")

        // clear old file
        saved.parentFile.listFiles()?.forEach {
            it.delete()
        }

        saved.writeBytes(tmp.value )
    }
}