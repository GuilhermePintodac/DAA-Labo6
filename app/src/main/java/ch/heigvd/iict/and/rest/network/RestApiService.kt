package ch.heigvd.iict.and.rest.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object RestApiService {

    /**
     * Effectue une requête GET vers un endpoint.
     *
     * @param endpoint L'URL de l'API (par exemple, "/enroll").
     * @param headers Les en-têtes HTTP à inclure.
     * @return La réponse sous forme de chaîne.
     */
    fun get(endpoint: String, headers: Map<String, String> = emptyMap()): String {
        val url = URL("https://daa.iict.ch$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Configurer la connexion
            connection.requestMethod = "GET"
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

            // Vérifier le code de réponse
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Erreur HTTP: $responseCode")
            }

            // Lire la réponse
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            return response
        } finally {
            connection.disconnect()
        }
    }
}
