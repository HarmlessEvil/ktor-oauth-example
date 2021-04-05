package chori.itmo.ru.models

import kotlinx.serialization.Serializable

@Serializable
data class ClientSession(val accessToken: String)
