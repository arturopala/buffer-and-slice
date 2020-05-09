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

class IntBufferSpec extends AnyWordSpecCompat {

  "IntBuffer" should {
    "access and update a value at an index" in {
      val buffer = new IntBuffer()
      buffer(0) shouldBe 0
      buffer.length shouldBe 0
      buffer(1000) shouldBe 0
      buffer.length shouldBe 0
      buffer(0) = 3
      buffer(0) shouldBe 3
      buffer.length shouldBe 1
      buffer(111) = 13
      buffer(111) shouldBe 13
      buffer.length shouldBe 112
      buffer(9999) = -13
      buffer(9999) shouldBe -13
      buffer.length shouldBe 10000
    }

    "append values" in {
      val buffer = new IntBuffer()
      buffer.append(13).append(7).append(16)
      buffer(0) shouldBe 13
      buffer(1) shouldBe 7
      buffer(2) shouldBe 16
      buffer.append(9).append(90).append(900)
      buffer(3) shouldBe 9
      buffer(4) shouldBe 90
      buffer(5) shouldBe 900
      buffer(10) = 1000
      buffer(5) shouldBe 900
      buffer(6) shouldBe 0
      buffer(7) shouldBe 0
      buffer(8) shouldBe 0
      buffer(9) shouldBe 0
      buffer(10) shouldBe 1000
      buffer(11) shouldBe 0
      buffer(12) shouldBe 0
      buffer(13) shouldBe 0
      buffer(14) shouldBe 0
      buffer.append(1).append(2).append(3)
      buffer(11) shouldBe 1
      buffer(12) shouldBe 2
      buffer(13) shouldBe 3
      buffer(14) shouldBe 0
      buffer.toArray shouldBe Array(13, 7, 16, 9, 90, 900, 0, 0, 0, 0, 1000, 1, 2, 3)
      buffer.length shouldBe 14
      buffer(1000) = -13
      buffer(1000) shouldBe -13
      buffer.length shouldBe 1001
    }

    "modify value at an index" in {
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modify(0, _ + 10).toArray shouldBe Array(10, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modify(1, _ + 1).toArray shouldBe Array(10, 2, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modify(1, _ + 1).toArray shouldBe Array(10, 3, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modify(2, _ - 1).toArray shouldBe Array(10, 3, 1, 3, 4, 5, 6, 7, 8, 9)
      buffer.modify(1, _ - 1).toArray shouldBe Array(10, 2, 1, 3, 4, 5, 6, 7, 8, 9)
    }

    "map all values" in {
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.map(_ + 1).toArray shouldBe Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      buffer.map(_ * 2).toArray shouldBe Array(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
      buffer.map(_ - 1).toArray shouldBe Array(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
    }

    "modify all values" in {
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyAll(_ + 1).toArray shouldBe Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      buffer.modifyAll(_ * 2).toArray shouldBe Array(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
      buffer.modifyAll(_ - 1).toArray shouldBe Array(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
    }

    "modify all values fulfilling the predicate" in {
      val even: Int => Boolean = _ % 2 == 0
      val odd: Int => Boolean = _  % 2 != 0
      val d3: Int => Boolean = _   % 3 == 0
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyAllWhen(_ + 1, odd).toArray shouldBe Array(0, 2, 2, 4, 4, 6, 6, 8, 8, 10)
      buffer.modifyAllWhen(_ * 2, odd).toArray shouldBe Array(0, 2, 2, 4, 4, 6, 6, 8, 8, 10)
      buffer.modifyAllWhen(_ * 2, even).toArray shouldBe Array(0, 4, 4, 8, 8, 12, 12, 16, 16, 20)
      buffer.modifyAllWhen(_ - 1, odd).toArray shouldBe Array(0, 4, 4, 8, 8, 12, 12, 16, 16, 20)
      buffer.modifyAllWhen(_ - 1, d3).toArray shouldBe Array(-1, 4, 4, 8, 8, 11, 11, 16, 16, 20)
      buffer.modifyAllWhen(_ - 2, odd).toArray shouldBe Array(-3, 4, 4, 8, 8, 9, 9, 16, 16, 20)
      buffer.modifyAllWhen(_ + 1, even).toArray shouldBe Array(-3, 5, 5, 9, 9, 9, 9, 17, 17, 21)
    }

    "modify values in the range" in {
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyRange(0, 2, _ + 10).toArray shouldBe Array(10, 11, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyRange(1, 5, _ + 1).toArray shouldBe Array(10, 12, 3, 4, 5, 5, 6, 7, 8, 9)
      buffer.modifyRange(2, 9, _ + 1).toArray shouldBe Array(10, 12, 4, 5, 6, 6, 7, 8, 9, 9)
      buffer.modifyRange(2, 1, _ - 1).toArray shouldBe Array(10, 12, 4, 5, 6, 6, 7, 8, 9, 9)
      buffer.modifyRange(0, 1, _ - 1).toArray shouldBe Array(9, 12, 4, 5, 6, 6, 7, 8, 9, 9)

      IntBuffer(0, 1).modifyRange(2, 5, _ + 1).toArray shouldBe Array(0, 1)
      IntBuffer(0, 1).modifyRange(0, 0, _ + 1).toArray shouldBe Array(0, 1)
    }

    "modify values in the range fulfilling the predicate" in {
      val even: Int => Boolean = _ % 2 == 0
      val odd: Int => Boolean = _  % 2 == 1
      val buffer = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyRangeWhen(0, 2, _ + 10, even).toArray shouldBe Array(10, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      buffer.modifyRangeWhen(1, 5, _ + 1, even).toArray shouldBe Array(10, 1, 3, 3, 5, 5, 6, 7, 8, 9)
      buffer.modifyRangeWhen(2, 9, _ + 1, odd).toArray shouldBe Array(10, 1, 4, 4, 6, 6, 6, 8, 8, 9)
      buffer.modifyRangeWhen(2, 1, _ - 1, even).toArray shouldBe Array(10, 1, 4, 4, 6, 6, 6, 8, 8, 9)
      buffer.modifyRangeWhen(0, 1, _ - 1, even).toArray shouldBe Array(9, 1, 4, 4, 6, 6, 6, 8, 8, 9)

      IntBuffer(0, 1).modifyRangeWhen(2, 5, _ + 1, even).toArray shouldBe Array(0, 1)
      IntBuffer(0, 1).modifyRangeWhen(0, 0, _ + 1, even).toArray shouldBe Array(0, 1)
    }

    "append a slice" in {
      val buffer = IntBuffer()
      buffer.appendSlice(IntSlice()).toArray shouldBe Array.empty[Int]
      buffer.appendSlice(IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5)
      buffer.appendSlice(IntSlice(2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5)
      buffer.appendSlice(IntSlice(3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5)
      buffer.appendSlice(IntSlice()).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5)
      buffer.appendSlice(IntSlice(0, 0, 0, 0)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5, 0, 0, 0, 0)
    }

    "insert a slice" in {
      val buffer = IntBuffer()
      buffer.insertSlice(0, IntSlice()).toArray shouldBe Array.empty[Int]
      buffer.insertSlice(3, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(0, 0, 0, 1, 2, 3, 4, 5)
      buffer.insertSlice(1, IntSlice(2, 3, 4, 5)).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 4, 5)
      buffer.insertSlice(10, IntSlice(3, 4, 5)).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 3, 4, 5, 4, 5)
      buffer.insertSlice(7, IntSlice()).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 3, 4, 5, 4, 5)
      buffer.insertSlice(0, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 0, 2, 3, 4, 5, 0, 0, 1, 2, 3,
        3, 4, 5, 4, 5)
      buffer.insertSlice(25, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 0, 2, 3, 4, 5, 0, 0, 1, 2,
        3, 3, 4, 5, 4, 5, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5)
    }

    "replace from a slice" in {
      val buffer = IntBuffer()
      buffer.replaceFromSlice(0, IntSlice()).toArray shouldBe Array.empty[Int]
      buffer.replaceFromSlice(3, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(0, 0, 0, 1, 2, 3, 4, 5)
      buffer.replaceFromSlice(0, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 5)
      buffer.replaceFromSlice(10, IntSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 5, 0, 0, 1, 2, 3,
        4, 5)
      buffer.replaceFromSlice(7, IntSlice(1, 2, 3)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5)
      buffer.replaceFromSlice(4, IntSlice(0, 0, 0, 0, 0, 0, 0, 0)).toArray shouldBe Array(1, 2, 3, 4, 0, 0, 0, 0, 0, 0,
        0, 0, 3, 4, 5)
    }

    "increment a value at index" in {
      val buffer = IntBuffer(1, 2, 3, 4, 5, 6)
      buffer.increment(3).toArray shouldBe Array(1, 2, 3, 5, 5, 6)
      buffer.increment(1).toArray shouldBe Array(1, 3, 3, 5, 5, 6)
      buffer.increment(5).toArray shouldBe Array(1, 3, 3, 5, 5, 7)
    }

    "decrement a value at index" in {
      val buffer = IntBuffer(1, 2, 3, 4, 5, 6)
      buffer.decrement(3).toArray shouldBe Array(1, 2, 3, 3, 5, 6)
      buffer.decrement(1).toArray shouldBe Array(1, 1, 3, 3, 5, 6)
      buffer.decrement(5).toArray shouldBe Array(1, 1, 3, 3, 5, 5)
    }

    "have an empty" in {
      IntBuffer.empty.length shouldBe 0
    }
  }

}
