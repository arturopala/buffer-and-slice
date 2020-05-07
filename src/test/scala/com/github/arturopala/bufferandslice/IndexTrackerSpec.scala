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

class IndexTrackerSpec extends AnyWordSpecCompat {

  "IndexTracker" should {

    "modify a buffer of indexes tracking Buffer.shiftRight" in {
      val indexes = IntBuffer(0, 3, 7, 11, 28)
      IndexTracker.trackShiftRight(5, 10, indexes).toArray shouldBe Array(0, 3, 17, 21, 38)
      IndexTracker.trackShiftRight(5, 0, indexes).toArray shouldBe Array(0, 3, 17, 21, 38)
      IndexTracker.trackShiftRight(-1, 5, indexes).toArray shouldBe Array(0, 3, 17, 21, 38)
      IndexTracker.trackShiftRight(20, 3, indexes).toArray shouldBe Array(0, 3, 17, 24, 41)
      IndexTracker.trackShiftRight(0, 1, indexes).toArray shouldBe Array(1, 4, 18, 25, 42)
      IndexTracker.trackShiftRight(30, 7, indexes).toArray shouldBe Array(1, 4, 18, 25, 49)
      IndexTracker.trackShiftRight(4, 1, indexes).toArray shouldBe Array(1, 5, 19, 26, 50)
    }

    "modify a list of indexes tracking Buffer.shiftRight" in {
      val indexes = List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftRight(5, 10, indexes) shouldBe List(0, 3, 17, 21, 38)
      IndexTracker.trackShiftRight(5, -10, indexes) shouldBe List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftRight(-5, 10, indexes) shouldBe List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftRight(10, 3, indexes) shouldBe List(0, 3, 7, 14, 31)
      IndexTracker.trackShiftRight(0, 1, indexes) shouldBe List(1, 4, 8, 12, 29)
      IndexTracker.trackShiftRight(25, 7, indexes) shouldBe List(0, 3, 7, 11, 35)
      IndexTracker.trackShiftRight(7, 1, indexes) shouldBe List(0, 3, 8, 12, 29)
    }

    "modify a buffer of indexes tracking Buffer.shiftLeft" in {
      val indexes = IntBuffer(0, 3, 7, 11, 28)
      IndexTracker.trackShiftLeft(5, 2, indexes).toArray shouldBe Array(0, 5, 9, 26)
      IndexTracker.trackShiftLeft(5, -2, indexes).toArray shouldBe Array(0, 5, 9, 26)
      IndexTracker.trackShiftLeft(-1, 2, indexes).toArray shouldBe Array(0, 5, 9, 26)
      IndexTracker.trackShiftLeft(8, 1, indexes).toArray shouldBe Array(0, 5, 8, 25)
      IndexTracker.trackShiftLeft(8, 2, indexes).toArray shouldBe Array(0, 5, 6, 23)
      IndexTracker.trackShiftLeft(6, 4, indexes).toArray shouldBe Array(0, 2, 19)
      IndexTracker.trackShiftLeft(0, 2, indexes).toArray shouldBe Array(0, 17)
      IndexTracker.trackShiftLeft(10, 10, indexes).toArray shouldBe Array(7)
      IndexTracker.trackShiftLeft(5, 8, indexes).isEmpty shouldBe true
    }

    "modify a list of indexes tracking Buffer.shiftLeft" in {
      val indexes = List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftLeft(5, 2, indexes) shouldBe List(0, 5, 9, 26)
      IndexTracker.trackShiftLeft(5, -1, indexes) shouldBe List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftLeft(-1, 2, indexes) shouldBe List(0, 3, 7, 11, 28)
      IndexTracker.trackShiftLeft(8, 1, indexes) shouldBe List(0, 3, 10, 27)
      IndexTracker.trackShiftLeft(8, 2, indexes) shouldBe List(0, 3, 9, 26)
      IndexTracker.trackShiftLeft(8, 5, indexes) shouldBe List(0, 6, 23)
      IndexTracker.trackShiftLeft(1, 5, indexes) shouldBe List(2, 6, 23)
      IndexTracker.trackShiftLeft(23, 20, indexes) shouldBe List(0, 8)
      IndexTracker.trackShiftLeft(5, 30, indexes) shouldBe Nil
    }

    "modify a buffer of indexes tracking Buffer.moveRangeRight" in {
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeRight(5, 10, 3, indexes).toArray shouldBe Array(0, 1, 2, 3, 4, 8, 9, 10, 11, 12, 5, 6,
        7, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeRight(11, 14, 2, indexes).toArray shouldBe Array(0, 1, 2, 3, 4, 8, 9, 10, 13, 14, 5, 6,
        7, 15, 11, 12, 16)
      IndexTracker.trackMoveRangeRight(3, 10, 1, indexes).toArray shouldBe Array(0, 1, 2, 4, 5, 9, 10, 3, 13, 14, 6, 7,
        8, 15, 11, 12, 16)
      IndexTracker.trackMoveRangeRight(0, 3, 3, indexes).toArray shouldBe Array(3, 4, 5, 1, 2, 9, 10, 0, 13, 14, 6, 7,
        8, 15, 11, 12, 16)
    }

    "modify a list of indexes tracking Buffer.moveRangeRight" in {
      val indexes = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeRight(5, 10, 3, indexes) shouldBe List(0, 1, 2, 3, 4, 8, 9, 10, 11, 12, 5, 6, 7, 13,
        14, 15, 16)
      IndexTracker.trackMoveRangeRight(11, 14, 2, indexes) shouldBe List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 14, 15,
        11, 12, 16)
      IndexTracker.trackMoveRangeRight(3, 10, 1, indexes) shouldBe List(0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 3, 11, 12, 13,
        14, 15, 16)
      IndexTracker.trackMoveRangeRight(0, 3, 3, indexes) shouldBe List(3, 4, 5, 0, 1, 2, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16)
    }

    "modify a buffer of indexes tracking Buffer.moveRangeLeft" in {
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeLeft(5, 10, 3, indexes).toArray shouldBe Array(0, 1, 7, 8, 9, 2, 3, 4, 5, 6, 10, 11,
        12, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeLeft(10, 13, 5, indexes).toArray shouldBe Array(0, 1, 10, 11, 12, 2, 3, 4, 8, 9, 5, 6,
        7, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeLeft(8, 11, 1, indexes).toArray shouldBe Array(0, 1, 9, 11, 12, 2, 3, 4, 7, 8, 5, 6,
        10, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeLeft(0, 5, 1, indexes).toArray shouldBe Array(0, 1, 10, 12, 13, 2, 3, 4, 8, 9, 6, 7,
        11, 14, 15, 16, 17)
    }

    "modify a list of indexes tracking Buffer.moveRangeLeft" in {
      val indexes = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackMoveRangeLeft(5, 10, 3, indexes) shouldBe List(0, 1, 7, 8, 9, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14,
        15, 16)
      IndexTracker.trackMoveRangeLeft(10, 13, 5, indexes) shouldBe List(0, 1, 2, 3, 4, 8, 9, 10, 11, 12, 5, 6, 7, 13,
        14, 15, 16)
      IndexTracker.trackMoveRangeLeft(0, 5, 2, indexes) shouldBe List(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18)

    }

    "modify a buffer of indexes tracking Buffer.swapRange" in {
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackSwapRange(3, 9, 4, indexes).toArray shouldBe Array(0, 1, 2, 9, 10, 11, 12, 7, 8, 3, 4, 5, 6, 13,
        14, 15, 16)
      IndexTracker.trackSwapRange(9, 3, 4, indexes).toArray shouldBe Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        14, 15, 16)
      IndexTracker.trackSwapRange(3, 5, 10, indexes).toArray shouldBe Array(0, 1, 2, -1, -1, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 15, 16)
      IndexTracker.trackSwapRange(4, 1, 7, indexes).toArray shouldBe Array(0, 4, 5, -1, -1, 6, 7, 8, 9, 10, -1, -1, -1,
        11, 12, 15, 16)
    }

    "modify a list of indexes tracking Buffer.swapRange" in {
      val indexes = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
      IndexTracker.trackSwapRange(3, 9, 4, indexes) shouldBe List(0, 1, 2, 9, 10, 11, 12, 7, 8, 3, 4, 5, 6, 13, 14, 15,
        16)
      IndexTracker.trackSwapRange(9, 3, 4, indexes) shouldBe List(0, 1, 2, 9, 10, 11, 12, 7, 8, 3, 4, 5, 6, 13, 14, 15,
        16)
      IndexTracker.trackSwapRange(3, 5, 10, indexes) shouldBe List(0, 1, 2, -1, -1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15,
        16)
      IndexTracker.trackSwapRange(5, 3, 10, indexes) shouldBe List(0, 1, 2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, -1, -1,
        15, 16)
    }

    "cross-check Indexes.movingRangeRight with Buffer.moveRangeRight" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.moveRangeRight(2, 6, 3)
      IndexTracker.trackMoveRangeRight(2, 6, 3, indexes)
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          initialValues(i1) shouldBe values(i2)
      }
    }

    "cross-check Indexes.movingRangeLeft with Buffer.moveRangeLeft" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.moveRangeLeft(2, 6, 5)
      IndexTracker.trackMoveRangeLeft(2, 6, 5, indexes)
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          initialValues(i1) shouldBe values(i2)
      }
    }

    "cross-check (2) Indexes.movingRangeLeft with Buffer.moveRangeLeft" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.moveRangeLeft(5, 10, 3)
      IndexTracker.trackMoveRangeLeft(5, 10, 3, indexes)
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          initialValues(i1) shouldBe values(i2)
      }
    }

    "cross-check Indexes.swappingRange with Buffer.swapRange" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.swapRange(3, 7, 3)
      IndexTracker.trackSwapRange(3, 7, 3, indexes)
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          initialValues(i1) shouldBe values(i2)
      }
    }

    "cross-check (2) Indexes.swappingRange with Buffer.swapRange" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.swapRange(6, 3, 4)
      println(values.toArray.mkString(","))
      IndexTracker.trackSwapRange(6, 3, 4, indexes)
      println(indexes.toArray.mkString(","))
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          if (i2 != -1) initialValues(i1) shouldBe values(i2)
      }
    }

    "cross-check (3) Indexes.swappingRange with Buffer.swapRange" in {
      val values = Buffer("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
      val indexes = IntBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val initialValues = values.copy
      val initialIndexes = indexes.copy
      values.swapRange(2, 5, 4)
      println(values.toArray.mkString(","))
      IndexTracker.trackSwapRange(2, 5, 4, indexes)
      println(indexes.toArray.mkString(","))
      initialIndexes.iterator.zip(indexes.iterator).foreach {
        case (i1, i2) =>
          if (i2 != -1) initialValues(i1) shouldBe values(i2)
      }
    }
  }

}
