![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice abstractions.

    "com.github.arturopala" %% "buffer-and-slice" % "@VERSION@"

Cross-compiles to Scala versions @SUPPORTED_SCALA_VERSIONS@, 
and ScalaJS version `@SCALA_JS_VERSION@`, and ScalaNative version `@SCALA_NATIVE_VERSION@`.

Motivation
---

Working directly with mutable arrays, even in Scala, is not always as simple and efficient as it could be. 
While `Array` features Scala Collections API, the first reason to use arrays is to fully exploit its compactness and mutability
for performance reasons. I've found it reasonable to have a separate, focused set of low-overhead tools dealing with an `Array`.

Design
---

This library provides two complementary abstractions, two sides of the coin: mutable `Buffer` and immutable lazy `Slice`.

- A `Buffer` role is to help easily build a growable array using mixed buffer- and stack- like APIs.

- A `Slice` role is to share an immutable slice of the array.

The usual workflow will use `Buffer` to build an array and `Slice` to share the result outside of a component/function.

Both `Buffer` and `Slice` come in variants, generic and specialized: `ArrayBuffer[T]` and `ArraySlice[T]`, 
`IntBuffer` and `IntSlice`, `ByteBuffer` and `ByteSlice`.

Dependencies
---

Depends only on a standard built-in Scala library.

API
---

For more details, see:
- [Scaladoc of Buffer](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Buffer.html).
- [Scaladoc of Slice](https://arturopala.github.io/buffer-and-slice/latest/api/com/github/arturopala/bufferandslice/Slice.html).

Performance
---

The principle of this library is to avoid creating intermediary arrays as much as possible, 
and use native `java.lang.System.arraycopy` and `java.util.Arrays.copyOf` where applicable.

Lightweight operations:

- creating new `Buffer` or `Slice` from an array
- slicing (`asSlice`, `slice`, `take`, `drop`, `takeRight`, `dropRight`)
- mapping the slice
- using iterators or `toIterable`
- making `Slice.copyToArray`
- subsequent detaching

Heavier operations, making a copy of an array:

- detaching a slice first-time, if not detached already
- updating a slice
- making a copy of a buffer
- exporting slice or buffer (`toArray`, `toList`, `toBuffer`)

E.g. the following code makes no copy of an array `a`:

```scala mdoc:silent
import com.github.arturopala.bufferandslice._

val a = Array.fill(1000)(1)
val buffer = Buffer(a,100)
val slice1 = buffer.slice(13,31).map(_ * 2)
buffer.insertSlice(23, slice1.map(_ * 3))
buffer.replaceFromSlice(87, slice1.drop(7))
val slice2 = buffer.slice(17,71)
slice2.map(_+10).iterator.mkString("[",",","]")
buffer.appendSlice(slice2)
```

Index tracking
--

Buffer manipulations, like `shift..`,`move..`, or `swap..` changes the buffer layout in the complex way.

An `IndexTracker` object provides set of functions to keep your external index buffers or lists in sync with those changes.


Examples
---

Buffer
---

[Open in Scastie](https://scastie.scala-lang.org/arturopala/AeuggR2xTYC4lpNWLZoyug/4)

```scala mdoc
import com.github.arturopala.bufferandslice._

Buffer.apply[String]()

Buffer("a","b","c")

Buffer(Array("a","b","c"))

Buffer(1,2,3).apply(1)

Buffer("a","b","c").head

Buffer("a","b","c").tail

Buffer(1,2,3,4,5,6,7,8,9).toArray

val b1 = Buffer(1,2)

b1.copy.append(3)

b1.copy.append(4)
```

- Specialized `IntBuffer`:

```scala mdoc
IntBuffer(0,1,2,3)

IntBuffer(Array(0,1,2,3))

IntBuffer(0,1,2,3).asSlice
```

- Modifying the content:

```scala mdoc
Buffer(1,2,3).update(1,0)

Buffer("a").append("a")

IntBuffer(0,1,1).appendSlice(Slice(0,1,2,3))

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))

Buffer("a").appendSequence(IndexedSeq("a","a","a"))

Buffer(0).appendIterable(1 to 10)

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))

Buffer(0,0,0).insertFromIterator(2,3,Iterator.continually(1))

Buffer("a","b","c").insertSlice(1, Slice("e","f"))

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))

Buffer("a","b","c","d","e","f").replaceFromSlice(4,Slice("a","b","c"))

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))

Buffer(0,0,0).replaceFromIterator(2,3,Iterator.continually(1))

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))

Buffer("a","b","c").remove(1)

Buffer("a","b","c","d","e").removeRange(1,4)

Buffer(0,0,0).modify(1,_ + 1)

Buffer(1,2,3,5,6).modifyAll(_ + 1)

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)

Buffer(1,2,3,4,5,6,7,8,9).moveRangeRight(1,4,3)

Buffer(1,2,3,4,5,6,7,8,9).moveRangeLeft(6,8,4)

Buffer(1,2,3,4).swap(0,3)

Buffer(1,2,3,4,5,6,7,8,9).swapRange(0,5,3)

Buffer(1,2,3,4,5,6,7,8,9).iterator

Buffer(1,2,3,4,5,6,7,8,9).reverseIterator
```

- Using `Buffer` as a stack:

```scala mdoc
Buffer(1,2,3).peek

Buffer(1,2,3).pop

Buffer(1,2,3).push(1).push(1).push(0)
```

- Manipulating `topIndex` limit:

```scala mdoc
Buffer(1,2,3).top

Buffer(1,2,3).set(1)

Buffer(1,2,3).forward(3)

Buffer(1,2,3).rewind(2)

Buffer(1,2,3).reset
```

- Making a `Slice` of a `Buffer`:

```scala mdoc
Buffer(1,2,3,4,5,6,7,8,9).asSlice

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)

Buffer(1,2,3,4,5,6,7,8,9).take(3)

Buffer(1,2,3,4,5,6,7,8,9).drop(3)

Buffer(1,2,3,4,5,6,7,8,9).takeRight(3)

Buffer(1,2,3,4,5,6,7,8,9).dropRight(3)
```

Slice
--

[Open in Scastie](https://scastie.scala-lang.org/arturopala/jo2JWppuRRyCkYL3SjmS7A/2)

```scala mdoc
import com.github.arturopala.bufferandslice._

val array = Array("a","b","c","d","ee","f","g","h","i","j")

val slice = Slice.of(array)

slice.apply(0)

slice.apply(5)

slice.update(4,"a")

slice.update(5,"b")

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

slice.iterator.toList

slice.iterator("abeij".contains(_)).toList

slice.reverseIterator.toList

slice.reverseIterator("adgh".contains(_)).toList

slice.toList

slice.toSeq

slice.toArray

slice.copyToArray(3, new Array[String](15))

slice.toBuffer

val slice3 = slice.detach
```

