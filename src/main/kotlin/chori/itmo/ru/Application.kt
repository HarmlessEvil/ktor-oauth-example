package chori.itmo.ru

import chori.itmo.ru.models.ClientSession
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import chori.itmo.ru.plugins.*
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import io.ktor.util.*

fun main() {
    val config = ConfigFactory.load()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()

        install(ContentNegotiation) {
            json()
        }

        val secretEncryptKey = hex(config.getString("secret.encryptKey"))
        val secretAuthKey = hex(config.getString("secret.authKey"))
        install(Sessions) {
            cookie<ClientSession>("LOGIN_SESSION", storage = SessionStorageMemory()) {
                transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretAuthKey))
            }
        }
    }.start(wait = true)
}
