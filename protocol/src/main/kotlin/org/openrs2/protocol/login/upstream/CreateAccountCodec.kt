package org.openrs2.protocol.login.upstream

import io.netty.buffer.ByteBuf
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.openrs2.buffer.readString
import org.openrs2.buffer.use
import org.openrs2.buffer.writeString
import org.openrs2.crypto.Rsa
import org.openrs2.crypto.StreamCipher
import org.openrs2.crypto.SymmetricKey
import org.openrs2.crypto.XTEA_BLOCK_SIZE
import org.openrs2.crypto.publicKey
import org.openrs2.crypto.rsa
import org.openrs2.crypto.secureRandom
import org.openrs2.crypto.xteaDecrypt
import org.openrs2.crypto.xteaEncrypt
import org.openrs2.protocol.VariableShortPacketCodec
import org.openrs2.util.Base37

@Singleton
public class CreateAccountCodec @Inject constructor(
    private val rsaKey: RSAPrivateCrtKeyParameters
) : VariableShortPacketCodec<LoginRequest.CreateAccount>(
    type = LoginRequest.CreateAccount::class.java,
    opcode = 22
) {
    override fun decode(input: ByteBuf, cipher: StreamCipher): LoginRequest.CreateAccount {
        val build = input.readShort().toInt()

        val ciphertextLen = input.readUnsignedByte().toInt()
        val ciphertext = input.readSlice(ciphertextLen)

        ciphertext.rsa(rsaKey).use { plaintext ->
            require(plaintext.readUnsignedByte().toInt() == Rsa.MAGIC) {
                "Invalid RSA magic"
            }

            val flags = plaintext.readUnsignedShort()
            val gameNewsletters = (flags and FLAG_GAME_NEWSLETTERS) != 0
            val otherNewsletters = (flags and FLAG_OTHER_NEWSLETTERS) != 0
            val shareDetailsWithBusinessPartners = (flags and FLAG_SHARE_DETAILS_WITH_BUSINESS_PARTNERS) != 0

            val username = Base37.decodeLowerCase(plaintext.readLong())
            val k0 = plaintext.readInt()
            val password = plaintext.readString()
            val k1 = plaintext.readInt()
            val affiliate = plaintext.readUnsignedShort()
            val day = plaintext.readUnsignedByte().toInt()
            val month = plaintext.readUnsignedByte().toInt()
            val k2 = plaintext.readInt()
            val year = plaintext.readUnsignedShort()
            val country = plaintext.readUnsignedShort()
            val k3 = plaintext.readInt()

            val xteaKey = SymmetricKey(k0, k1, k2, k3)
            input.xteaDecrypt(input.readerIndex(), input.readableBytes(), xteaKey)

            val email = input.readString()

            val padding = input.readableBytes()
            require(padding in 1..XTEA_BLOCK_SIZE) {
                "Padding ($padding bytes) must be between 1 and $XTEA_BLOCK_SIZE bytes"
            }

            input.skipBytes(padding)

            return LoginRequest.CreateAccount(
                build,
                gameNewsletters,
                otherNewsletters,
                shareDetailsWithBusinessPartners,
                username,
                password,
                affiliate,
                year,
                month,
                day,
                country,
                email
            )
        }
    }

    override fun encode(input: LoginRequest.CreateAccount, output: ByteBuf, cipher: StreamCipher) {
        val xteaKey = SymmetricKey.generate()

        output.writeShort(input.build)

        output.alloc().buffer().use { plaintext ->
            plaintext.writeByte(Rsa.MAGIC)

            var flags = 0
            if (input.gameNewsletters) {
                flags = flags or FLAG_GAME_NEWSLETTERS
            }
            if (input.otherNewsletters) {
                flags = flags or FLAG_OTHER_NEWSLETTERS
            }
            if (input.shareDetailsWithBusinessPartners) {
                flags = flags or FLAG_SHARE_DETAILS_WITH_BUSINESS_PARTNERS
            }
            plaintext.writeShort(flags)

            plaintext.writeLong(Base37.encode(input.username))
            plaintext.writeInt(xteaKey.k0)
            plaintext.writeString(input.password)
            plaintext.writeInt(xteaKey.k1)
            plaintext.writeShort(input.affiliate)
            plaintext.writeByte(input.day)
            plaintext.writeByte(input.month)
            plaintext.writeInt(xteaKey.k2)
            plaintext.writeShort(input.year)
            plaintext.writeShort(input.country)
            plaintext.writeInt(xteaKey.k3)

            plaintext.rsa(rsaKey.publicKey).use { ciphertext ->
                output.writeByte(ciphertext.readableBytes())
                output.writeBytes(ciphertext)
            }
        }

        val xteaIndex = output.writerIndex()

        output.writeString(input.email)

        val padding = XTEA_BLOCK_SIZE - (output.writerIndex() - xteaIndex) % XTEA_BLOCK_SIZE
        for (i in 0 until padding) {
            output.writeByte(secureRandom.nextInt())
        }

        output.xteaEncrypt(xteaIndex, output.writerIndex() - xteaIndex, xteaKey)
    }

    private companion object {
        private const val FLAG_GAME_NEWSLETTERS = 0x1
        private const val FLAG_OTHER_NEWSLETTERS = 0x2
        private const val FLAG_SHARE_DETAILS_WITH_BUSINESS_PARTNERS = 0x4
    }
}
