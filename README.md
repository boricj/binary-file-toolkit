# Binary file toolkit

This is a set of Java libraries for reading and writing file formats used by toolchains.

## Modules

* `bft-base`: library containing common base classes.
* `bft-coff`: library for the _Common Object File Format_ specification.
* `bft-elf`: library for the _Executable and Linkable File_ specification.

## Documentation

API documentation is available at:
- Latest (master): https://boricj.github.io/binary-file-toolkit/javadoc/master/
- Development (develop): https://boricj.github.io/binary-file-toolkit/javadoc/develop/
- Release: replace branch name with tag name.

## Ethos

* **Correctness**: strong typing and exhaustive checks to reduce the opportunities for misuse.
* **Reproducibility**: whenever possible, parsing and serializing results in a byte-identical output.
* **Unopinionated**: no canonical layout is imposed.

The following use-cases are out of scope:
* Tolerating malformed/corrupted files.
* Graceful handling of unsupported features.
* Editing existing files.
