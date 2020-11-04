![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.3.0.svg)](https://www.scala-js.org)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice abstractions.

    "com.github.arturopala" %% "buffer-and-slice" % "1.35.0"

Cross-compiles to Scala versions `2.13.3`, `2.12.11`, `2.11.12`, `3.0.0-M1`, `0.27.0-RC1`, 
and ScalaJS version `1.3.0`, and ScalaNative version `0.4.0-M2`.

Motivation
---

Working directly with mutable arrays, even in Scala, is not always as simple and efficient as it could be. 
While `Array` features Scala Collections API, the first reason to use arrays is to fully exploit its compactness and mutability
for performance reasons. I've found it reasonable to have a separate, focused set of low-overhead tools dealing with an `Array`.

Another concern was a proliferation of `ClassTag` context parameter, 
making it hard to offer Array-based variants of a generic data-structures.
This API requires now `ClassTag` only for explicit `.toArray[T1 >: T]` call, nowhere else.

Why at all using arrays in the era of functional programming?
---

Arrays are the most primitive but very efficient data structures, with fast access time, 
compact representation, unbeatable copy performance and taking advantage of the CPU cache line. 

The FP solution is to exploit arrays but avoid sharing its mutable state around. 
This is where the `Buffer` and the `Slice` concept fits in.

Design
---

This library provides two complementary abstractions, two sides of the coin: mutable `Buffer` and immutable lazy `Slice`.

- A `Buffer` role is to help easily build a growable array using mixed buffer- and stack- like APIs.

- A `Slice` role is to share an immutable slice of the array.

The usual workflow will use `Buffer` to build an array and `Slice` to share the result outside of a component/function.

Both `Buffer` and `Slice` come in variants: 

- generic `ArrayBuffer[T]` and `ArraySlice[T]`, 
- specialized `IntBuffer` and `IntSlice` with additional numeric API, 
- specialized `ByteBuffer` and `ByteSlice`.
- `LazyMapArraySlice` provides very light mapping operation on `Slice` without forcing underlying array copy.
- `RangeMapSlice` provides a slice of function of integers.
- `DeferredArrayBuffer[T]` makes it possible to defer underlying array type decision for abstract types.

Dependencies
---

Depends only on a standard built-in Scala library.

API
---

`Buffer` offers comprehensive API, together with `Stack`- and `List`- like interfaces.

For the purpose of using as a mutable List, the top element is a `head`.

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

```scala
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

Buffer manipulations, like `shift..`, `move..`, or `swap..` can change the buffer layout in a complex way.

An `IndexTracker` object provides set of functions to keep your external index buffer or list in sync with those changes.

Examples
---

Buffer
---

[Open in Scastie](https://scastie.scala-lang.org/arturopala/AeuggR2xTYC4lpNWLZoyug/6)

```scala
import com.github.arturopala.bufferandslice._

Buffer.empty[String]
// res4: Buffer[String] = []

Buffer.empty[Double]
// res5: Buffer[Double] = []

Buffer.empty[Int]
// res6: Buffer[Int] = []

Buffer.empty[Byte]
// res7: Buffer[Byte] = []

Buffer("a","b","c")
// res8: Buffer[String] = [a,b,c]

Buffer(Array("a","b","c"))
// res9: Buffer[String] = [a,b,c]

Buffer(1,2,3).apply(1)
// res10: Int = 2

Buffer(1,2,3).get(2)
// res11: Option[Int] = Some(value = 3)

Buffer("a","b","c").head
// res12: String = "c"

Buffer("a","b","c").last
// res13: String = "a"

Buffer("a","b","c").tail
// res14: Buffer[String] = [a,b]

Buffer("a","b","c").init
// res15: Buffer[String] = [b,c]

Buffer(1,2,3,4,5,6,7,8,9).asSlice
// res16: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).toArray
// res17: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

Buffer(1d,2d,3d,4d,5d,6d,7d,8d,9d).asArray
// res18: Array[Double] = Array(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)

val b1 = ByteBuffer(1,2)
// b1: ByteBuffer = [1,2]

b1.copy.append(3)
// res19: ByteBuffer = [1,2,3]

b1.copy.append(4)
// res20: ByteBuffer = [1,2,4]

b1.emptyCopy.append(9)
// res21: ByteBuffer = [9]
```

- Specialized `IntBuffer`:

```scala
IntBuffer(0,1,2,3)
// res22: IntBuffer = [0,1,2,3]

IntBuffer(Array(0,1,2,3))
// res23: IntBuffer = [0,1,2,3]

IntBuffer(0,1,2,3).asSlice
// res24: IntSlice = Slice(0,1,2,3)
```

- Specialized `ByteBuffer`:

```scala
ByteBuffer(0,1,2,3)
// res25: ByteBuffer = [0,1,2,3]

ByteBuffer(Array(0,1,2,3).map(_.toByte))
// res26: ByteBuffer = [0,1,2,3]

ByteBuffer(0,1,2,3).asSlice
// res27: ByteSlice = Slice(0,1,2,3)
```

- Modifying the content:

```scala
ByteBuffer(1,2,3).update(1,0)
// res28: ByteBuffer = [1,0,3]

Buffer(0,0,0).modify(1,_ + 1)
// res29: Buffer[Int] = [0,1,0]

Buffer("c").append("a")
// res30: Buffer[String] = [c,a]

Buffer("x","y","z").insert(1,"a")
// res31: Buffer[String] = [x,a,y,z]

Buffer("a","b","c").remove(1)
// res32: Buffer[String] = [a,c]

IntBuffer(0,1,1).appendSlice(Slice(0,1,2,3))
// res33: IntBuffer = [0,1,1,0,1,2,3]

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))
// res34: IntBuffer = [0,1,1,0,1,2,3]

Buffer("a").appendSequence(IndexedSeq("a","a","a"))
// res35: Buffer[String] = [a,a,a,a]

Buffer(0).appendIterable(1 to 10)
// res36: Buffer[Int] = [0,1,2,3,4,5,6,7,8,9,10]

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))
// res37: Buffer[String] = [b,a,a,a,a,a,a,a,a,a,a]

Buffer("b").appendFromIterator(3, Iterator.fill(10)("a"))
// res38: Buffer[String] = [b,a,a,a]

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))
// res39: Buffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).insertFromIterator(2, 3, (1 to 7).iterator)
// res40: Buffer[Int] = [0,0,1,2,3,0]

Buffer(0,0,0).insertFromIterator(2, (1 to 7).iterator)
// res41: Buffer[Int] = [0,0,1,2,3,4,5,6,7,0]

Buffer(0,0,0).insertFromIteratorReverse(1, 5, (1 to 7).iterator)
// res42: Buffer[Int] = [0,5,4,3,2,1,0,0]

Buffer(0,0,0).insertFromIteratorReverse(1, (1 to 7).iterator)
// res43: Buffer[Int] = [0,7,6,5,4,3,2,1,0,0]

Buffer("a","b","c").insertSlice(1, Slice("e","f"))
// res44: Buffer[String] = [a,e,f,b,c]

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))
// res45: Buffer[Int] = [0,2,3,4,0,0]

Buffer("a","b","c","d","e","f").replaceFromSlice(4,Slice("a","b","c"))
// res46: Buffer[String] = [a,b,c,d,a,b,c]

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))
// res47: Buffer[Int] = [0,2,3,4]

Buffer(0,0,0).replaceFromIterator(2,3, (1 to 7).iterator)
// res48: Buffer[Int] = [0,0,1,2,3]

Buffer(0,0,0,0,0,0,0).replaceFromIteratorReverse(5,3, (1 to 7).iterator)
// res49: Buffer[Int] = [0,0,0,0,0,3,2,1]

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))
// res50: Buffer[Int] = [0,2,3,4]

Buffer("a","b","c","d","e").removeRange(1,4)
// res51: Buffer[String] = [a,e]

Buffer(1,2,3,5,6).mapInPlace(_ * 2)
// res52: Buffer[Int] = [2,4,6,10,12]

Buffer(1,2,3,5,6).modifyAll(_ + 1)
// res53: Buffer[Int] = [2,3,4,6,7]

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)
// res54: Buffer[Int] = [1,3,3,5,7]

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)
// res55: Buffer[Int] = [0,1,1,0,0]

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)
// res56: Buffer[Int] = [1,2,4,4,5]

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)
// res57: IntBuffer = [1,2,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)
// res58: Buffer[Int] = [1,2,3,4,5,6,7,8,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeRight(1,4,3)
// res59: Buffer[Int] = [1,5,6,7,2,3,4,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeLeft(6,8,4)
// res60: Buffer[Int] = [1,2,7,8,3,4,5,6,9]

Buffer(1,2,3,4).swap(0,3)
// res61: Buffer[Int] = [4,2,3,1]

Buffer(1,2,3,4,5,6,7,8,9).swapRange(0,5,3)
// res62: Buffer[Int] = [6,7,8,4,5,1,2,3,9]

Buffer(1,2,3,4,5,6,7,8,9).contains(7)
// res63: Boolean = true

Buffer(1,2,3,4,5,6,7,8,9).exists(_ > 8)
// res64: Boolean = true

Buffer(1,2,3,4,5,6,7,8,9).exists(_ < 0)
// res65: Boolean = false
```

- Using `Buffer` as a stack:

```scala
Buffer(1,2,3).peek
// res66: Int = 3

Buffer(1,2,3).peek(1)
// res67: Int = 2

Buffer(1,2,3).peekOption(2)
// res68: Option[Int] = Some(value = 1)

Buffer(1,2,3).peekOption(3)
// res69: Option[Int] = None

Buffer(1,2,3).pop
// res70: Int = 3

Buffer(1,2,3).push(1).push(1).push(0)
// res71: Buffer[Int] = [1,2,3,1,1,0]
```

- Manipulating `topIndex` limit:

```scala
Buffer(1,2,3).top
// res72: Int = 2

Buffer(1,2,3).set(1)
// res73: Buffer[Int] = [1,2]

Buffer(1,2,3).forward(3)
// res74: Buffer[Int] = [1,2,3,0,0,0]

Buffer(1,2,3).rewind(2)
// res75: Buffer[Int] = [1]

Buffer(1,2,3).reset
// res76: Int = 2
```

- Making a `Slice` of a `Buffer`:

```scala
Buffer(1,2,3,4,5,6,7,8,9).asSlice
// res77: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)
// res78: Slice[Int] = Slice(3,4,5,6)

Buffer("a","c","e").asSlice
// res79: Slice[String] = Slice(a,c,e)

Buffer("a","c","e","d","b").slice(2,6)
// res80: Slice[String] = Slice(e,d,b)
```

- Accessing buffer content

```scala
Buffer("a","c","e").toArray
// res81: Array[String] = Array("a", "c", "e")

Buffer("a","c","e").asSlice.toList
// res82: List[String] = List("a", "c", "e")

Buffer(1,2,3,4,5,6,7,8,9).iterator
// res83: Iterator[Int] = non-empty iterator

Buffer(1,2,3,4,5,6,7,8,9).reverseIterator
// res84: Iterator[Int] = non-empty iterator

val s = "abscdefghijklmnopqrstuvxyz"
// s: String = "abscdefghijklmnopqrstuvxyz"

Buffer(1,2,3,4,5,6,7,8,9).map(s.apply).toList
// res85: List[Char] = List('b', 's', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
```


Slice
--

[Open in Scastie](https://scastie.scala-lang.org/arturopala/jo2JWppuRRyCkYL3SjmS7A/3)

```scala
import com.github.arturopala.bufferandslice._

val array = Array("a","b","c","d","ee","f","g","h","i","j")
// array: Array[String] = Array("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

val slice = Slice.of(array)
// slice: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)

slice.length
// res86: Int = 10

slice.top
// res87: Int = 9

slice.apply(0)
// res88: String = "a"

slice.apply(5)
// res89: String = "f"

slice.get(0)
// res90: Option[String] = Some(value = "a")

slice.get(50)
// res91: Option[String] = None

slice.update(4,"a")
// res92: Slice[String] = Slice(a,b,c,d,a,f,g,h,i,j)

slice.update(5,"b")
// res93: Slice[String] = Slice(a,b,c,d,ee,b,g,h,i,j)

slice.slice(1,5)
// res94: Slice[String] = Slice(b,c,d,ee)

slice.take(5)
// res95: Slice[String] = Slice(a,b,c,d,ee)

slice.drop(5)
// res96: Slice[String] = Slice(f,g,h,i,j)

slice.takeRight(5)
// res97: Slice[String] = Slice(f,g,h,i,j)

slice.dropRight(5)
// res98: Slice[String] = Slice(a,b,c,d,ee)

slice.slice(2,6)
// res99: Slice[String] = Slice(c,d,ee,f)

slice.head
// res100: String = "a"

slice.headOption
// res101: Option[String] = Some(value = "a")

slice.init
// res102: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i)

slice.last
// res103: String = "j"

slice.find("slice".contains)
// res104: Option[String] = Some(value = "c")

slice.exists("slice".contains)
// res105: Boolean = true

slice.map(s => s+s)
// res106: Slice[String] = Slice(aa,bb,cc,dd,eeee,ff,gg,hh,ii,jj)

slice.map(s => s"($s)")
// res107: Slice[String] = Slice((a),(b),(c),(d),(ee),(f),(g),(h),(i),(j))

slice.asIterable
// res108: Iterable[String] = Iterable("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

slice.iterator.toList
// res109: List[String] = List("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

slice.indexIterator("abeij".contains(_)).toList
// res110: List[Int] = List(0, 1, 8, 9)

slice.iterator("abeij".contains(_)).toList
// res111: List[String] = List("a", "b", "i", "j")

slice.reverseIterator.toList
// res112: List[String] = List("j", "i", "h", "g", "f", "ee", "d", "c", "b", "a")

slice.reverseIndexIterator("adgh".contains(_)).toList
// res113: List[Int] = List(7, 6, 3, 0)

slice.reverseIterator("adgh".contains(_)).toList
// res114: List[String] = List("h", "g", "d", "a")

slice.toList
// res115: List[String] = List("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

slice.toSeq
// res116: Seq[String] = Vector("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

slice.toArray
// res117: Array[String] = Array("a", "b", "c", "d", "ee", "f", "g", "h", "i", "j")

slice.copyToArray(3, new Array[String](15))
// res118: Array[String] = Array(null, null, null, "a", "b", "c", "d", "ee", "f", "g", "h", "i", "j", null, null)

slice.toBuffer
// res119: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]

slice.asBuffer
// res120: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]

val detached = slice.detach
// detached: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)
```

- Aggregating a `Slice`:

```scala
val slice3 = Slice.of("abcdefghijklmno".split(""))
// slice3: Slice[String] = Slice(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o)

slice3.fold("---")(_ + _)
// res121: String = "---abcdefghijklmno"

slice3.reduce(_ + _)
// res122: String = "abcdefghijklmno"

val slice4 = IntSlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
// slice4: IntSlice = Slice(0,1,2,3,4,5,6,7,8,9)

slice4.sum
// res123: Int = 45

slice4.min
// res124: Int = 0

slice4.max
// res125: Int = 9

slice4.reduce(_ + _)
// res126: Int = 45

slice4.fold(10)(_ + _)
// res127: Int = 55

slice4.foldLeft(10)(_ + _)
// res128: Int = 55

slice4.foldRight(10)(_ + _)
// res129: Int = 55

slice4.foldLeft("---")(_ + _.toString)
// res130: String = "---0123456789"

slice4.foldRight("---")(_.toString + _)
// res131: String = "0123456789---"

RangeMapSlice(x => s"($x)").take(10).drop(3).toList
// res132: List[String] = List("(3)", "(4)", "(5)", "(6)", "(7)", "(8)", "(9)")
```
