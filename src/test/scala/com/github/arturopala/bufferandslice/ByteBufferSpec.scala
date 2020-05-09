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

class ByteBufferSpec extends AnyWordSpecCompat {

  "ByteBuffer" should {
    "access and update a value at an index" in {
      val buffer = new ByteBuffer()
      buffer(0) shouldBe 0
      buffer.length shouldBe 0
      buffer(6) shouldBe 0
      buffer.length shouldBe 0
      buffer(0) = 3
      buffer(0) shouldBe 3
      buffer.length shouldBe 1
      buffer(111) = 7
      buffer(111) shouldBe 7
      buffer.length shouldBe 112
      buffer(9999) = 3
      buffer(9999) shouldBe 3
      buffer.length shouldBe 10000
    }

    "append values" in {
      val buffer = new ByteBuffer()
      buffer.append(7).append(7).append(4)
      buffer(0) shouldBe 7
      buffer(1) shouldBe 7
      buffer(2) shouldBe 4
      buffer.append(1).append(5).append(2)
      buffer(3) shouldBe 1
      buffer(4) shouldBe 5
      buffer(5) shouldBe 2
      buffer(10) = 6
      buffer(5) shouldBe 2
      buffer(6) shouldBe 0
      buffer(7) shouldBe 0
      buffer(8) shouldBe 0
      buffer(9) shouldBe 0
      buffer(10) shouldBe 6
      buffer(11) shouldBe 0
      buffer(12) shouldBe 0
      buffer(7) shouldBe 0
      buffer(14) shouldBe 0
      buffer.append(1).append(2).append(3)
      buffer(11) shouldBe 1
      buffer(12) shouldBe 2
      buffer(13) shouldBe 3
      buffer(14) shouldBe 0
      buffer.toArray shouldBe Array(7, 7, 4, 1, 5, 2, 0, 0, 0, 0, 6, 1, 2, 3)
      buffer.length shouldBe 14
      buffer(1000) = 3
      buffer(1000) shouldBe 3
      buffer.length shouldBe 1001
    }

    "append a slice" in {
      val buffer = new ByteBuffer()
      buffer.appendSlice(ByteSlice()).toArray shouldBe Array.empty[Byte]
      buffer.appendSlice(ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5)
      buffer.appendSlice(ByteSlice(2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5)
      buffer.appendSlice(ByteSlice(3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5)
      buffer.appendSlice(ByteSlice()).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5)
      buffer.appendSlice(ByteSlice(0, 0, 0, 0)).toArray shouldBe Array(1, 2, 3, 4, 5, 2, 3, 4, 5, 3, 4, 5, 0, 0, 0, 0)
    }

    "insert a slice" in {
      val buffer = new ByteBuffer()
      buffer.insertSlice(0, ByteSlice()).toArray shouldBe Array.empty[Byte]
      buffer.insertSlice(3, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(0, 0, 0, 1, 2, 3, 4, 5)
      buffer.insertSlice(1, ByteSlice(2, 3, 4, 5)).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 4, 5)
      buffer.insertSlice(10, ByteSlice(3, 4, 5)).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 3, 4, 5, 4, 5)
      buffer.insertSlice(7, ByteSlice()).toArray shouldBe Array(0, 2, 3, 4, 5, 0, 0, 1, 2, 3, 3, 4, 5, 4, 5)
      buffer.insertSlice(0, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 0, 2, 3, 4, 5, 0, 0, 1, 2,
        3, 3, 4, 5, 4, 5)
      buffer.insertSlice(25, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 0, 2, 3, 4, 5, 0, 0, 1, 2,
        3, 3, 4, 5, 4, 5, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5)
    }

    "replace from a slice" in {
      val buffer = new ByteBuffer()
      buffer.replaceFromSlice(0, ByteSlice()).toArray shouldBe Array.empty[Byte]
      buffer.replaceFromSlice(3, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(0, 0, 0, 1, 2, 3, 4, 5)
      buffer.replaceFromSlice(0, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 5)
      buffer.replaceFromSlice(10, ByteSlice(1, 2, 3, 4, 5)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 5, 0, 0, 1, 2,
        3, 4, 5)
      buffer.replaceFromSlice(7, ByteSlice(1, 2, 3)).toArray shouldBe Array(1, 2, 3, 4, 5, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5)
      buffer.replaceFromSlice(4, ByteSlice(0, 0, 0, 0, 0, 0, 0, 0)).toArray shouldBe Array(1, 2, 3, 4, 0, 0, 0, 0, 0, 0,
        0, 0, 3, 4, 5)
    }

    "have an empty" in {
      ByteBuffer.empty.length shouldBe 0
    }
  }

}
