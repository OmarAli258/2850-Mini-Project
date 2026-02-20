package your.package.name

import io.ktor.server.application.*
import io.ktor.server.pebble.*

fun Application.configureTemplates() {
    install(Pebble) {
        // Default looks in: src/main/resources/templates
    }
}
