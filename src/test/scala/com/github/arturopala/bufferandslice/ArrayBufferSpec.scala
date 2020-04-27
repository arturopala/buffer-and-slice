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

import com.github.arturopala.bufferandslice.ArrayBuffer

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

  }

}
