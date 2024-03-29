package org.openrs2.protocol.jaggrab

import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.DecoderException
import org.openrs2.protocol.jaggrab.upstream.JaggrabRequest
import org.openrs2.protocol.jaggrab.upstream.JaggrabRequestDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JaggrabRequestDecoderTest {
    @Test
    fun testDecode() {
        val channel = EmbeddedChannel(JaggrabRequestDecoder)
        channel.writeInbound("JAGGRAB runescape.pack200")

        val actual = channel.readInbound<JaggrabRequest>()
        assertEquals(JaggrabRequest("runescape.pack200"), actual)
    }

    @Test
    fun testInvalid() {
        val channel = EmbeddedChannel(JaggrabRequestDecoder)

        assertFailsWith<DecoderException> {
            channel.writeInbound("Hello, world!")
        }
    }
}
