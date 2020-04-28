![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice implementations.

    "com.github.arturopala" %% "buffer-and-slice" % "1.0.0"

Cross-compiles to Scala versions `2.13.1`, `2.12.11`, `2.11.12`, and Dotty `0.23.0-RC1`.

Motivation
---

Working directly with mutable arrays, even in Scala, is not always as simple as it could be. 
While `Array` features Scala Collections API, the first reason to use arrays is to fully exploit its compactness and mutability
for performance reasons. 

I've found it reasonable to have a separate, focused set of low-overhead tools dealing with an `Array`.

Design
---

This library provides two complementary abstractions: mutable `Buffer` and immutable lazy `Slice`.

- A `Buffer` role is to help easily build a growable array using mixed buffer- and stack- like APIs.

- A `Slice` role is to share an immutable slice of the array.

The usual workflow will use `Buffer` to build an array and `Slice` to share the result outside of a component/function.

Both `Buffer` and `Slice` come in two variants: generic and specialized for `Int`.

Dependencies
---

Depends only on a standard built-in Scala library.

API
---

For more details, see:
- [Scaladoc of Buffer](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Buffer.html).
- [Scaladoc of Slice](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Slice.html).

Examples
---

Buffer
---

[Open in Scastie](https://scastie.scala-lang.org/hQkIThU8S0ynsbnNzji08g)

```scala mdoc
import com.github.arturopala.bufferandslice._

Buffer.apply[String]()

Buffer(1,2,3).apply(1)

Buffer(1,2,3).update(1,0)

Buffer("a","b","c")

Buffer("a").append("a")

Buffer("a").appendSequence(IndexedSeq("a","a","a"))

Buffer(0).appendIterable(1 to 10)

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))

Buffer(0,1,1).appendArray(Array(0,1,2,3))

Buffer(0,1,2).appendArray(Array(0,1,2,3))

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))

Buffer(0,0,0).insertFromIterator(2,3,Iterator.continually(1))

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))

Buffer(0,0,0).modify(1,_ + 1)

Buffer(1,2,3,5,6).modifyAll(_ + 1)

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)

Buffer(1,2,3).peek

Buffer(1,2,3).pop

Buffer(1,2,3).push(1).push(1).push(0)

Buffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)

Buffer(1,2,3,4,5,6,7,8,9).toSlice

Buffer(1,2,3,4,5,6,7,8,9).toArray

Buffer(1,2,3).top

Buffer(1,2,3).reset
```

Slice
--

[Open in Scastie](https://scastie.scala-lang.org/VbObn3VXQsCHdDFdI6DO8w)

```scala mdoc
import com.github.arturopala.bufferandslice._

val array = Array("a","b","c","d","ee","f","g","h","i","j")

val slice = Slice.of(array)

slice.apply(0)

slice.apply(5)

slice.update(4,"a")

slice.update(5,"b")

slice.array

slice.slice(1,5)

slice.take(5)

slice.drop(5)

slice.takeRight(5)

slice.dropRight(5)

slice.slice(2,6)

slice.head

slice.headOption

slice.init

slice.last

slice.count(_.length > 1)

slice.count(_.length == 1)

slice.map(s => s+s)

slice.map(s => s"($s)")

slice.asIterable

slice.iterator

slice.reverseIterator

slice.reverseIterator("adgh".contains(_))

slice.toList

slice.toArray

slice.toBuffer
```

