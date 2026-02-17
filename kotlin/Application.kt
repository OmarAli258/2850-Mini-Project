import io.ktor.server.application.*
import com.yourteam.library.configureRouting
import com.yourteam.library.configureTemplates

fun Application.module() {
    // 1) Enable template rendering (.peb)
    configureTemplates()

    // 2) Add routes (/ , /login , /book/{id} etc.)
    configureRouting()
}


