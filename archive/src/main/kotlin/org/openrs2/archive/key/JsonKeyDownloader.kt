package org.openrs2.archive.key

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

public abstract class JsonKeyDownloader(
    source: KeySource,
    private val client: HttpClient,
    private val jsonKeyReader: JsonKeyReader
) : KeyDownloader(source) {
    override suspend fun download(url: String): Sequence<SymmetricKey> {
        val request = HttpRequest.newBuilder(URI(url))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()

        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).await()
        response.checkStatusCode()

        return withContext(Dispatchers.IO) {
            response.body().use { input ->
                jsonKeyReader.read(input)
            }
        }
    }
}
