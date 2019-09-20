# Frequently Asked Questions

## How was build 550 chosen?

A mixture of reasons:

* The early HD era is my favourite. In particular, I like the 'clean'-looking
  tabs in the user interface, and 550 was the last build with those. I also
  considered 530, but 550 has a better built-in world map viewer (it supports
  dungeons in addition to the main surface).
* Availability of the original loader, client, game unpacker and jaggl jars.
* Availability of the complete set of original client data files.
* Availability of a large proportion of the location file encryption keys.

## Why does OpenRS2 use Maven instead of Gradle?

Gradle's task-based model is significantly better than Maven's fixed lifecycle
model.

However, Gradle's flexibility and rate of development has come at a cost of
worse IDE integration, to the point at which recent versions of IntelliJ IDEA
now delegate build actions to Gradle by default rather than attempting to
understand the project structure. This tends to be slower and consume more
memory than IDEA's built-in build system. While this setting can be changed, I
think it is a sign of the future of Gradle's IDE integration.

Furthermore, [nar-maven-plugin][nar-maven-plugin] is, at the time of writing,
significantly better than Gradle's support for building native code. Gradle's
new C++ plugin simply doesn't provide enough features.

This might be a decision we revisit in the future.

## Why is OpenRS2 licensed under the GNU GPL?

As significant amount of work went into the development of OpenRS2. My aim is
to encourage community contributions rather than effort being duplicated across
multiple independent closed-source forks, making a copyleft license desirable.

I also wanted to frustrate commercial use, given OpenRS2 is itself developed
entirely non-commercially. While the GPL does this to an extent, the AGPL would
have been more appropriate. However, it would be far more difficult to enforce
the AGPL than the GPL, disadvantaging honest users who would have otherwise
obeyed the license.

A small number of modules (`deob-annotations` and the native library
replacements) are instead licensed under the LGPL, as it needs to be possible
to legally link these modules with the proprietary client code.

## Why rewrite the client's native libraries?

Again, there are a mixture of reasons:

* Availability of the original native libraries. I struggled to find the
  original native libraries for 550, except for 32-bit Windows. While Linux and
  macOS natives are available for nearby revisions, they are not compatible
  with the 550 client.
* The original native libraries were not built for 64-bit Linux and macOS.
  While this was probably not a major problem in 2009, 64-bit architectures are
  now the norm.
* Non-x86 architectures like ARM and RISC-V are becoming more popular. If we
  start seeing a shift away from x86 on desktop machines, the native libraries
  will need to be built for those architectures.
* The original macOS jaggl native library is backed by an NSView, which was
  deprecated in Java 6 and removed in Java 7. Java 7 requires surfaces to be
  backed by a CALayer instead.
* I anticipate that at some point in the future the Linux AWT implementation
  will be ported from X11 to Wayland, which will require porting the jaggl
  native library from GLX to EGL.
* The switch away from OpenGL to newer graphics APIs like Metal and Vulkan
  might eventually necessitate the inclusion of OpenGL to Metal/Vulkan
  translation layers.
* I'm concerned about backwards compatibility and bit rot. The original native
  libraries were compiled 10 years ago, and at some point one of their
  dependencies might drop backwards binary compatibility.

[nar-maven-plugin]: https://maven-nar.github.io/