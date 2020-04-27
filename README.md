
Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice abstractions.

Motivation
---

Working directly with mutable arrays, even in Scala, is not always as simple as it could be. 
While Array features Scala Collections API, the first reason to use Array is to fully exploit its compactness and mutability
for performance reasons. 

Design
---

This library provides two complementary abstractions: mutable Buffer and immutable lazy Slice.

A **Buffer** role is to help easily build a growable array using mixed buffer- and stack- like APIs.

A **Slice** role is to share an immutable view of the full or range of an Array.

The usual workflow will use Buffer to elaborate an array and Slice to share it outside of component/function.

Both Buffer and Slice come in two variants: generic and specialized for Int.

Dependencies
---

Depends only on a standard built-in Scala library.

Cross-compiles to Scala versions `2.13.1`, `2.12.11`, `2.11.12`, and Dotty `0.23.0-RC1`.

API
---

For more details, see:
- [Scaladoc of Buffer](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Buffer.html).
- [Scaladoc of Slice](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Slice.html).
