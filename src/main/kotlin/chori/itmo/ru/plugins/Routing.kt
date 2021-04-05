package chori.itmo.ru.plugins

import chori.itmo.ru.models.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun PipelineContext<Unit, ApplicationCall>.authenticatedUser(client: HttpClient): ClientOauthName? {
    val session = call.sessions.get<ClientSession>() ?: return null
    return try {
        val profile = client.get<ClientOauthName>("https://www.googleapis.com/oauth2/v2/userinfo") {
            header("Authorization", "Bearer ${session.accessToken}")
        }

        profile
    } catch (e: ResponseException) {
        // This may fail if token is expired -> don't need to worry about forgetting user manually
        // Downside: it makes HTTP requests too often
        // Conclusion: it should fit to development needs, but not production

        call.sessions.clear<ClientSession>()
        null
    }
}

fun Application.configureRouting() {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }
    val secret = Json.decodeFromString<ClientSecret>(
        javaClass.getResourceAsStream("/client_secret.json").readAllBytes().decodeToString()
    ).web

    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondRedirect("/home", permanent = true)
        }

        get("/home") {
            val clientOauthName = authenticatedUser(client)
            val isAuthenticated = clientOauthName != null
            val userName = clientOauthName?.name ?: "new user"

            call.respondHtml {
                body {
                    h1 {
                        +"Welcome ${userName}! This is a home page"
                    }
                    if (!isAuthenticated) {
                        a(href = "/signin") { +"Sign In" }
                    } else {
                        a(href = "/signout") { +"Sign Out" }
                    }
                }
            }
        }

        get("/signin") {
            authenticatedUser(client)?.let{
                call.respondHtml {
                    body {
                        h1 {
                            +"You are already signed in as ${it.name}"
                        }
                        a(href = "/home") { +"Go Home" }
                    }
                }
                return@get
            }

            val oauthUrl = URLBuilder("https://accounts.google.com/o/oauth2/v2/auth")
            oauthUrl.parameters.apply {
                append("client_id", secret.clientId)
                append("redirect_uri", secret.redirectUris.first { it.endsWith("/oauth") })
                append("response_type", "code")
                append(
                    "scope",
                    "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid"
                )
                append("access_type", "online")
            }

            call.respondHtml {
                body {
                    a(href = oauthUrl.buildString()) { +"Sign In using Google Account" }
                }
            }
        }

        get("/signout") {
            call.sessions.clear<ClientSession>()
            call.respondRedirect("/home")
        }

        get("/oauth") {
            call.parameters["error"]?.let {
                call.respondText(it)
                return@get
            }

            val exchangeUrl = URLBuilder("https://oauth2.googleapis.com/token")
            exchangeUrl.parameters.apply {
                append("client_id", secret.clientId)
                append("client_secret", secret.clientSecret)
                append("code", call.parameters["code"]!!) // Error == null => code != null
                append("grant_type", "authorization_code")
                append("redirect_uri", secret.redirectUris.first { it.endsWith("/oauth") })
            }

            val token = client.post<ClientToken>(exchangeUrl.buildString())
            call.sessions.set(ClientSession(token.accessToken))

            call.respondRedirect("/home")
        }
    }
}
