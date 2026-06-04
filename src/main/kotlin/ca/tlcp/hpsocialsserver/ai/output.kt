package ca.tlcp.hpsocialsserver.ai

data class ImageResult(
    val filename: String,
    val description: String,
    val people: List<String>
)