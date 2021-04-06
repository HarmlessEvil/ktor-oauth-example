package chori.itmo.ru

import chori.itmo.ru.models.ClientSession
import chori.itmo.ru.plugins.configureRouting
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.sessions.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    internal fun testNotLoggedIn() = withTestApplication(moduleFunction = { configureRouting() }) {
        application.install(Sessions) {
            cookie<ClientSession>("LOGIN_SESSION")
        }

        cookiesSession {
            handleRequest(HttpMethod.Get, "/home").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertThat(response.content, StringContains("new user"))
                assertThat(response.content, StringContains("Sign In"))
            }
        }
    }

    @Test
    internal fun testNotLoggedInSignIn() = withTestApplication(moduleFunction = { configureRouting() }) {
        application.install(Sessions) {
            cookie<ClientSession>("LOGIN_SESSION")
        }

        cookiesSession {
            handleRequest(HttpMethod.Get, "/signin").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertThat(response.content, StringContains("Sign In using Google Account"))
            }
        }
    }
}