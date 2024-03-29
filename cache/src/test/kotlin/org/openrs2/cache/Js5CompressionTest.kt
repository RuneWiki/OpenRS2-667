package org.openrs2.cache

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.openrs2.buffer.copiedBuffer
import org.openrs2.buffer.use
import org.openrs2.crypto.SymmetricKey
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Js5CompressionTest {
    @Test
    fun testCompressNone() {
        read("none.dat").use { expected ->
            copiedBuffer("OpenRS2").use { input ->
                Js5Compression.compress(input, Js5CompressionType.UNCOMPRESSED).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressNone() {
        copiedBuffer("OpenRS2").use { expected ->
            read("none.dat").use { input ->
                Js5Compression.uncompress(input).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressGzip() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.GZIP).use { compressed ->
                Js5Compression.uncompress(compressed).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressGzip() {
        copiedBuffer("OpenRS2").use { expected ->
            read("gzip.dat").use { input ->
                Js5Compression.uncompress(input).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressLargeGzip() {
        read("gzip-large.dat").use { input ->
            Js5Compression.uncompress(input).use { actual ->
                read("large.dat").use { expected ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressBzip2() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.BZIP2).use { compressed ->
                Js5Compression.uncompress(compressed).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressBzip2() {
        copiedBuffer("OpenRS2").use { expected ->
            read("bzip2.dat").use { input ->
                Js5Compression.uncompress(input).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressLzma() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.LZMA).use { compressed ->
                Js5Compression.uncompress(compressed).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressLzma() {
        copiedBuffer("OpenRS2").use { expected ->
            read("lzma.dat").use { input ->
                Js5Compression.uncompress(input).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressNoneEncrypted() {
        read("none-encrypted.dat").use { expected ->
            copiedBuffer("OpenRS2".repeat(3)).use { input ->
                Js5Compression.compress(input, Js5CompressionType.UNCOMPRESSED, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressNoneEncrypted() {
        copiedBuffer("OpenRS2".repeat(3)).use { expected ->
            read("none-encrypted.dat").use { input ->
                Js5Compression.uncompress(input, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressGzipEncrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.GZIP, KEY).use { compressed ->
                Js5Compression.uncompress(compressed, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressGzipEncrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            read("gzip-encrypted.dat").use { input ->
                Js5Compression.uncompress(input, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressBzip2Encrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.BZIP2, KEY).use { compressed ->
                Js5Compression.uncompress(compressed, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressBzip2Encrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            read("bzip2-encrypted.dat").use { input ->
                Js5Compression.uncompress(input, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressLzmaEncrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            Js5Compression.compress(expected.slice(), Js5CompressionType.LZMA, KEY).use { compressed ->
                Js5Compression.uncompress(compressed, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressLzmaEncrypted() {
        copiedBuffer("OpenRS2").use { expected ->
            read("lzma-encrypted.dat").use { input ->
                Js5Compression.uncompress(input, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressBest() {
        copiedBuffer("OpenRS2".repeat(100)).use { expected ->
            val noneLen = Js5Compression.compress(expected.slice(), Js5CompressionType.UNCOMPRESSED).use { compressed ->
                compressed.readableBytes()
            }

            Js5Compression.compressBest(expected.slice()).use { compressed ->
                assertNotEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
                assert(compressed.readableBytes() < noneLen)

                Js5Compression.uncompress(compressed).use { actual ->
                    assertEquals(expected, actual)
                }
            }

            Js5Compression.compressBest(expected.slice(), enableLzma = true).use { compressed ->
                assertNotEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
                assert(compressed.readableBytes() < noneLen)

                Js5Compression.uncompress(compressed).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testCompressBestEncrypted() {
        copiedBuffer("OpenRS2".repeat(100)).use { expected ->
            val noneLen = Js5Compression.compress(expected.slice(), Js5CompressionType.UNCOMPRESSED).use { compressed ->
                compressed.readableBytes()
            }

            Js5Compression.compressBest(expected.slice(), key = KEY).use { compressed ->
                assertNotEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
                assert(compressed.readableBytes() < noneLen)

                Js5Compression.uncompress(compressed, KEY).use { actual ->
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @Test
    fun testUncompressedEncryption() {
        copiedBuffer("OpenRS2").use { buf ->
            Js5Compression.compressBest(buf.slice()).use { compressed ->
                assertEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
            }

            Js5Compression.compressBest(buf.slice(), enableUncompressedEncryption = true).use { compressed ->
                assertEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
            }

            Js5Compression.compressBest(buf.slice(), key = KEY).use { compressed ->
                assertNotEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
            }

            Js5Compression.compressBest(buf.slice(), key = KEY, enableUncompressedEncryption = true).use { compressed ->
                assertEquals(Js5CompressionType.UNCOMPRESSED.ordinal, compressed.getUnsignedByte(0).toInt())
            }
        }
    }

    @Test
    fun testInvalidType() {
        read("invalid-type.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO)?.release()
            }
        }
    }

    @Test
    fun testInvalidLength() {
        read("invalid-length.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO)?.release()
            }
        }
    }

    @Test
    fun testInvalidUncompressedLength() {
        read("invalid-uncompressed-length.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testNoneEof() {
        read("none-eof.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO)?.release()
            }
        }
    }

    @Test
    fun testBzip2Eof() {
        read("bzip2-eof.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testGzipEof() {
        read("gzip-eof.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testLzmaEof() {
        read("lzma-eof.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testBzip2Corrupt() {
        read("bzip2-corrupt.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testGzipCorrupt() {
        read("gzip-corrupt.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testLzmaCorrupt() {
        read("lzma-corrupt.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { uncompressed ->
                assertNull(uncompressed)
            }
        }
    }

    @Test
    fun testNoneKeyValid() {
        copiedBuffer("OpenRS2").use { expected ->
            read("none.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }
        }
    }

    @Test
    fun testBzip2KeyValid() {
        copiedBuffer("OpenRS2").use { expected ->
            read("bzip2.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }

            read("bzip2-encrypted.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }
        }

        read("bzip2-invalid-magic.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }
    }

    @Test
    fun testGzipKeyValid() {
        copiedBuffer("OpenRS2").use { expected ->
            read("gzip.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }

            read("gzip-encrypted.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }
        }

        read("gzip-invalid-magic.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }

        read("gzip-invalid-method.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }
    }

    @Test
    fun testLzmaKeyValid() {
        copiedBuffer("OpenRS2").use { expected ->
            read("lzma.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }

            read("lzma-encrypted.dat").use { compressed ->
                Js5Compression.uncompressUnlessEncrypted(compressed.slice()).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                    assertNull(actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), KEY).use { actual ->
                    assertEquals(expected, actual)
                }

                Js5Compression.uncompressIfKeyValid(compressed.slice(), INVALID_KEY).use { actual ->
                    assertNull(actual)
                }
            }
        }

        read("lzma-dict-size-negative.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }

        read("lzma-dict-size-larger-than-preset.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }

        read("lzma-invalid-pb.dat").use { compressed ->
            Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }
    }

    @Test
    fun testKeyValidShorterThanTwoBlocks() {
        read("shorter-than-two-blocks.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO)?.release()
            }
        }
    }

    @Test
    fun testCompressedUnderflow() {
        read("compressed-underflow.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO)?.release()
            }
        }
    }

    @Test
    fun testUncompressedOverflow() {
        read("uncompressed-overflow.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }
    }

    @Test
    fun testUncompressedUnderflow() {
        read("uncompressed-underflow.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed.slice()).release()
            }

            Js5Compression.uncompressIfKeyValid(compressed.slice(), SymmetricKey.ZERO).use { actual ->
                assertNull(actual)
            }
        }
    }

    @Test
    fun testEmptyLocNone() {
        read("empty-loc-none.dat").use { compressed ->
            assertTrue(Js5Compression.isEmptyLoc(compressed))
        }

        read("none.dat").use { compressed ->
            assertFalse(Js5Compression.isEmptyLoc(compressed))
        }
    }

    @Test
    fun testEmptyLocGzip() {
        read("empty-loc-gzip.dat").use { compressed ->
            assertTrue(Js5Compression.isEmptyLoc(compressed))
        }

        read("empty-loc-gzip-stored.dat").use { compressed ->
            assertTrue(Js5Compression.isEmptyLoc(compressed))
        }

        read("gzip.dat").use { compressed ->
            assertFalse(Js5Compression.isEmptyLoc(compressed))
        }
    }

    @Test
    fun testEmptyLocBzip2() {
        read("empty-loc-bzip2.dat").use { compressed ->
            assertTrue(Js5Compression.isEmptyLoc(compressed))
        }

        read("bzip2.dat").use { compressed ->
            assertFalse(Js5Compression.isEmptyLoc(compressed))
        }
    }

    @Test
    fun testEmptyLocLzma() {
        read("empty-loc-lzma.dat").use { compressed ->
            assertTrue(Js5Compression.isEmptyLoc(compressed))
        }

        read("lzma.dat").use { compressed ->
            assertFalse(Js5Compression.isEmptyLoc(compressed))
        }
    }

    @Test
    fun testEmptyLocInvalid() {
        read("invalid-type.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.isEmptyLoc(compressed)
            }
        }

        read("invalid-length.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.isEmptyLoc(compressed)
            }
        }

        read("none-eof.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.isEmptyLoc(compressed)
            }
        }

        read("compressed-underflow.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.isEmptyLoc(compressed)
            }
        }
    }

    @Test
    fun testMissingHeader() {
        read("missing-header.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompress(compressed)
            }
        }

        read("missing-header.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompressUnlessEncrypted(compressed)
            }
        }

        read("missing-header.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.uncompressIfKeyValid(compressed, SymmetricKey.ZERO)
            }
        }

        read("missing-header.dat").use { compressed ->
            assertFailsWith<IOException> {
                Js5Compression.isEmptyLoc(compressed)
            }
        }
    }

    private fun read(name: String): ByteBuf {
        Js5CompressionTest::class.java.getResourceAsStream("compression/$name").use { input ->
            return Unpooled.wrappedBuffer(input.readBytes())
        }
    }

    private companion object {
        private val KEY = SymmetricKey.fromHex("00112233445566778899AABBCCDDEEFF")
        private val INVALID_KEY = SymmetricKey.fromHex("0123456789ABCDEF0123456789ABCDEF")
    }
}
