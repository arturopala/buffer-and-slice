/*
 * Copyright 2020 Artur Opala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.arturopala.bufferandslice

class ArrayBufferSpec extends AnyWordSpecCompat {

  "Buffer" should {
    "access and update a value at an index" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer(0) shouldBe "a"
      buffer(1) shouldBe "b"
      buffer(2) shouldBe "c"
      buffer(0) = "aa"
      buffer(0) shouldBe "aa"
      buffer(1) shouldBe "b"
      buffer(2) shouldBe "c"
      buffer.length shouldBe 3
      buffer(111) = "foo"
      buffer(111) shouldBe "foo"
      buffer.length shouldBe 112
      buffer(9999) = "bar"
      buffer(9999) shouldBe "bar"
      buffer.length shouldBe 10000
    }

    "append value" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer(0) shouldBe "a"
      buffer(1) shouldBe "b"
      buffer(2) shouldBe "c"
      an[IndexOutOfBoundsException] shouldBe thrownBy(buffer(100))
      buffer.length shouldBe 3
      buffer.append("d").append("e").append("f")
      buffer(0) shouldBe "a"
      buffer(1) shouldBe "b"
      buffer(2) shouldBe "c"
      buffer(3) shouldBe "d"
      buffer(4) shouldBe "e"
      buffer(5) shouldBe "f"
      buffer.length shouldBe 6
      buffer.append("aa").append("aaa").append("aaaa")
      buffer(0) shouldBe "a"
      buffer(1) shouldBe "b"
      buffer(2) shouldBe "c"
      buffer(3) shouldBe "d"
      buffer(4) shouldBe "e"
      buffer(5) shouldBe "f"
      buffer(6) shouldBe "aa"
      buffer(7) shouldBe "aaa"
      buffer(8) shouldBe "aaaa"
      buffer.length shouldBe 9
      buffer(10) = "foo"
      buffer(9) shouldBe null
      buffer(10) shouldBe "foo"
      an[IndexOutOfBoundsException] shouldBe thrownBy(buffer(11))
      buffer.toArray shouldBe Array("a", "b", "c", "d", "e", "f", "aa", "aaa", "aaaa", null, "foo")
      buffer.length shouldBe 11
      buffer(1000) = "bar"
      buffer(999) shouldBe null
      buffer(1000) shouldBe "bar"
      an[IndexOutOfBoundsException] shouldBe thrownBy(buffer(1001))
      buffer.length shouldBe 1001
    }

    "remove value at an index" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.remove(3).toArray shouldBe Array("a", "b", "c")
      buffer.remove(1).toArray shouldBe Array("a", "c")
      buffer.remove(0).toArray shouldBe Array("c")
      buffer.remove(0).toArray shouldBe Array.empty[String]
    }

    "remove values in the range" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "e", "f", "g", "h"))
      buffer.removeRange(7, 15).toArray shouldBe Array("a", "b", "c", "e", "f", "g", "h")
      buffer.removeRange(1, 5).toArray shouldBe Array("a", "g", "h")
      buffer.removeRange(0, 2).toArray shouldBe Array("h")
      buffer.removeRange(0, 2).toArray shouldBe Array.empty[String]
    }

    "remove values matching the predicate" in {
      val buffer = Buffer(1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.removeWhen(_ % 2 != 0).toArray shouldBe Array(2, 4, 6, 8)
      buffer.removeWhen(_ < 5).toArray shouldBe Array(6, 8)
      buffer.removeWhen(_ > 7).toArray shouldBe Array(6)
    }

    "shift values right" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.shiftRight(1, 5).toArray shouldBe Array("a", "b", "c", null, null, null, "b", "c")
      buffer.length shouldBe 8
      buffer.shiftRight(7, 1).toArray shouldBe Array("a", "b", "c", null, null, null, "b", "c", "c")
      buffer.length shouldBe 9
      buffer.shiftRight(5, 2).toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11
      buffer.shiftRight(5, -2) // ignore negative distance
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11
      buffer.shiftRight(-5, 5) // ignore negative index
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11

      val buffer2 = new ArrayBuffer(Array.empty[String])
      buffer2.shiftRight(0, 10)
      buffer2.length shouldBe 10
    }

    "shift values left" in {
      val buffer = new ArrayBuffer(Array("a", "d", "e", "b", "c"))
      buffer.shiftLeft(2, 1)
      buffer.toArray shouldBe Array("a", "e", "b", "c")
      buffer.length shouldBe 4
      buffer.shiftLeft(2, 1)
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.shiftLeft(2, 2)
      buffer.toArray shouldBe Array("c")
      buffer.length shouldBe 1
      buffer.shiftLeft(0, 2)
      buffer.toArray shouldBe Array.empty[String]
      buffer.length shouldBe 0

      val buffer2 = new ArrayBuffer(Array.empty[String])
      buffer2.shiftLeft(0, 10)
      buffer2.length shouldBe 0
    }

    "move values in range to the right at a distance" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.moveRangeRight(2, 5, 2).toArray shouldBe Array("a", "b", "f", "g", "c", "d", "e", "h")
      buffer.moveRangeRight(0, 3, 3).toArray shouldBe Array("g", "c", "d", "a", "b", "f", "e", "h")
      buffer.moveRangeRight(1, 7, 2).toArray shouldBe Array("g", "h", null, "c", "d", "a", "b", "f", "e")
      buffer.moveRangeRight(15, 78, 25).toArray shouldBe Array("g", "h", null, "c", "d", "a", "b", "f", "e")
      buffer.moveRangeRight(1, 3, 5).toArray shouldBe Array("g", "c", "d", "a", "b", "f", "h", null, "e")
      buffer
        .moveRangeRight(1, 4, 10)
        .toArray shouldBe Array("g", "b", "f", "h", null, "e", null, null, null, null, null, "c", "d", "a")
    }

    "move values in range to the left at a distance" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.moveRangeLeft(3, 5, 2).toArray shouldBe Array("a", "d", "e", "b", "c", "f", "g", "h")
      buffer.moveRangeLeft(6, 10, 4).toArray shouldBe Array("a", "d", "g", "h", "e", "b", "c", "f")
      buffer.moveRangeLeft(2, 5, 2).toArray shouldBe Array("g", "h", "e", "a", "d", "b", "c", "f")
      buffer
        .moveRangeLeft(0, 5, 2)
        .toArray shouldBe Array("g", "h", "e", "a", "d", null, null, "b", "c", "f")
      buffer
        .moveRangeLeft(7, 12, 4)
        .toArray shouldBe Array("g", "h", "e", "b", "c", "f", "a", "d", null, null)
      buffer.trim(8).toArray shouldBe Array("g", "h", "e", "b", "c", "f", "a", "d")
      buffer
        .moveRangeLeft(5, 8, 5)
        .toArray shouldBe Array("f", "a", "d", "g", "h", "e", "b", "c")
      buffer
        .moveRangeLeft(5, 8, 4)
        .toArray shouldBe Array("f", "e", "b", "c", "a", "d", "g", "h")
      buffer
        .moveRangeLeft(5, 7, 3)
        .toArray shouldBe Array("f", "e", "d", "g", "b", "c", "a", "h")
      buffer
        .moveRangeLeft(7, 8, 7)
        .toArray shouldBe Array("h", "f", "e", "d", "g", "b", "c", "a")
    }

    "insert new value at index" in {
      Buffer.empty[String].insert(-1, "a").toArray shouldBe Array.empty[String]
      Buffer.empty[String].insert(0, "a").toArray shouldBe Array("a")
      Buffer.empty[String].insert(1, "a").toArray shouldBe Array(null, "a")
      Buffer.empty[String].insert(5, "c").toArray shouldBe Array(null, null, null, null, null, "c")
      Buffer("a").insert(-1, "c").toArray shouldBe Array("a")
      Buffer("a").insert(0, "c").toArray shouldBe Array("c", "a")
      Buffer("a").insert(1, "c").toArray shouldBe Array("a", "c")
      Buffer("a").insert(2, "c").toArray shouldBe Array("a", null, "c")
      Buffer("a", "b", "c").insert(-1, "x").toArray shouldBe Array("a", "b", "c")
      Buffer("a", "b", "c").insert(0, "x").toArray shouldBe Array("x", "a", "b", "c")
      Buffer("a", "b", "c").insert(1, "x").toArray shouldBe Array("a", "x", "b", "c")
      Buffer("a", "b", "c").insert(2, "x").toArray shouldBe Array("a", "b", "x", "c")
      Buffer("a", "b", "c").insert(3, "x").toArray shouldBe Array("a", "b", "c", "x")
      Buffer("a", "b", "c").insert(4, "x").toArray shouldBe Array("a", "b", "c", null, "x")
      Buffer("a", "b", "c").insert(5, "x").toArray shouldBe Array("a", "b", "c", null, null, "x")
    }

    "insert new array of values" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.insertArray(0, 0, 3, Array("a", "b", "c"))
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.insertArray(1, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("a", "d", "e", "b", "c")
      buffer.length shouldBe 5
      buffer.insertArray(0, 1, 1, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertArray(0, 1, 0, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertArray(5, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c")
      buffer.length shouldBe 8
      buffer.insertArray(10, 0, 5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12
      buffer.insertArray(10, 0, -5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12
    }

    "insert a slice" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.insertSlice(0, Slice("a", "b", "c")).toArray shouldBe Array("a", "b", "c")
      buffer.insertSlice(0, Slice("c", "b", "a")).toArray shouldBe Array("c", "b", "a", "a", "b", "c")
      buffer.insertSlice(3, Slice("e", "f")).toArray shouldBe Array("c", "b", "a", "e", "f", "a", "b", "c")
      buffer.insertSlice(5, Slice.empty[String]).toArray shouldBe Array("c", "b", "a", "e", "f", "a", "b", "c")
      buffer
        .insertSlice(10, Slice("e", "f", "g", "h"))
        .toArray shouldBe Array("c", "b", "a", "e", "f", "a", "b", "c", null, null, "e", "f", "g", "h")
    }

    "replace with new array of values" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.replaceFromArray(0, 0, 3, Array("a", "b", "c"))
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.replaceFromArray(1, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("a", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromArray(0, 1, 1, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromArray(0, 1, 0, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromArray(5, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", null, null, "d", "e")
      buffer.length shouldBe 7
      buffer.replaceFromArray(3, 0, 5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "d", "e", "d", "e")
      buffer.length shouldBe 7
      buffer.replaceFromArray(5, 0, -5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "d", "e", "d", "e")
      buffer.length shouldBe 7
    }

    "replace with a values from slice" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.replaceFromSlice(3, Slice("e", "f", "g", "h")).toArray shouldBe Array(null, null, null, "e", "f", "g", "h")
      buffer.replaceFromSlice(0, Slice("e", "f", "g", "h")).toArray shouldBe Array("e", "f", "g", "h", "f", "g", "h")
      buffer.replaceFromSlice(5, Slice("a", "b", "c")).toArray shouldBe Array("e", "f", "g", "h", "f", "a", "b", "c")
    }

    "insert new values from indexed source" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.insertValues(0, 0, 3, Array("a", "b", "c"))
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.insertValues(1, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("a", "d", "e", "b", "c")
      buffer.length shouldBe 5
      buffer.insertValues(0, 1, 1, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertValues(0, 1, 0, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertValues(5, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c")
      buffer.length shouldBe 8
      buffer.insertValues(10, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12
      buffer.insertValues(10, 0, -5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12
    }

    "replace with values from indexed source" in {
      val buffer = Buffer("a", "b", "c")
      buffer.replaceValues(1, 0, 3, Array("e", "f", "g"))
      buffer.toArray shouldBe Array("a", "e", "f", "g")
      buffer.length shouldBe 4
      buffer.replaceValues(1, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("a", "d", "e", "g")
      buffer.length shouldBe 4
      buffer.replaceValues(0, 1, 1, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "g")
      buffer.length shouldBe 4
      buffer.replaceValues(0, 1, 0, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "g")
      buffer.length shouldBe 4
      buffer.replaceValues(5, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "g", null, "d", "e")
      buffer.length shouldBe 7
      buffer.replaceValues(4, 0, 2, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "g", "d", "e", "e")
      buffer.length shouldBe 7
      buffer.replaceValues(10, 0, -5, Array("d", "e"))
      buffer.toArray shouldBe Array("e", "d", "e", "g", "d", "e", "e")
      buffer.length shouldBe 7
    }

    "insert new values from iterator" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.insertFromIterator(0, 3, Array("a", "b", "c").iterator)
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.insertFromIterator(1, 2, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("a", "d", "e", "b", "c")
      buffer.length shouldBe 5
      buffer.insertFromIterator(0, 1, Array("e").iterator)
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertFromIterator(0, 0, Array("e").iterator)
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "c")
      buffer.length shouldBe 6
      buffer.insertFromIterator(5, 2, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c")
      buffer.length shouldBe 8
      buffer.insertFromIterator(10, 2, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12
      buffer.insertFromIterator(10, -5, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("e", "a", "d", "e", "b", "d", "e", "c", null, null, "d", "e")
      buffer.length shouldBe 12

      Buffer
        .empty[String]
        .insertFromIterator(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer
        .empty[String]
        .insertFromIterator(0, 7, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer
        .empty[String]
        .insertFromIterator(0, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer
        .empty[String]
        .insertFromIterator(0, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b")
      Buffer
        .empty[String]
        .insertFromIterator(2, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, "a", "b")
      Buffer
        .empty[String]
        .insertFromIterator(2, 5, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, "a", "b", "c")

      Buffer("x")
        .insertFromIterator(0, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "x")
      Buffer("x")
        .insertFromIterator(1, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a")
      Buffer("x", "y", "z")
        .insertFromIterator(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c", "x", "y", "z")
      Buffer("x", "y", "z")
        .insertFromIterator(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "b", "y", "z")
      Buffer("x", "y", "z")
        .insertFromIterator(5, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, null, "a", "b")
    }

    "insert new values from iterator in the reverse order" in {
      Buffer
        .empty[String]
        .insertFromIteratorReverse(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("c", "b", "a")
      Buffer
        .empty[String]
        .insertFromIteratorReverse(0, 7, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("c", "b", "a")
      Buffer
        .empty[String]
        .insertFromIteratorReverse(0, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("c", "b", "a")
      Buffer
        .empty[String]
        .insertFromIteratorReverse(0, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("b", "a")
      Buffer
        .empty[String]
        .insertFromIteratorReverse(2, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, "b", "a")
      Buffer
        .empty[String]
        .insertFromIteratorReverse(2, 5, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, "c", "b", "a")

      Buffer("x")
        .insertFromIteratorReverse(0, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "x")
      Buffer("x")
        .insertFromIteratorReverse(1, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a")
      Buffer("x", "y", "z")
        .insertFromIteratorReverse(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("c", "b", "a", "x", "y", "z")
      Buffer("x", "y", "z")
        .insertFromIteratorReverse(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "b", "a", "y", "z")
      Buffer("x", "y", "z")
        .insertFromIteratorReverse(5, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, null, "b", "a")
    }

    "replace with values from iterator" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.replaceFromIterator(0, 3, Array("a", "b", "c").iterator)
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.length shouldBe 3
      buffer.replaceFromIterator(1, 2, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("a", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromIterator(0, 1, Array("e").iterator)
      buffer.toArray shouldBe Array("e", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromIterator(0, 0, Array("e").iterator)
      buffer.toArray shouldBe Array("e", "d", "e")
      buffer.length shouldBe 3
      buffer.replaceFromIterator(5, 2, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("e", "d", "e", null, null, "d", "e")
      buffer.length shouldBe 7
      buffer.replaceFromIterator(3, 3, Array("d", "e", "f").iterator)
      buffer.toArray shouldBe Array("e", "d", "e", "d", "e", "f", "e")
      buffer.length shouldBe 7
      buffer.replaceFromIterator(10, -5, Array("d", "e").iterator)
      buffer.toArray shouldBe Array("e", "d", "e", "d", "e", "f", "e")
      buffer.length shouldBe 7

      Buffer
        .empty[String]
        .replaceFromIterator(0, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer
        .empty[String]
        .replaceFromIterator(0, 7, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer
        .empty[String]
        .replaceFromIterator(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, "a", "b")
      Buffer
        .empty[String]
        .replaceFromIterator(1, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, "a", "b", "c")

      Buffer("x", "y", "z")
        .replaceFromIterator(0, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "y", "z")
      Buffer("x", "y", "z")
        .replaceFromIterator(1, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "z")
      Buffer("x", "y", "z")
        .replaceFromIterator(2, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "a")
      Buffer("x", "y", "z")
        .replaceFromIterator(3, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", "a")
      Buffer("x", "y", "z")
        .replaceFromIterator(4, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, "a")
      Buffer("x", "y", "z")
        .replaceFromIterator(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "b")
      Buffer("x", "y", "z")
        .replaceFromIterator(1, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "b", "c")
      Buffer("x", "y", "z")
        .replaceFromIterator(1, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "b", "c")
      Buffer("x", "y", "z")
        .replaceFromIterator(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "b", "c")
      Buffer("x", "y", "z")
        .replaceFromIterator(5, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, null, "a", "b")
    }

    "replace with values from iterator in the reverse order" in {
      Buffer
        .empty[String]
        .replaceFromIteratorReverse(0, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, "c", "b", "a")
      Buffer
        .empty[String]
        .replaceFromIteratorReverse(0, 7, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, null, null, "c", "b", "a")
      Buffer
        .empty[String]
        .replaceFromIteratorReverse(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, "b", "a")
      Buffer
        .empty[String]
        .replaceFromIteratorReverse(1, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array(null, null, "c", "b", "a")

      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(0, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("a", "y", "z")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(1, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "a", "z")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(2, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(3, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(4, 1, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(1, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "b", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(1, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "c", "b", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(1, 4, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "c", "b", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(1, 7, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, null, "c", "b", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(0, 3, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("c", "b", "a")
      Buffer("x", "y", "z")
        .replaceFromIteratorReverse(5, 2, Array("a", "b", "c").iterator)
        .toArray shouldBe Array("x", "y", "z", null, null, "b", "a")
    }

    "append an array" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.appendArray(Array("a", "b", "c"))
      buffer.length shouldBe 3
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.appendArray(Array("a", "b", "c"))
      buffer.length shouldBe 6
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c")
      buffer.appendArray(Array("d", "e"))
      buffer.length shouldBe 8
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c", "d", "e")
      buffer.appendArray(Array())
      buffer.length shouldBe 8
    }

    "append a slice" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.appendSlice(Slice.empty[String]).toArray shouldBe Array.empty[String]
      buffer.appendSlice(Slice("a", "b", "c")).toArray shouldBe Array("a", "b", "c")
      buffer.appendSlice(Slice("a", "b", "c")).toArray shouldBe Array("a", "b", "c", "a", "b", "c")
      buffer.appendSlice(Slice("e")).toArray shouldBe Array("a", "b", "c", "a", "b", "c", "e")
      buffer.appendSlice(Slice("e", "f", "g")).toArray shouldBe Array("a", "b", "c", "a", "b", "c", "e", "e", "f", "g")
      buffer.appendSlice(Slice.empty[String]).toArray shouldBe Array("a", "b", "c", "a", "b", "c", "e", "e", "f", "g")
    }

    "append sequence of values" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.appendSequence(IndexedSeq("a", "b", "c"))
      buffer.length shouldBe 3
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.appendSequence(IndexedSeq("a", "b", "c"))
      buffer.length shouldBe 6
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c")
      buffer.appendSequence(IndexedSeq("d", "e"))
      buffer.length shouldBe 8
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c", "d", "e")
      buffer.appendSequence(IndexedSeq())
      buffer.length shouldBe 8
    }

    "append values from iterator" in {
      val buffer = new ArrayBuffer(Array.empty[String])
      buffer.appendFromIterator(List("a", "b", "c").iterator)
      buffer.length shouldBe 3
      buffer.toArray shouldBe Array("a", "b", "c")
      buffer.appendFromIterator(List("a", "b", "c").iterator)
      buffer.length shouldBe 6
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c")
      buffer.appendFromIterator(List("d", "e").iterator)
      buffer.length shouldBe 8
      buffer.toArray shouldBe Array("a", "b", "c", "a", "b", "c", "d", "e")
      buffer.appendFromIterator(List().iterator)
      buffer.length shouldBe 8
    }

    "set and reset top index" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.length shouldBe 3
      buffer.set(10)
      buffer.length shouldBe 11
      buffer.set(3)
      buffer.length shouldBe 4
      buffer.set(10)
      buffer.length shouldBe 11
      buffer.set(-10)
      buffer.length shouldBe 0
      buffer.set(13)
      buffer.length shouldBe 14
      buffer.reset shouldBe 13
      buffer.length shouldBe 0
      buffer.reset shouldBe -1
      buffer.length shouldBe 0
    }

    "rewind and forward top index" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.top shouldBe 2
      buffer.set(10)
      buffer.top shouldBe 10
      buffer.rewind(3)
      buffer.top shouldBe 7
      buffer.rewind(10)
      buffer.top shouldBe -1
      buffer.rewind(10)
      buffer.top shouldBe -1
      buffer.forward(9)
      buffer.top shouldBe 9
      buffer.forward(3)
      buffer.top shouldBe 12
      buffer.rewind(1)
      buffer.top shouldBe 11
      buffer.forward(2)
      buffer.top shouldBe 13
    }

    "swap two values" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.swap(2, 2).toArray shouldBe Array("a", "b", "c")
      buffer.swap(0, 2).toArray shouldBe Array("c", "b", "a")
      buffer.swap(0, 1).toArray shouldBe Array("b", "c", "a")
      buffer.swap(2, 1).toArray shouldBe Array("b", "a", "c")
      buffer.swap(2, 3).toArray shouldBe Array("b", "a", "c")
      buffer.swap(-1, 1).toArray shouldBe Array("b", "a", "c")
      buffer.swap(-1, -5).toArray shouldBe Array("b", "a", "c")
    }

    "swap two ranges of values" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.swapRange(0, 5, 2).toArray shouldBe Array("f", "g", "c", "d", "e", "a", "b", "h")
      buffer.swapRange(0, 3, 5).toArray shouldBe Array("d", "e", "a", "b", "h", "c", "d", "e")
      buffer.swapRange(-2, 3, 5).toArray shouldBe Array("d", "e", "a", "b", "h", "c", "d", "e")
      buffer.swapRange(3, -3, 5).toArray shouldBe Array("d", "e", "a", "b", "h", "c", "d", "e")
      buffer.swapRange(3, 3, 5).toArray shouldBe Array("d", "e", "a", "b", "h", "c", "d", "e")
      buffer.swapRange(3, 4, 0).toArray shouldBe Array("d", "e", "a", "b", "h", "c", "d", "e")
      buffer.swapRange(4, 1, 3).toArray shouldBe Array("d", "h", "c", "d", "e", "a", "b", "e")
    }

    "have a tail" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.tail.toArray shouldBe Array("a", "b", "c", "d", "e", "f", "g")
      buffer.tail.toArray shouldBe Array("a", "b", "c", "d", "e", "f")
      buffer.tail.toArray shouldBe Array("a", "b", "c", "d", "e")
      buffer.tail.toArray shouldBe Array("a", "b", "c", "d")
      buffer.tail.toArray shouldBe Array("a", "b", "c")
      buffer.tail.toArray shouldBe Array("a", "b")
      buffer.tail.toArray shouldBe Array("a")
      buffer.tail.isEmpty shouldBe true
      buffer.tail.isEmpty shouldBe true
    }

    "have an iterator" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.iterator.toList shouldBe List("a", "b", "c", "d", "e", "f", "g", "h")
      buffer.tail.iterator.toList shouldBe List("a", "b", "c", "d", "e", "f", "g")
      buffer.tail.iterator.toList shouldBe List("a", "b", "c", "d", "e", "f")
    }

    "have a reverse iterator" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c", "d", "e", "f", "g", "h"))
      buffer.reverseIterator.toList shouldBe List("h", "g", "f", "e", "d", "c", "b", "a")
      buffer.tail.reverseIterator.toList shouldBe List("g", "f", "e", "d", "c", "b", "a")
      buffer.tail.reverseIterator.toList shouldBe List("f", "e", "d", "c", "b", "a")
    }

    "have a copy" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.copy.append("d").toArray shouldBe Array("a", "b", "c", "d")
      buffer.copy.append("e").toArray shouldBe Array("a", "b", "c", "e")
      buffer.toArray shouldBe Array("a", "b", "c")
    }

    "have a headOption" in {
      Buffer.empty[String].headOption shouldBe None
      Buffer.empty[Int].headOption shouldBe None
      Buffer(1).headOption shouldBe Some(1)
      Buffer("a", "b").headOption shouldBe Some("b")
      Buffer("a", "c").headOption shouldBe Some("c")
      Buffer(1, 2).headOption shouldBe Some(2)
      Buffer(1, 13).headOption shouldBe Some(13)
    }

    "have a last" in {
      Buffer(1).last shouldBe 1
      Buffer("a", "b").last shouldBe "a"
      Buffer("a", "c").last shouldBe "a"
      Buffer(1, 2).last shouldBe 1
      Buffer(13, 1).last shouldBe 13
    }

    "have a lastOption" in {
      Buffer.empty[String].lastOption shouldBe None
      Buffer.empty[Int].lastOption shouldBe None
      Buffer(1).lastOption shouldBe Some(1)
      Buffer("a", "b").lastOption shouldBe Some("a")
      Buffer("a", "c").lastOption shouldBe Some("a")
      Buffer(1, 2).lastOption shouldBe Some(1)
      Buffer(13, 1).lastOption shouldBe Some(13)
    }

    "have an init" in {
      Buffer.empty[String].init.toArray shouldBe Array.empty[String]
      Buffer.empty[Int].init.toArray shouldBe Array.empty[Int]
      Buffer(1).init.toArray shouldBe Array.empty[Int]
      Buffer("a", "b").init.toArray shouldBe Array("b")
      Buffer("a", "b", "c").init.toArray shouldBe Array("b", "c")
      Buffer("a", "b", "c", "d").init.toArray shouldBe Array("b", "c", "d")
      Buffer("a", "c").init.toArray shouldBe Array("c")
      Buffer(1, 2).init.toArray shouldBe Array(2)
      Buffer(13, 1).init.toArray shouldBe Array(1)
      Buffer(13, 1, 17, 23).init.toArray shouldBe Array(1, 17, 23)
      Buffer(13, 17, 1, 23).init.toArray shouldBe Array(17, 1, 23)
    }

    "have a get" in {
      Buffer.empty[String].get(0) shouldBe None
      Buffer.empty[String].get(-1) shouldBe None
      Buffer.empty[String].get(1) shouldBe None
      Buffer("a").get(-1) shouldBe None
      Buffer("a").get(0) shouldBe Some("a")
      Buffer("a").get(1) shouldBe None
      Buffer("a").get(2) shouldBe None
      Buffer("a", "b").get(0) shouldBe Some("a")
      Buffer("a", "b").get(1) shouldBe Some("b")
      Buffer("a", "b").get(2) shouldBe None
    }

    "have a peek with offset" in {
      Buffer("a").peek shouldBe "a"
      Buffer("a").peek(0) shouldBe "a"
      Buffer("a", "b").peek shouldBe "b"
      Buffer("a", "b").peek(0) shouldBe "b"
      Buffer("a", "b").peek(1) shouldBe "a"
    }

    "have a peekOption" in {
      Buffer.empty[String].peekOption(-1) shouldBe None
      Buffer.empty[String].peekOption(0) shouldBe None
      Buffer.empty[String].peekOption(1) shouldBe None
      Buffer("a").peekOption(0) shouldBe Some("a")
      Buffer("a").peekOption(1) shouldBe None
      Buffer("a", "b").peekOption(-1) shouldBe None
      Buffer("a", "b").peekOption(0) shouldBe Some("b")
      Buffer("a", "b").peekOption(1) shouldBe Some("a")
      Buffer("a", "b").peekOption(2) shouldBe None
    }

    "have an empty" in {
      Buffer.empty[String].length shouldBe 0
    }

    "have asSlice" in {
      Buffer.empty[String].asSlice.isEmpty shouldBe true
      Buffer.empty[String].asSlice shouldBe Slice.empty[String]
      Buffer("a").asSlice shouldBe Slice("a")
      Buffer("a", "b").asSlice shouldBe Slice("a", "b")
      Buffer("a", "b", "c").asSlice shouldBe Slice("a", "b", "c")
      Buffer("a", "a", "a").asSlice shouldBe Slice("a", "a", "a")
      Buffer("a", "a", "a").rewind(1).asSlice shouldBe Slice("a", "a")
      Buffer("a", "b", "c").tail.asSlice shouldBe Slice("a", "b")
    }

    "have slice" in {
      val b0 = Buffer.empty[String]
      b0.slice(0, 0).isEmpty shouldBe true
      b0.slice(0, 1).isEmpty shouldBe true
      b0.slice(0, 2).isEmpty shouldBe true
      b0.slice(0, 0) shouldBe Slice.empty[String]
      b0.slice(0, 1) shouldBe Slice.empty[String]
      b0.slice(0, 2) shouldBe Slice.empty[String]
      val b1 = Buffer("a")
      b1.slice(0, 1) shouldBe Slice("a")
      b1.slice(0, 2) shouldBe Slice("a")
      val b2 = Buffer("a", "b")
      b2.slice(0, 3) shouldBe Slice("a", "b")
      b2.slice(0, 2) shouldBe Slice("a", "b")
      b2.slice(1, 2) shouldBe Slice("b")
      b2.slice(1, 3) shouldBe Slice("b")
      b2.slice(0, 1) shouldBe Slice("a")
      b2.slice(0, 0) shouldBe Slice.empty[String]
      val b3_1 = Buffer("a", "b", "c")
      b3_1.slice(0, 4) shouldBe Slice("a", "b", "c")
      b3_1.slice(0, 3) shouldBe Slice("a", "b", "c")
      b3_1.slice(1, 3) shouldBe Slice("b", "c")
      b3_1.slice(2, 3) shouldBe Slice("c")
      b3_1.slice(3, 3) shouldBe Slice.empty[String]
      b3_1.slice(0, 2) shouldBe Slice("a", "b")
      b3_1.slice(1, 2) shouldBe Slice("b")
      b3_1.slice(0, 1) shouldBe Slice("a")
      b3_1.slice(0, 0) shouldBe Slice.empty[String]
      b3_1.tail.slice(0, 0) shouldBe Slice.empty[String]
      b3_1.tail.slice(0, 1) shouldBe Slice("a")
      Buffer("a", "b", "c").tail.slice(0, 2) shouldBe Slice("a", "b")
      val b3_2 = Buffer("a", "a", "a")
      b3_2.slice(0, 4) shouldBe Slice("a", "a", "a")
      b3_2.slice(0, 3) shouldBe Slice("a", "a", "a")
      b3_2.slice(0, 2) shouldBe Slice("a", "a")
      b3_2.slice(0, 1) shouldBe Slice("a")
      b3_2.rewind(1).slice(0, 3) shouldBe Slice("a", "a")
      b3_2.rewind(1).slice(0, 2) shouldBe Slice("a")
    }

  }

}
