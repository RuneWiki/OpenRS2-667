package org.openrs2.archive.key

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.openrs2.crypto.SymmetricKey
import org.openrs2.http.checkStatusCode
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Singleton
public class HdosKeyDownloader @Inject constructor(
    private val client: HttpClient
) : KeyDownloader(KeySource.HDOS) {
    override suspend fun getMissingUrls(seenUrls: Set<String>): Set<String> {
        return setOf(ENDPOINT)
    }

    override suspend fun download(url: String): Sequence<SymmetricKey> {
        val request = HttpRequest.newBuilder(URI(url))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()

        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).await()
        response.checkStatusCode()

        return withContext(Dispatchers.IO) {
            response.body().use { input ->
                input.bufferedReader().use { reader ->
                    val keys = mutableSetOf<SymmetricKey>()

                    for (line in reader.lineSequence()) {
                        val parts = line.split(',')
                        if (parts.size < 3) {
                            continue
                        }

                        val key = SymmetricKey.fromHexOrNull(parts[2]) ?: continue
                        keys += key
                    }

                    keys.asSequence()
                }
            }
        }
    }

    private companion object {
        private const val ENDPOINT = "https://api.hdos.dev/keys/get"
    }
}
