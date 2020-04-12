package dev.openrs2.deob.map

import java.util.SortedMap

data class Method(
    val owner: String,
    val name: String,
    /*
     * Uses concrete type as there is no interface for a map sorted by
     * insertion order.
     */
    val arguments: LinkedHashMap<Int, String>,
    val locals: SortedMap<Int, String>
)