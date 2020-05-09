package dev.openrs2.asm

import com.fasterxml.jackson.databind.module.SimpleModule
import javax.inject.Singleton

@Singleton
class AsmJacksonModule : SimpleModule() {
    init {
        addDeserializer(MemberRef::class.java, MemberRefDeserializer)
        addKeyDeserializer(MemberRef::class.java, MemberRefKeyDeserializer)
    }
}
