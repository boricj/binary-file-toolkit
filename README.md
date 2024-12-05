# Binary file toolkit

This is a set of Java libraries for manipulating file formats used by toolchains.

## Modules

* `bft-base`: library containing common base classes
* `bft-base-tests`: library containing common base test classes

## Ethos

This project focuses on the following points:
* Correctness, using strong typing and pervasive checks to reduce the opportunities for misuse ;
* Reproducibility, parsing and writing out a file should yield a byte-for-byte identical copy if practical ;
* Unopiniated, allowing any valid layout for generated files without restriction.

The following use-cases are outside the scope of this project:
* Tolerating malformed or corrupted files ;
* Graceful handling of valid but unsupported features ;
* Directly modifying existing files.
