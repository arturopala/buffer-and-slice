![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice implementations.

    "com.github.arturopala" %% "buffer-and-slice" % "1.2.1"

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

```scala
import com.github.arturopala.bufferandslice._

Buffer.apply[String]()
// res0: ArrayBuffer[String] = []

Buffer("a","b","c")
// res1: ArrayBuffer[String] = [a,b,c]

Buffer(Array("a","b","c"))
// res2: ArrayBuffer[String] = [a,b,c]

Buffer(1,2,3).apply(1)
// res3: Int = 2

Buffer("a","b","c").head
// res4: String = "c"

Buffer(1,2,3,4,5,6,7,8,9).toArray
// res5: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
```

- Specialized `IntBuffer`:

```scala
IntBuffer(0,1,2,3)
// res6: IntBuffer = [0,1,2,3]

IntBuffer(Array(0,1,2,3))
// res7: IntBuffer = [0,1,2,3]

IntBuffer(0,1,2,3).toSlice
// res8: IntSlice = Slice(0,1,2,3)
```

- Modifying the content:

```scala
Buffer(1,2,3).update(1,0)
// res9: ArrayBuffer[Int] = [1,0,3]

Buffer("a").append("a")
// res10: ArrayBuffer[String] = [a,a]

Buffer("a").appendSequence(IndexedSeq("a","a","a"))
// res11: ArrayBuffer[String] = [a,a,a,a]

Buffer(0).appendIterable(1 to 10)
// res12: ArrayBuffer[Int] = [0,1,2,3,4,5,6,7,8,9,10]

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))
// res13: ArrayBuffer[String] = [b,a,a,a,a,a,a,a,a,a,a]

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))
// res14: IntBuffer = [0,1,1,0,1,2,3]

IntBuffer(0,1,2).appendArray(Array(0,1,2,3))
// res15: IntBuffer = [0,1,2,0,1,2,3]

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))
// res16: ArrayBuffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).insertFromIterator(2,3,Iterator.continually(1))
// res17: ArrayBuffer[Int] = [0,0,1,1,1,0]

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))
// res18: ArrayBuffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))
// res19: ArrayBuffer[Int] = [0,2,3,4]

Buffer(0,0,0).replaceFromIterator(2,3,Iterator.continually(1))
// res20: ArrayBuffer[Int] = [0,0,1,1,1]

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))
// res21: ArrayBuffer[Int] = [0,2,3,4]

Buffer("a","b","c").remove(1)
// res22: ArrayBuffer[String] = [a,c]

Buffer("a","b","c","d","e").removeRange(1,4)
// res23: ArrayBuffer[String] = [a,e]

Buffer(0,0,0).modify(1,_ + 1)
// res24: ArrayBuffer[Int] = [0,1,0]

Buffer(1,2,3,5,6).modifyAll(_ + 1)
// res25: ArrayBuffer[Int] = [2,3,4,6,7]

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)
// res26: ArrayBuffer[Int] = [1,3,3,5,7]

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)
// res27: ArrayBuffer[Int] = [0,1,1,0,0]

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)
// res28: ArrayBuffer[Int] = [1,2,4,4,5]

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)
// res29: IntBuffer = [1,2,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)
// res30: ArrayBuffer[Int] = [1,2,3,4,5,6,7,8,6,7,8,9]
```

- Using `Buffer` as a stack:

```scala
Buffer(1,2,3).peek
// res31: Int = 3

Buffer(1,2,3).pop
// res32: Int = 3

Buffer(1,2,3).push(1).push(1).push(0)
// res33: ArrayBuffer[Int] = [1,2,3,1,1,0]
```

- Manipulating `topIndex` limit:

```scala
Buffer(1,2,3).top
// res34: Int = 2

Buffer(1,2,3).set(1)
// res35: ArrayBuffer[Int] = [1,2]

Buffer(1,2,3).forward(3)
// res36: ArrayBuffer[Int] = [1,2,3,0,0,0]

Buffer(1,2,3).rewind(2)
// res37: ArrayBuffer[Int] = [1]

Buffer(1,2,3).reset
// res38: Int = 2
```

- Making a `Slice` of a `Buffer`:

```scala
Buffer(1,2,3,4,5,6,7,8,9).toSlice
// res39: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)
// res40: Slice[Int] = Slice(3,4,5,6)

Buffer(1,2,3,4,5,6,7,8,9).take(3)
// res41: Slice[Int] = Slice(1,2,3)

Buffer(1,2,3,4,5,6,7,8,9).drop(3)
// res42: Slice[Int] = Slice(4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).takeRight(3)
// res43: Slice[Int] = Slice(7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).dropRight(3)
// res44: Slice[Int] = Slice(1,2,3,4,5,6)
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
// slice: ArraySlice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)

slice.apply(0)
// res45: String = "a"

slice.apply(5)
// res46: String = "f"

slice.update(4,"a")
// res47: Slice[String] = Slice(a,b,c,d,a,f,g,h,i,j)

slice.update(5,"b")
// res48: Slice[String] = Slice(a,b,c,d,ee,b,g,h,i,j)

slice.array
// res49: Array[slice.A] = Array(
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

slice.slice(1,5)
// res50: Slice[String] = Slice(b,c,d,ee)

slice.take(5)
// res51: Slice[String] = Slice(a,b,c,d,ee)

slice.drop(5)
// res52: Slice[String] = Slice(f,g,h,i,j)

slice.takeRight(5)
// res53: Slice[String] = Slice(f,g,h,i,j)

slice.dropRight(5)
// res54: Slice[String] = Slice(a,b,c,d,ee)

slice.slice(2,6)
// res55: Slice[String] = Slice(c,d,ee,f)

slice.head
// res56: String = "a"

slice.headOption
// res57: Option[String] = Some("a")

slice.init
// res58: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i)

slice.last
// res59: String = "j"

slice.count(_.length > 1)
// res60: Int = 1

slice.count(_.length == 1)
// res61: Int = 9

slice.map(s => s+s)
// res62: Slice[String] = Slice(aa,bb,cc,dd,eeee,ff,gg,hh,ii,jj)

slice.map(s => s"($s)")
// res63: Slice[String] = Slice((a),(b),(c),(d),(ee),(f),(g),(h),(i),(j))

slice.asIterable
// res64: Iterable[String] = Iterable(
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

slice.iterator
// res65: Iterator[String] = non-empty iterator

slice.reverseIterator
// res66: Iterator[String] = non-empty iterator

slice.reverseIterator("adgh".contains(_))
// res67: Iterator[String] = non-empty iterator

slice.toList
// res68: List[String] = List(
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
// res69: Array[Nothing] = Array(
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

slice.toBuffer
// res70: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]
```

