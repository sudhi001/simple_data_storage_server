package `in`.sudhi.lib

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun Application.module() {
    val rootFolder = environment.config.propertyOrNull("ktor.custom.folder")?.getString() ?: "/"

    routing {
        get("/") {
            call.respond(HttpStatusCode.BadGateway, "BadGateway")
        }

        post("/upload/{userId}") {
            val userId = call.parameters["userId"]
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or malformed userId")
                return@post
            }

            val folder = File("$rootFolder/uploads/$userId").apply { mkdirs() }

            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem) {
                    val contentType = part.contentType?.toString()
                    val originalFileName = part.originalFileName!!

                    // List of file extensions and MIME types to exclude
                    val excludedExtensions = listOf("exe", "bat", "sh", "cmd", "com", "scr", "pif", "js", "vbs", "vbe", "jar", "vb", "wsf", "msi", "msp", "reg")
                    val excludedMimeTypes = listOf(
                        "application/x-msdownload",
                        "application/x-msdos-program",
                        "application/x-executable",
                        "application/x-sh",
                        "application/x-bat",
                        "application/java-archive",
                        "application/x-ms-installer",
                        "application/x-msi"
                    )

                    val fileExtension = originalFileName.substringAfterLast('.', "").lowercase()

                    // Exclude files with dangerous extensions or MIME types
                    if (fileExtension in excludedExtensions || contentType in excludedMimeTypes) {
                        // Skip this file
                        part.dispose()
                        return@forEachPart
                    }

                    val newFileName = generateFileName(originalFileName)
                    part.streamProvider().use { input ->
                        File(folder, newFileName).outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                part.dispose()
            }

            call.respond(HttpStatusCode.OK, "Received")
        }


        post("/process/{userId}/{type}") {
            val userId = call.parameters["userId"]
            val type = call.parameters["type"]
            if (userId == null || type == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or malformed userId or type")
                return@post
            }

            val request = call.receive<FolderRequest>()
            val folder = File("$rootFolder/uploads/$userId").apply { mkdirs() }

            val jsonFile = File(folder, "$type.json")
            jsonFile.writeText(Json.encodeToString(request.files))

            call.respond(HttpStatusCode.OK, "Received")
        }
    }
}

@Serializable
data class FolderRequest(val files: List<String>)

private fun generateFileName(originalFileName: String): String {
    val timestamp = System.currentTimeMillis()
    val fileExtension = originalFileName.substringAfterLast('.', "")
    return if (fileExtension.isNotEmpty()) {
        "${originalFileName.substringBeforeLast('.')}_$timestamp.$fileExtension"
    } else {
        "${originalFileName}_$timestamp"
    }
}
