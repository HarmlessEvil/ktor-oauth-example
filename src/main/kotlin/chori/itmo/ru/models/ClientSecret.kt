package chori.itmo.ru.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientWebSecret(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("project_id")
    val projectId: String,
    @SerialName("auth_uri")
    val authUri: String,
    @SerialName("token_uri")
    val tokenUri: String,
    @SerialName("auth_provider_x509_cert_url")
    val authProviderX509CertUrl: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("redirect_uris")
    val redirectUris: Array<String>,
    @SerialName("javascript_origins")
    val javascriptOrigins: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientWebSecret

        if (clientId != other.clientId) return false
        if (projectId != other.projectId) return false
        if (authUri != other.authUri) return false
        if (tokenUri != other.tokenUri) return false
        if (authProviderX509CertUrl != other.authProviderX509CertUrl) return false
        if (clientSecret != other.clientSecret) return false
        if (!redirectUris.contentEquals(other.redirectUris)) return false
        if (!javascriptOrigins.contentEquals(other.javascriptOrigins)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientId.hashCode()
        result = 31 * result + projectId.hashCode()
        result = 31 * result + authUri.hashCode()
        result = 31 * result + tokenUri.hashCode()
        result = 31 * result + authProviderX509CertUrl.hashCode()
        result = 31 * result + clientSecret.hashCode()
        result = 31 * result + redirectUris.contentHashCode()
        result = 31 * result + javascriptOrigins.contentHashCode()
        return result
    }
}

@Serializable
data class ClientSecret(val web: ClientWebSecret)
