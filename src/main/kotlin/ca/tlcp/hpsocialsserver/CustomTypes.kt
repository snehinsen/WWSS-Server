package ca.tlcp.hpsocialsserver

import ca.tlcp.hpsocialsserver.db.User
import java.time.Instant

enum class CharacterState {
    busy, sleeping, free
}

enum class ChatType {
    DM, GC
}