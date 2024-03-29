package org.openrs2.protocol

import io.netty.buffer.ByteBuf
import org.openrs2.crypto.StreamCipher

internal object TestFixedPacketCodec : FixedPacketCodec<FixedPacket>(
    type = FixedPacket::class.java,
    opcode = 0,
    length = 4
) {
    override fun decode(input: ByteBuf, cipher: StreamCipher): FixedPacket {
        val value = input.readInt()
        return FixedPacket(value)
    }

    override fun encode(input: FixedPacket, output: ByteBuf, cipher: StreamCipher) {
        output.writeInt(input.value)
    }
}
