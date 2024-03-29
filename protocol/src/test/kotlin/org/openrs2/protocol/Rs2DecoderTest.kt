package org.openrs2.protocol

import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.DecoderException
import org.openrs2.buffer.wrappedBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Rs2DecoderTest {
    @Test
    fun testDecode() {
        testDecode(byteArrayOf(0, 0x11, 0x22, 0x33, 0x44), FixedPacket(0x11223344))
        testDecode(byteArrayOf(1, 3, 0x11, 0x22, 0x33), VariableBytePacket(byteArrayOf(0x11, 0x22, 0x33)))
        testDecode(byteArrayOf(2, 0, 3, 0x11, 0x22, 0x33), VariableShortPacket(byteArrayOf(0x11, 0x22, 0x33)))
        testDecode(byteArrayOf(5), EmptyPacket)
    }

    @Test
    fun testFragmented() {
        testFragmented(byteArrayOf(0, 0x11, 0x22, 0x33, 0x44), FixedPacket(0x11223344))
        testFragmented(byteArrayOf(1, 3, 0x11, 0x22, 0x33), VariableBytePacket(byteArrayOf(0x11, 0x22, 0x33)))
        testFragmented(byteArrayOf(2, 0, 3, 0x11, 0x22, 0x33), VariableShortPacket(byteArrayOf(0x11, 0x22, 0x33)))
    }

    @Test
    fun testUnsupported() {
        val channel = EmbeddedChannel(Rs2Decoder(Protocol()))

        assertFailsWith<DecoderException> {
            channel.writeInbound(wrappedBuffer(0))
        }
    }

    @Test
    fun testEncryptedOpcode() {
        val decoder = Rs2Decoder(Protocol(TestFixedPacketCodec))
        decoder.cipher = TestStreamCipher

        val channel = EmbeddedChannel(decoder)
        channel.writeInbound(wrappedBuffer(10, 0x11, 0x22, 0x33, 0x44))

        val actual = channel.readInbound<Packet>()
        assertEquals(FixedPacket(0x11223344), actual)
    }

    @Test
    fun testSwitchProtocol() {
        val decoder = Rs2Decoder(Protocol(TestFixedPacketCodec))
        val channel = EmbeddedChannel(decoder)

        channel.writeInbound(wrappedBuffer(0, 0x11, 0x22, 0x33, 0x44))
        channel.readInbound<Packet>()

        assertFailsWith<DecoderException> {
            channel.writeInbound(wrappedBuffer(5))
        }

        decoder.protocol = Protocol(TestEmptyPacketCodec)

        channel.writeInbound(wrappedBuffer(5))

        val actual = channel.readInbound<Packet>()
        assertEquals(EmptyPacket, actual)

        assertFailsWith<DecoderException> {
            channel.writeInbound(wrappedBuffer(0, 0x11, 0x22, 0x33, 0x44))
        }
    }

    @Test
    fun testTrailingBytes() {
        val decoder = Rs2Decoder(Protocol(LengthMismatchPacketCodec))
        val channel = EmbeddedChannel(decoder)

        assertFailsWith<DecoderException> {
            channel.writeInbound(wrappedBuffer(0, 0x11, 0x22, 0x33, 0x44, 0x55))
        }
    }

    private fun testDecode(buf: ByteArray, expected: Packet) {
        val channel = EmbeddedChannel(
            Rs2Decoder(
                Protocol(
                    TestFixedPacketCodec,
                    TestVariableBytePacketCodec,
                    TestVariableShortPacketCodec,
                    VariableByteOptimisedPacketCodec,
                    VariableShortOptimisedPacketCodec,
                    TestEmptyPacketCodec
                )
            )
        )
        channel.writeInbound(Unpooled.wrappedBuffer(buf))

        val actual = channel.readInbound<Packet>()
        assertEquals(expected, actual)
    }

    private fun testFragmented(buf: ByteArray, expected: Packet) {
        val channel = EmbeddedChannel(
            Rs2Decoder(
                Protocol(
                    TestFixedPacketCodec,
                    TestVariableBytePacketCodec,
                    TestVariableShortPacketCodec,
                    VariableByteOptimisedPacketCodec,
                    VariableShortOptimisedPacketCodec
                )
            )
        )

        for (b in buf) {
            channel.writeInbound(wrappedBuffer(b))
        }

        val actual = channel.readInbound<Packet>()
        assertEquals(expected, actual)
    }
}
