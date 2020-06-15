![Build](https://github.com/arturopala/buffer-and-slice/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/buffer-and-slice_2.13)

Buffer\[T] and Slice\[T]
===

This is a micro-library for Scala providing lightweight Buffer and Slice abstractions.

    "com.github.arturopala" %% "buffer-and-slice" % "1.23.0"

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

Buffer manipulations, like `shift..`,`move..`, or `swap..` changes the buffer layout in the complex way.

An `IndexTracker` object provides set of functions to keep your external index buffers or lists in sync with those changes.


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

Buffer(1,2,3).get(2)
// res8: Option[Int] = Some(3)

Buffer("a","b","c").head
// res9: String = "c"

Buffer("a","b","c").last
// res10: String = "a"

Buffer("a","b","c").tail
// res11: Buffer[String] = [a,b]

Buffer("a","b","c").init
// res12: Buffer[String] = [b,c]

Buffer(1,2,3,4,5,6,7,8,9).asSlice
// res13: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).toArray
// res14: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

val b1 = ByteBuffer(1,2)
// b1: ByteBuffer = [1,2]

b1.copy.append(3)
// res15: ByteBuffer = [1,2,3]

b1.copy.append(4)
// res16: ByteBuffer = [1,2,4]

b1.emptyCopy.append(9)
// res17: ByteBuffer = [9]
```

- Specialized `IntBuffer`:

```scala
IntBuffer(0,1,2,3)
// res18: IntBuffer = [0,1,2,3]

IntBuffer(Array(0,1,2,3))
// res19: IntBuffer = [0,1,2,3]

IntBuffer(0,1,2,3).asSlice
// res20: IntSlice = Slice(0,1,2,3)
```

- Specialized `ByteBuffer`:

```scala
ByteBuffer(0,1,2,3)
// res21: ByteBuffer = [0,1,2,3]

ByteBuffer(Array(0,1,2,3).map(_.toByte))
// res22: ByteBuffer = [0,1,2,3]

ByteBuffer(0,1,2,3).asSlice
// res23: ByteSlice = Slice(0,1,2,3)
```

- Modifying the content:

```scala
ByteBuffer(1,2,3).update(1,0)
// res24: ByteBuffer = [1,0,3]

Buffer(0,0,0).modify(1,_ + 1)
// res25: Buffer[Int] = [0,1,0]

Buffer("c").append("a")
// res26: Buffer[String] = [c,a]

Buffer("x","y","z").insert(1,"a")
// res27: Buffer[String] = [x,a,y,z]

Buffer("a","b","c").remove(1)
// res28: Buffer[String] = [a,c]

IntBuffer(0,1,1).appendSlice(Slice(0,1,2,3))
// res29: IntBuffer = [0,1,1,0,1,2,3]

IntBuffer(0,1,1).appendArray(Array(0,1,2,3))
// res30: IntBuffer = [0,1,1,0,1,2,3]

Buffer("a").appendSequence(IndexedSeq("a","a","a"))
// res31: Buffer[String] = [a,a,a,a]

Buffer(0).appendIterable(1 to 10)
// res32: Buffer[Int] = [0,1,2,3,4,5,6,7,8,9,10]

Buffer("b").appendFromIterator(Iterator.fill(10)("a"))
// res33: Buffer[String] = [b,a,a,a,a,a,a,a,a,a,a]

Buffer("b").appendFromIterator(3, Iterator.fill(10)("a"))
// res34: Buffer[String] = [b,a,a,a]

Buffer(0,0,0).insertValues(1,2,3,List(0,1,2,3,4,5))
// res35: Buffer[Int] = [0,2,3,4,0,0]

Buffer(0,0,0).insertFromIterator(2, 3, (1 to 7).iterator)
// res36: Buffer[Int] = [0,0,1,2,3,0]

Buffer(0,0,0).insertFromIterator(2, (1 to 7).iterator)
// res37: Buffer[Int] = [0,0,1,2,3,4,5,6,7,0]

Buffer(0,0,0).insertFromIteratorReverse(1, 5, (1 to 7).iterator)
// res38: Buffer[Int] = [0,5,4,3,2,1,0,0]

Buffer(0,0,0).insertFromIteratorReverse(1, (1 to 7).iterator)
// res39: Buffer[Int] = [0,7,6,5,4,3,2,1,0,0]

Buffer("a","b","c").insertSlice(1, Slice("e","f"))
// res40: Buffer[String] = [a,e,f,b,c]

Buffer(0,0,0).insertArray(1,2,3,Array(0,1,2,3,4,5))
// res41: Buffer[Int] = [0,2,3,4,0,0]

Buffer("a","b","c","d","e","f").replaceFromSlice(4,Slice("a","b","c"))
// res42: Buffer[String] = [a,b,c,d,a,b,c]

Buffer(0,0,0).replaceValues(1,2,3,List(0,1,2,3,4,5))
// res43: Buffer[Int] = [0,2,3,4]

Buffer(0,0,0).replaceFromIterator(2,3, (1 to 7).iterator)
// res44: Buffer[Int] = [0,0,1,2,3]

Buffer(0,0,0,0,0,0,0).replaceFromIteratorReverse(5,3, (1 to 7).iterator)
// res45: Buffer[Int] = [0,0,0,0,0,3,2,1]

Buffer(0,0,0).replaceFromArray(1,2,3,Array(0,1,2,3,4,5))
// res46: Buffer[Int] = [0,2,3,4]

Buffer("a","b","c","d","e").removeRange(1,4)
// res47: Buffer[String] = [a,e]

Buffer(1,2,3,5,6).mapInPlace(_ * 2)
// res48: Buffer[Int] = [2,4,6,10,12]

Buffer(1,2,3,5,6).modifyAll(_ + 1)
// res49: Buffer[Int] = [2,3,4,6,7]

Buffer(1,2,3,5,6).modifyAllWhen(_ + 1, _ % 2 == 0)
// res50: Buffer[Int] = [1,3,3,5,7]

Buffer(0,0,0,0,0).modifyRange(1, 3, _ + 1)
// res51: Buffer[Int] = [0,1,1,0,0]

Buffer(1,2,3,4,5).modifyRangeWhen(1, 3, _ + 1, _ % 2 != 0)
// res52: Buffer[Int] = [1,2,4,4,5]

IntBuffer(1,2,3,4,5,6,7,8,9).shiftLeft(5,3)
// res53: IntBuffer = [1,2,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).shiftRight(5,3)
// res54: Buffer[Int] = [1,2,3,4,5,6,7,8,6,7,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeRight(1,4,3)
// res55: Buffer[Int] = [1,5,6,7,2,3,4,8,9]

Buffer(1,2,3,4,5,6,7,8,9).moveRangeLeft(6,8,4)
// res56: Buffer[Int] = [1,2,7,8,3,4,5,6,9]

Buffer(1,2,3,4).swap(0,3)
// res57: Buffer[Int] = [4,2,3,1]

Buffer(1,2,3,4,5,6,7,8,9).swapRange(0,5,3)
// res58: Buffer[Int] = [6,7,8,4,5,1,2,3,9]

Buffer(1,2,3,4,5,6,7,8,9).contains(7)
// res59: Boolean = true

Buffer(1,2,3,4,5,6,7,8,9).exists(_ > 8)
// res60: Boolean = true

Buffer(1,2,3,4,5,6,7,8,9).exists(_ < 0)
// res61: Boolean = false
```

- Using `Buffer` as a stack:

```scala
Buffer(1,2,3).peek
// res62: Int = 3

Buffer(1,2,3).peek(1)
// res63: Int = 2

Buffer(1,2,3).peekOption(2)
// res64: Option[Int] = Some(1)

Buffer(1,2,3).peekOption(3)
// res65: Option[Int] = None

Buffer(1,2,3).pop
// res66: Int = 3

Buffer(1,2,3).push(1).push(1).push(0)
// res67: Buffer[Int] = [1,2,3,1,1,0]
```

- Manipulating `topIndex` limit:

```scala
Buffer(1,2,3).top
// res68: Int = 2

Buffer(1,2,3).set(1)
// res69: Buffer[Int] = [1,2]

Buffer(1,2,3).forward(3)
// res70: Buffer[Int] = [1,2,3,0,0,0]

Buffer(1,2,3).rewind(2)
// res71: Buffer[Int] = [1]

Buffer(1,2,3).reset
// res72: Int = 2
```

- Making a `Slice` of a `Buffer`:

```scala
Buffer(1,2,3,4,5,6,7,8,9).asSlice
// res73: Slice[Int] = Slice(1,2,3,4,5,6,7,8,9)

Buffer(1,2,3,4,5,6,7,8,9).slice(2,6)
// res74: Slice[Int] = Slice(3,4,5,6)

Buffer("a","c","e").asSlice
// res75: Slice[String] = Slice(a,c,e)

Buffer("a","c","e","d","b").slice(2,6)
// res76: Slice[String] = Slice(e,d,b)
```

- Accessing buffer content

```scala
Buffer("a","c","e").toArray
// res77: Array[String] = Array("a", "c", "e")

Buffer("a","c","e").asSlice.toList
// res78: List[String] = List("a", "c", "e")

Buffer(1,2,3,4,5,6,7,8,9).iterator
// res79: Iterator[Int] = non-empty iterator

Buffer(1,2,3,4,5,6,7,8,9).reverseIterator
// res80: Iterator[Int] = non-empty iterator

val s = "abscdefghijklmnopqrstuvxyz"
// s: String = "abscdefghijklmnopqrstuvxyz"

Buffer(1,2,3,4,5,6,7,8,9).map(s.apply).toList
// res81: List[Char] = List('b', 's', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
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

slice.length
// res82: Int = 10

slice.top
// res83: Int = 9

slice.apply(0)
// res84: String = "a"

slice.apply(5)
// res85: String = "f"

slice.get(0)
// res86: Option[String] = Some("a")

slice.get(50)
// res87: Option[String] = None

slice.update(4,"a")
// res88: Slice[String] = Slice(a,b,c,d,a,f,g,h,i,j)

slice.update(5,"b")
// res89: Slice[String] = Slice(a,b,c,d,ee,b,g,h,i,j)

slice.slice(1,5)
// res90: Slice[String] = Slice(b,c,d,ee)

slice.take(5)
// res91: Slice[String] = Slice(a,b,c,d,ee)

slice.drop(5)
// res92: Slice[String] = Slice(f,g,h,i,j)

slice.takeRight(5)
// res93: Slice[String] = Slice(f,g,h,i,j)

slice.dropRight(5)
// res94: Slice[String] = Slice(a,b,c,d,ee)

slice.slice(2,6)
// res95: Slice[String] = Slice(c,d,ee,f)

slice.head
// res96: String = "a"

slice.headOption
// res97: Option[String] = Some("a")

slice.init
// res98: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i)

slice.last
// res99: String = "j"

slice.find("slice".contains)
// res100: Option[String] = Some("c")

slice.exists("slice".contains)
// res101: Boolean = true

slice.count(_.length > 1)
// res102: Int = 1

slice.count(_.length == 1)
// res103: Int = 9

slice.map(s => s+s)
// res104: Slice[String] = Slice(aa,bb,cc,dd,eeee,ff,gg,hh,ii,jj)

slice.map(s => s"($s)")
// res105: Slice[String] = Slice((a),(b),(c),(d),(ee),(f),(g),(h),(i),(j))

slice.asIterable
// res106: Iterable[String] = Iterable(
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
// res107: List[String] = List(
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

slice.indexIterator("abeij".contains(_)).toList
// res108: List[Int] = List(0, 1, 8, 9)

slice.iterator("abeij".contains(_)).toList
// res109: List[String] = List("a", "b", "i", "j")

slice.reverseIterator.toList
// res110: List[String] = List(
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

slice.reverseIndexIterator("adgh".contains(_)).toList
// res111: List[Int] = List(7, 6, 3, 0)

slice.reverseIterator("adgh".contains(_)).toList
// res112: List[String] = List("h", "g", "d", "a")

slice.toList
// res113: List[String] = List(
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
// res114: Seq[String] = Vector(
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
// res115: Array[String] = Array(
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
// res116: Array[String] = Array(
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
// res117: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]

slice.asBuffer
// res118: Buffer[String] = [a,b,c,d,ee,f,g,h,i,j]

val slice3 = slice.detach
// slice3: Slice[String] = Slice(a,b,c,d,ee,f,g,h,i,j)
```

