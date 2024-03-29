package org.openrs2.cache

import io.netty.buffer.ByteBuf

public object VersionTrailer {
    public fun peek(buf: ByteBuf): Int? {
        return if (buf.readableBytes() >= 2) {
            buf.getUnsignedShort(buf.writerIndex() - 2)
        } else {
            null
        }
    }

    public fun strip(buf: ByteBuf): Int? {
        return if (buf.readableBytes() >= 2) {
            val index = buf.writerIndex() - 2
            val version = buf.getUnsignedShort(index)
            buf.writerIndex(index)
            version
        } else {
            null
        }
    }
}
