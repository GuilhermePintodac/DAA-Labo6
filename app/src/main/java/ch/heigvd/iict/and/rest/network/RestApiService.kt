/*
Auteur :  Shyshmarov Alexandre / Guilherme Pinto
 */
package ch.heigvd.iict.and.rest.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
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


            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                throw Exception("Erreur HTTP: $responseCode, Message: $errorMessage")
            }


            // Lire la réponse
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            return response
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Effectue une requête POST vers un endpoint.
     *
     * @param endpoint L'URL de l'API (par exemple, "/contacts").
     * @param headers Les en-têtes HTTP à inclure.
     * @param payload Les données JSON à envoyer dans le corps de la requête.
     * @return La réponse sous forme de chaîne.
     */
    fun post(endpoint: String, headers: Map<String, String> = emptyMap(), payload: String): String {
        val url = URL("https://daa.iict.ch$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Configurer la connexion
            connection.requestMethod = "POST"
            connection.doOutput = true
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            connection.setRequestProperty("Content-Type", "application/json")

            // Écrire le corps de la requête
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(payload.toByteArray())
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                throw Exception("Erreur HTTP: $responseCode, Message: $errorMessage")
            }

            // Lire la réponse
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            return response
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Effectue une requête PUT vers un endpoint.
     *
     * @param endpoint L'URL de l'API (par exemple, "/contacts/34").
     * @param headers Les en-têtes HTTP à inclure.
     * @param payload Les données JSON à envoyer dans le corps de la requête.
     * @return La réponse sous forme de chaîne.
     */
    fun put(endpoint: String, headers: Map<String, String> = emptyMap(), payload: String): String {
        val url = URL("https://daa.iict.ch$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Configurer la connexion
            connection.requestMethod = "PUT"
            connection.doOutput = true
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            connection.setRequestProperty("Content-Type", "application/json")

            // Écrire le corps de la requête
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(payload.toByteArray())
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                throw Exception("Erreur HTTP: $responseCode, Message: $errorMessage")
            }

            // Lire la réponse
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            return response
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Effectue une requête DELETE vers un endpoint.
     *
     * @param endpoint L'URL de l'API (par exemple, "/contacts/34").
     * @param headers Les en-têtes HTTP à inclure.
     * @return La réponse sous forme de chaîne.
     */
    fun delete(endpoint: String, headers: Map<String, String> = emptyMap()): String {
        val url = URL("https://daa.iict.ch$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Configurer la connexion
            connection.requestMethod = "DELETE"
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                throw Exception("Erreur HTTP: $responseCode, Message: $errorMessage")
            }

            // Lire la réponse (s'il y en a une)
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            return response
        } finally {
            connection.disconnect()
        }
    }
}
