![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice implementations.

    "com.github.arturopala" %% "buffer-and-slice" % "1.2.2"

Cross-compiles to Scala versions `2.13.2`, `2.12.11`, `2.11.12`, `0.24.0-RC1`, `0.23.0`, 
and ScalaJS version `1.0.1`, and ScalaNative version `0.4.0-M2`.

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

Performance
---

The principle of this library is to avoid creating intermediary arrays as much as possible, 
and use native `java.lang.System.arraycopy` and `java.util.Arrays.copyOf` where applicable.

Lightweight operations:

- creating new `Buffer` or `Slice` from an array
- slicing (`toSlice`, `slice`, `take`, `drop`, `takeRight`, `dropRight`)
- mapping the slice
- using iterators or `toIterable`
- making `Slice.copyToArray`

Heavy operations, making a copy of an array:

- exporting (`toArray`, `toList`, `toBuffer`)
- updating a slice

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

[Open in Scastie](https://scastie.scala-lang.org/hQkIThU8S0ynsbnNzji08g)

```scala
import com.github.arturopala.bufferandslice._

Buffer.apply[String]()
// res4: ArrayBuffer[String] = []

Buffer("a","b","c")
// res5: ArrayBuffer[String] = [a,b,c]

Buffer(Array("a","b","c"))
// res6: ArrayBuffer[String] = [a,b,c]

Buffer(1,2,3).apply(1)
// res7: Int = 2

Buffer("a","b","c").head
// res8: String = "c"

Buffer(1,2,3,4,5,6,7,8,9).toArray
// res9: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
```

- Specialized `IntBuffer`:

```scala
IntBuffer(0,1,2,3)
// res10: IntBuffer = [0,1,2,3]

IntBuffer(Array(0,1,2,3))
// res11: IntBuffer = [0,1,2,3]

IntBuffer(0,1,2,3).toSlice
// res12: IntSlice = Slice(0,1,2,3)
```

- Modifying the content:

```scala
Buffer(1,2,3).update(1,0)
// res13: ArrayBuffer[Int] = [1,0,3]

Buffer("a").append("a")
// res14: ArrayBuffer[String] = [a,a]

IntBuffer(0,1,1).appendSlice(Slice(0,1,2,3))
// res15: IntBuffer = [0,1,1,0,1,2,3]

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))
// res16: IntBuffer = [0,1,1,0,1,2,3]

Buffer("a").appendSequence(IndexedSeq("a","a","a"))
// res17: ArrayBuffer[String] = [a,a,a,a]

Buffer(0).appendIterable(1 to 10)
// res18: ArrayBuffer[Int] = [0,1,2,3,4,5,6,7,8,9,10]

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))
// res19: ArrayBuffer[String] = [b,a,a,a,a,a,a,a,a,a,a]

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))
// res20: ArrayBuffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).insertFromIterator(2,3,Iterator.continually(1))
// res21: ArrayBuffer[Int] = [0,0,1,1,1,0]

Buffer("a","b","c").insertSlice(1, Slice("e","f"))
// res22: ArrayBuffer[String] = [a,e,f,b,c]

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))
// res23: ArrayBuffer[Int] = [0,2,3,4,0,0]

Buffer("a","b","c","d","e","f").replaceFromSlice(4,Slice("a","b","c"))
// res24: ArrayBuffer[String] = [a,b,c,d,a,b,c]

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))
// res25: ArrayBuffer[Int] = [0,2,3,4]

Buffer(0,0,0).replaceFromIterator(2,3,Iterator.continually(1))
// res26: ArrayBuffer[Int] = [0,0,1,1,1]

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))
// res27: ArrayBuffer[Int] = [0,2,3,4]

Buffer("a","b","c").remove(1)
// res28: ArrayBuffer[String] = [a,c]

Buffer("a","b","c","d","e").removeRange(1,4)
// res29: ArrayBuffer[String] = [a,e]

Buffer(0,0,0).modify(1,_ + 1)
// res30: ArrayBuffer[Int] = [0,1,0]

Buffer(1,2,3,5,6).modifyAll(_ + 1)
// res31: ArrayBuffer[Int] = [2,3,4,6,7]

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)
// res32: ArrayBuffer[Int] = [1,3,3,5,7]

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)
// res33: ArrayBuffer[Int] = [0,1,1,0,0]

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)
// res34: ArrayBuffer[Int] = [1,2,4,4,5]

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)
// res35: IntBuffer = [1,2,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)
// res36: ArrayBuffer[Int] = [1,2,3,4,5,6,7,8,6,7,8,9]
```

- Using `Buffer` as a stack:

```scala
Buffer(1,2,3).peek
// res37: Int = 3

Buffer(1,2,3).pop
// res38: Int = 3

Buffer(1,2,3).push(1).push(1).push(0)
// res39: ArrayBuffer[Int] = [1,2,3,1,1,0]
```

- Manipulating `topIndex` limit:

```scala
Buffer(1,2,3).top
// res40: Int = 2

Buffer(1,2,3).set(1)
// res41: ArrayBuffer[Int] = [1,2]

Buffer(1,2,3).forward(3)
// res42: ArrayBuffer[Int] = [1,2,3,0,0,0]

Buffer(1,2,3).rewind(2)
// res43: ArrayBuffer[Int] = [1]

Buffer(1,2,3).reset
// res44: Int = 2
```

- Making a `Slice` of a `Buffer`:

```scala
Buffer(1,2,3,4,5,6,7,8,9).toSlice
// res45: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)
// res46: Slice[Int] = Slice(3,4,5,6)

Buffer(1,2,3,4,5,6,7,8,9).take(3)
// res47: Slice[Int] = Slice(1,2,3)

Buffer(1,2,3,4,5,6,7,8,9).drop(3)
// res48: Slice[Int] = Slice(4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).takeRight(3)
// res49: Slice[Int] = Slice(7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).dropRight(3)
// res50: Slice[Int] = Slice(1,2,3,4,5,6)
```

Slice
--

[Open in Scastie](https://scastie.scala-lang.org/VbObn3VXQsCHdDFdI6DO8w)

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
// res51: String = "a"

slice.apply(5)
// res52: String = "f"

slice.update(4,"a")
// res53: Slice[String] = Slice(a,b,c,d,a,f,g,h,i,j)

slice.update(5,"b")
// res54: Slice[String] = Slice(a,b,c,d,ee,b,g,h,i,j)

slice.slice(1,5)
// res55: Slice[String] = Slice(b,c,d,ee)

slice.take(5)
// res56: Slice[String] = Slice(a,b,c,d,ee)

slice.drop(5)
// res57: Slice[String] = Slice(f,g,h,i,j)

slice.takeRight(5)
// res58: Slice[String] = Slice(f,g,h,i,j)

slice.dropRight(5)
// res59: Slice[String] = Slice(a,b,c,d,ee)

slice.slice(2,6)
// res60: Slice[String] = Slice(c,d,ee,f)

slice.head
// res61: String = "a"

slice.headOption
// res62: Option[String] = Some("a")

slice.init
// res63: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i)

slice.last
// res64: String = "j"

slice.count(_.length > 1)
// res65: Int = 1

slice.count(_.length == 1)
// res66: Int = 9

slice.map(s => s+s)
// res67: Slice[String] = Slice(aa,bb,cc,dd,eeee,ff,gg,hh,ii,jj)

slice.map(s => s"($s)")
// res68: Slice[String] = Slice((a),(b),(c),(d),(ee),(f),(g),(h),(i),(j))

slice.asIterable
// res69: Iterable[String] = Iterable(
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
// res70: List[String] = List(
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

slice.reverseIterator.toList
// res71: List[String] = List(
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
// res72: List[String] = List("h", "g", "d", "a")

slice.toList
// res73: List[String] = List(
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
// res74: Array[String] = Array(
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
// res75: Array[String] = Array(
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
// res76: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]
```

