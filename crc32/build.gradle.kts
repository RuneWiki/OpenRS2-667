plugins {
    `maven-publish`
    application
    kotlin("jvm")
}

application {
    mainClassName = "dev.openrs2.crc32.Crc32CommandKt"
}

dependencies {
    api(project(":cli"))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        pom {
            packaging = "jar"
            name.set("OpenRS2 CRC-32")
            description.set(
                """
                A tool for calculating the CRC-32 checksum of a file.
            """.trimIndent()
            )
        }
    }
}