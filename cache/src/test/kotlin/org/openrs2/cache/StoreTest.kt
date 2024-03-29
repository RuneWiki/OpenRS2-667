package org.openrs2.cache

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class StoreTest {
    @Test
    fun testOpen() {
        Store.open(DISK_ROOT).use { store ->
            assertTrue(store is DiskStore)
        }

        Store.open(LEGACY_DISK_ROOT).use { store ->
            assertTrue(store is DiskStore)
        }

        Store.open(FLAT_FILE_ROOT).use { store ->
            assertTrue(store is FlatFileStore)
        }
    }

    private companion object {
        private val DISK_ROOT = Path.of(StoreTest::class.java.getResource("disk-store/empty").toURI())
        private val LEGACY_DISK_ROOT =
            Path.of(StoreTest::class.java.getResource("disk-store/single-block-legacy").toURI())
        private val FLAT_FILE_ROOT = Path.of(StoreTest::class.java.getResource("flat-file-store/empty").toURI())
    }
}
