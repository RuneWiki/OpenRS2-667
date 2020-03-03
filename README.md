# OpenRS2

[![Build status badge](https://build.openrs2.dev/buildStatus/icon?job=openrs2&build=lastCompleted)](https://build.openrs2.dev/job/openrs2/)

## Introduction

OpenRS2 is an open-source multiplayer game server and suite of associated
tools. It is compatible with build 550 of the RuneScape client, which was
released in late 2009.

## Prerequisites

Building OpenRS2 requires version 8 or later of the [Java Development Kit][jdk].

### Non-free components

OpenRS2 requires the original RuneScape client code, data and location file
encryption keys, which we cannot legally distribute.

These files must be manually placed in the `nonfree` directory (directly
beneath the root of the repository), in the following structure:

```
nonfree
└── code
    ├── game_unpacker.dat
    ├── jaggl.pack200
    ├── loader_gl.jar
    ├── loader.jar
    ├── runescape_gl.pack200
    └── runescape.jar
```

The SHA-256 checksums of the correct files are:

```
7c090e07f8d754d09804ff6e9733ef3ba227893b6b639436db90977b39122590  nonfree/code/game_unpacker.dat
d39578f4a88a376bcb2571f05da1939a14a80d8c4ed89a4eb172d9e525795fe2  nonfree/code/jaggl.pack200
31182683ba04dc0ad45859161c13f66424b10deb0b2df10aa58b48bba57402db  nonfree/code/loader_gl.jar
ccdfaa86be07452ddd69f869ade86ea900dbb916fd853db16602edf2eb54211b  nonfree/code/loader.jar
4a5032ea8079d2154617ae1f21dfcc46a10e023c8ba23a4827d5e25e75c73045  nonfree/code/runescape_gl.pack200
0ab28a95e7c5993860ff439ebb331c0df02ad40aa1f544777ed91b46d30d3d24  nonfree/code/runescape.jar
```

The `nonfree` directory is included in the `.gitignore` file to prevent any
non-free material from being accidentally included in the repository.

## Building

Run `./gradlew` to download the dependencies, build the code, run the unit tests
and package it.

## Contributing

### Code style

All source code must be formatted with [IntelliJ IDEA][idea]'s built-in
formatter before each commit. The 'Optimize imports' option should also be
selected. Do not select 'Rearrange entries'.

OpenRS2's code style settings are held in `.idea/codeStyles/Project.xml` in the
repository, and IDEA should use them automatically after importing the Gradle
project.

Kotlin code must pass all of [ktlint][ktlint]'s tests.

### Commit messages

Commit messages should follow the ['seven rules'][commitmsg] described in
'How to Write a Git Commit Message', with the exception that the summary line
can be up to 72 characters in length (as OpenRS2 does not use email-based
patches).

### Developer Certificate of Origin

OpenRS2 uses version 1.1 of the [Developer Certificate of Origin][dco] (DCO) to
certify that contributors agree to license their code under OpenRS2's license
(see the Copyright section below). To confirm that a contribution meets the
requirements of the DCO, a `Signed-off-by:` line must be added to the Git
commit message by passing `--signoff` to the `git commit` invocation.

The full text of the DCO is reproduced below:

```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
1 Letterman Drive
Suite D4700
San Francisco, CA, 94129

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.


Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

### Versioning

OpenRS2 uses [Semantic Versioning][semver].

## License

Unless otherwise stated, all code and data is licensed under version 3.0 or
later of the [GNU General Public License][gpl]. The full terms are available
in the `COPYING` file.

The `deob-annotations` and `jsobject` modules are instead licensed under
version 3.0 or later of the [GNU Lesser General Public License][lgpl]. The full
terms are available in the `COPYING.LESSER` file in each module's directory.

## Copyright

Copyright (c) 2019-2020 OpenRS2 Authors

OpenRS2 is free software: you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

OpenRS2 is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
OpenRS2. If not, see <https://www.gnu.org/licenses/>.

[commitmsg]: https://chris.beams.io/posts/git-commit/#seven-rules
[dco]: https://developercertificate.org/
[gpl]: https://www.gnu.org/licenses/gpl-3.0.html
[idea]: https://www.jetbrains.com/idea/
[jdk]: https://jdk.java.net/
[ktlint]: https://github.com/pinterest/ktlint#readme
[lgpl]: https://www.gnu.org/licenses/lgpl-3.0.html
[semver]: https://semver.org/
