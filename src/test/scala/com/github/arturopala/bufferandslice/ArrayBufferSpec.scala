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

    "shift values right" in {
      val buffer = new ArrayBuffer(Array("a", "b", "c"))
      buffer.shiftRight(1, 5)
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", "c")
      buffer.length shouldBe 8
      buffer.shiftRight(7, 1)
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", "c", "c")
      buffer.length shouldBe 9
      buffer.shiftRight(5, 2)
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11
      buffer.shiftRight(5, -2) // ignore negative distance
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11
      buffer.shiftRight(-5, 5) // ignore negative index
      buffer.toArray shouldBe Array("a", "b", "c", null, null, null, "b", null, "b", "c", "c")
      buffer.length shouldBe 11

      val buffer2 = new ArrayBuffer(Array.empty[String])
      buffer2.shiftRight(0, 10)
      buffer2.length shouldBe 0
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
  }

}
