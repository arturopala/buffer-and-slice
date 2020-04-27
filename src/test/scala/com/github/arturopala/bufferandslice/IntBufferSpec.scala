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
  }

}
