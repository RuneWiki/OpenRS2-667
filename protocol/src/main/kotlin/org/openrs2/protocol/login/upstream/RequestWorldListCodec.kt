package org.openrs2.protocol.login.upstream

import io.netty.buffer.ByteBuf
import jakarta.inject.Singleton
import org.openrs2.crypto.StreamCipher
import org.openrs2.protocol.FixedPacketCodec

@Singleton
public class RequestWorldListCodec : FixedPacketCodec<LoginRequest.RequestWorldList>(
    type = LoginRequest.RequestWorldList::class.java,
    opcode = 23,
    length = 4
) {
    override fun decode(input: ByteBuf, cipher: StreamCipher): LoginRequest.RequestWorldList {
        val checksum = input.readInt()
        return LoginRequest.RequestWorldList(checksum)
    }

    override fun encode(input: LoginRequest.RequestWorldList, output: ByteBuf, cipher: StreamCipher) {
        output.writeInt(input.checksum)
    }
}
