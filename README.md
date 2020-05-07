![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice abstractions.

    "com.github.arturopala" %% "buffer-and-slice" % "1.8.0"

Cross-compiles to Scala versions `2.13.2`, `2.12.11`, `2.11.12`, `0.24.0-RC1`, `0.23.0`, 
and ScalaJS version `1.0.1`, and ScalaNative version `0.4.0-M2`.

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
- exporting slice or buffer (`toArray`, `toList`, `toBuffer`)

E.g. the following code makes no copy of an array:

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

Examples
---

Buffer
---

[Open in Scastie](https://scastie.scala-lang.org/arturopala/AeuggR2xTYC4lpNWLZoyug/4)

```scala
import com.github.arturopala.bufferandslice._

Buffer.apply[String]()
// res4: Buffer[String] = []

Buffer("a","b","c")
// res5: Buffer[String] = [a,b,c]

Buffer(Array("a","b","c"))
// res6: Buffer[String] = [a,b,c]

Buffer(1,2,3).apply(1)
// res7: Int = 2

Buffer("a","b","c").head
// res8: String = "c"

Buffer("a","b","c").tail
// res9: Buffer[String] = [a,b]

Buffer(1,2,3,4,5,6,7,8,9).toArray
// res10: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
```

- Specialized `IntBuffer`:

```scala
IntBuffer(0,1,2,3)
// res11: IntBuffer = [0,1,2,3]

IntBuffer(Array(0,1,2,3))
// res12: IntBuffer = [0,1,2,3]

IntBuffer(0,1,2,3).asSlice
// res13: IntSlice = Slice(0,1,2,3)
```

- Modifying the content:

```scala
Buffer(1,2,3).update(1,0)
// res14: Buffer[Int] = [1,0,3]

Buffer("a").append("a")
// res15: Buffer[String] = [a,a]

IntBuffer(0,1,1).appendSlice(Slice(0,1,2,3))
// res16: IntBuffer = [0,1,1,0,1,2,3]

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))
// res17: IntBuffer = [0,1,1,0,1,2,3]

Buffer("a").appendSequence(IndexedSeq("a","a","a"))
// res18: Buffer[String] = [a,a,a,a]

Buffer(0).appendIterable(1 to 10)
// res19: Buffer[Int] = [0,1,2,3,4,5,6,7,8,9,10]

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))
// res20: Buffer[String] = [b,a,a,a,a,a,a,a,a,a,a]

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))
// res21: Buffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).insertFromIterator(2,3,Iterator.continually(1))
// res22: Buffer[Int] = [0,0,1,1,1,0]

Buffer("a","b","c").insertSlice(1, Slice("e","f"))
// res23: Buffer[String] = [a,e,f,b,c]

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))
// res24: Buffer[Int] = [0,2,3,4,0,0]

Buffer("a","b","c","d","e","f").replaceFromSlice(4,Slice("a","b","c"))
// res25: Buffer[String] = [a,b,c,d,a,b,c]

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))
// res26: Buffer[Int] = [0,2,3,4]

Buffer(0,0,0).replaceFromIterator(2,3,Iterator.continually(1))
// res27: Buffer[Int] = [0,0,1,1,1]

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))
// res28: Buffer[Int] = [0,2,3,4]

Buffer("a","b","c").remove(1)
// res29: Buffer[String] = [a,c]

Buffer("a","b","c","d","e").removeRange(1,4)
// res30: Buffer[String] = [a,e]

Buffer(0,0,0).modify(1,_ + 1)
// res31: Buffer[Int] = [0,1,0]

Buffer(1,2,3,5,6).modifyAll(_ + 1)
// res32: Buffer[Int] = [2,3,4,6,7]

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)
// res33: Buffer[Int] = [1,3,3,5,7]

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)
// res34: Buffer[Int] = [0,1,1,0,0]

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)
// res35: Buffer[Int] = [1,2,4,4,5]

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)
// res36: IntBuffer = [1,2,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)
// res37: Buffer[Int] = [1,2,3,4,5,6,7,8,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeRight(1,4,3)
// res38: Buffer[Int] = [1,5,6,7,2,3,4,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeLeft(6,8,4)
// res39: Buffer[Int] = [1,2,7,8,3,4,5,6,9]

Buffer(1,2,3,4).swap(0,3)
// res40: Buffer[Int] = [4,2,3,1]

Buffer(1,2,3,4,5,6,7,8,9).swapRange(0,5,3)
// res41: Buffer[Int] = [6,7,8,4,5,1,2,3,9]

Buffer(1,2,3,4,5,6,7,8,9).iterator
// res42: Iterator[Int] = non-empty iterator

Buffer(1,2,3,4,5,6,7,8,9).reverseIterator
// res43: Iterator[Int] = non-empty iterator
```

- Using `Buffer` as a stack:

```scala
Buffer(1,2,3).peek
// res44: Int = 3

Buffer(1,2,3).pop
// res45: Int = 3

Buffer(1,2,3).push(1).push(1).push(0)
// res46: Buffer[Int] = [1,2,3,1,1,0]
```

- Manipulating `topIndex` limit:

```scala
Buffer(1,2,3).top
// res47: Int = 2

Buffer(1,2,3).set(1)
// res48: Buffer[Int] = [1,2]

Buffer(1,2,3).forward(3)
// res49: Buffer[Int] = [1,2,3,0,0,0]

Buffer(1,2,3).rewind(2)
// res50: Buffer[Int] = [1]

Buffer(1,2,3).reset
// res51: Int = 2
```

- Making a `Slice` of a `Buffer`:

```scala
Buffer(1,2,3,4,5,6,7,8,9).asSlice
// res52: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)
// res53: Slice[Int] = Slice(3,4,5,6)

Buffer(1,2,3,4,5,6,7,8,9).take(3)
// res54: Slice[Int] = Slice(1,2,3)

Buffer(1,2,3,4,5,6,7,8,9).drop(3)
// res55: Slice[Int] = Slice(4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).takeRight(3)
// res56: Slice[Int] = Slice(7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).dropRight(3)
// res57: Slice[Int] = Slice(1,2,3,4,5,6)
```

Slice
--

[Open in Scastie](https://scastie.scala-lang.org/arturopala/jo2JWppuRRyCkYL3SjmS7A/2)

```scala
import com.github.arturopala.bufferandslice._

val array = Array("a","b","c","d","ee","f","g","h","i","j")
// array: Array[String] = Array(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

val slice = Slice.of(array)
// slice: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)

slice.apply(0)
// res58: String = "a"

slice.apply(5)
// res59: String = "f"

slice.update(4,"a")
// res60: Slice[String] = Slice(a,b,c,d,a,f,g,h,i,j)

slice.update(5,"b")
// res61: Slice[String] = Slice(a,b,c,d,ee,b,g,h,i,j)

slice.slice(1,5)
// res62: Slice[String] = Slice(b,c,d,ee)

slice.take(5)
// res63: Slice[String] = Slice(a,b,c,d,ee)

slice.drop(5)
// res64: Slice[String] = Slice(f,g,h,i,j)

slice.takeRight(5)
// res65: Slice[String] = Slice(f,g,h,i,j)

slice.dropRight(5)
// res66: Slice[String] = Slice(a,b,c,d,ee)

slice.slice(2,6)
// res67: Slice[String] = Slice(c,d,ee,f)

slice.head
// res68: String = "a"

slice.headOption
// res69: Option[String] = Some("a")

slice.init
// res70: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i)

slice.last
// res71: String = "j"

slice.count(_.length > 1)
// res72: Int = 1

slice.count(_.length == 1)
// res73: Int = 9

slice.map(s => s+s)
// res74: Slice[String] = Slice(aa,bb,cc,dd,eeee,ff,gg,hh,ii,jj)

slice.map(s => s"($s)")
// res75: Slice[String] = Slice((a),(b),(c),(d),(ee),(f),(g),(h),(i),(j))

slice.asIterable
// res76: Iterable[String] = Iterable(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

slice.iterator.toList
// res77: List[String] = List(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

slice.iterator("abeij".contains(_)).toList
// res78: List[String] = List("a", "b", "i", "j")

slice.reverseIterator.toList
// res79: List[String] = List(
//   "j",
//   "i",
//   "h",
//   "g",
//   "f",
//   "ee",
//   "d",
//   "c",
//   "b",
//   "a"
// )

slice.reverseIterator("adgh".contains(_)).toList
// res80: List[String] = List("h", "g", "d", "a")

slice.toList
// res81: List[String] = List(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

slice.toSeq
// res82: Seq[String] = Vector(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

slice.toArray
// res83: Array[String] = Array(
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j"
// )

slice.copyToArray(3, new Array[String](15))
// res84: Array[String] = Array(
//   null,
//   null,
//   null,
//   "a",
//   "b",
//   "c",
//   "d",
//   "ee",
//   "f",
//   "g",
//   "h",
//   "i",
//   "j",
//   null,
//   null
// )

slice.toBuffer
// res85: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]

val slice3 = slice.detach
// slice3: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)
```

