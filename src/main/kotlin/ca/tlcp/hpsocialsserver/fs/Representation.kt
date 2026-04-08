package ca.tlcp.hpsocialsserver.fs

open class Representation(
    open val label: String,
    open val value: Any
)

// label here refers to extension
class PFPRepresentation(
    override val label: String, // The file extension in this context is the label
    override val value: ByteArray
): Representation(label, value)