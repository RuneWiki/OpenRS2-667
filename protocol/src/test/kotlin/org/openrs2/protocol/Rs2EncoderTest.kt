package org.openrs2.protocol

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.EncoderException
import org.openrs2.buffer.use
import org.openrs2.buffer.wrappedBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Rs2EncoderTest {
    @Test
    fun testEncode() {
        testEncode(FixedPacket(0x11223344), byteArrayOf(0, 0x11, 0x22, 0x33, 0x44))
        testEncode(VariableBytePacket(byteArrayOf(0x11, 0x22, 0x33)), byteArrayOf(1, 3, 0x11, 0x22, 0x33))
        testEncode(VariableShortPacket(byteArrayOf(0x11, 0x22, 0x33)), byteArrayOf(2, 0, 3, 0x11, 0x22, 0x33))
        testEncode(EmptyPacket, byteArrayOf(5))
    }

    @Test
    fun testTooLong() {
        val channel = EmbeddedChannel(
            Rs2Encoder(
                Protocol(
                    TestVariableBytePacketCodec,
                    TestVariableShortPacketCodec
                )
            )
        )

        channel.writeOutbound(VariableShortPacket(ByteArray(255)))
        channel.readOutbound<ByteBuf>().release()

        channel.writeOutbound(VariableShortPacket(ByteArray(65535)))
        channel.readOutbound<ByteBuf>().release()

        assertFailsWith<EncoderException> {
            channel.writeOutbound(VariableBytePacket(ByteArray(256)))
        }

        assertFailsWith<EncoderException> {
            channel.writeOutbound(VariableShortPacket(ByteArray(65536)))
        }
    }

    @Test
    fun testUnsupported() {
        val channel = EmbeddedChannel(Rs2Encoder(Protocol()))

        assertFailsWith<EncoderException> {
            channel.writeOutbound(FixedPacket(0x11223344))
        }
    }

    @Test
    fun testLengthMismatch() {
        val channel = EmbeddedChannel(Rs2Encoder(Protocol(LengthMismatchPacketCodec)))

        assertFailsWith<EncoderException> {
            channel.writeOutbound(FixedPacket(0x11223344))
        }
    }

    @Test
    fun testLengthOptimised() {
        testEncode(VariableByteOptimisedPacket(byteArrayOf(0x11, 0x22, 0x33)), byteArrayOf(3, 3, 0x11, 0x22, 0x33))
        testEncode(VariableShortOptimisedPacket(byteArrayOf(0x11, 0x22, 0x33)), byteArrayOf(4, 0, 3, 0x11, 0x22, 0x33))
    }

    @Test
    fun testEncryptedOpcode() {
        val encoder = Rs2Encoder(Protocol(TestFixedPacketCodec))
        encoder.cipher = TestStreamCipher

        val channel = EmbeddedChannel(encoder)
        channel.writeOutbound(FixedPacket(0x11223344))

        channel.readOutbound<ByteBuf>().use { actual ->
            wrappedBuffer(10, 0x11, 0x22, 0x33, 0x44).use { expected ->
                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun testSwitchProtocol() {
        val encoder = Rs2Encoder(Protocol(TestFixedPacketCodec))
        val channel = EmbeddedChannel(encoder)

        channel.writeOutbound(FixedPacket(0x11223344))
        channel.readOutbound<ByteBuf>().release()

        assertFailsWith<EncoderException> {
            channel.writeOutbound(EmptyPacket)
        }

        encoder.protocol = Protocol(TestEmptyPacketCodec)

        channel.writeOutbound(EmptyPacket)

        channel.readOutbound<ByteBuf>().use { actual ->
            wrappedBuffer(5).use { expected ->
                assertEquals(expected, actual)
            }
        }

        assertFailsWith<EncoderException> {
            channel.writeOutbound(FixedPacket(0x11223344))
        }
    }

    private fun testEncode(packet: Packet, expected: ByteArray) {
        val channel = EmbeddedChannel(
            Rs2Encoder(
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
        channel.writeOutbound(packet)

        channel.readOutbound<ByteBuf>().use { actual ->
            Unpooled.wrappedBuffer(expected).use { expected ->
                assertEquals(expected, actual)
            }
        }
    }
}
