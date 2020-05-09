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

class ByteSliceSpec extends AnyWordSpecCompat {

  "ByteSlice" should {
    "wrap a whole array" in {
      ByteSlice.of(Array(0, 1, 2, 3, 4)).toArray[Byte] shouldBe Array(0, 1, 2, 3, 4)
      ByteSlice.of(Array.empty[Byte]).toArray[Byte] shouldBe Array.empty[Byte]
    }

    "wrap a slice of an array" in {
      ByteSlice.of(Array(0, 1, 2, 3, 4), 1, 3).toArray[Byte] shouldBe Array(1, 2)
    }

    "have isEmpty" in {
      ByteSlice.of(Array(0, 1, 2, 3, 4)).isEmpty shouldBe false
      ByteSlice.of(Array.empty[Byte], 0, 0).isEmpty shouldBe true
    }

    "iterate over slice of values" in {
      ByteSlice.of(Array(2, 4, 3, 7, 1)).iterator.toList shouldBe List(2, 4, 3, 7, 1)
      ByteSlice.of(Array(2, 4, 3, 7, 1), 1, 5).iterator.toList shouldBe List(4, 3, 7, 1)
      ByteSlice.of(Array(2, 4, 3, 7, 1), 1, 4).iterator.toList shouldBe List(4, 3, 7)
      ByteSlice.of(Array(2, 4, 3, 7, 1), 2, 3).iterator.toList shouldBe List(3)
    }

    "reverse-iterate over slice of values" in {
      ByteSlice.of(Array(2, 4, 3, 7, 1)).reverseIterator.toList shouldBe List(2, 4, 3, 7, 1)
        .map(_.toByte)
        .reverse
      ByteSlice.of(Array(2, 4, 3, 7, 1), 1, 5).reverseIterator.toList shouldBe List(4, 3, 7, 1)
        .map(_.toByte)
        .reverse
      ByteSlice.of(Array(2, 4, 3, 7, 1), 1, 4).reverseIterator.toList shouldBe List(4, 3, 7)
        .map(_.toByte)
        .reverse
      ByteSlice.of(Array(2, 4, 3, 7, 1), 2, 3).reverseIterator.toList shouldBe List(3).map(_.toByte).reverse
      ByteSlice.of(Array(2, 4, 3, 7, 1), 0, 0).reverseIterator.toList shouldBe List()
      ByteSlice.of(Array(2, 4, 3, 7, 1), 5, 5).reverseIterator.toList shouldBe List()
    }

    "reverse-iterate with filter over slice of values" in {
      ByteSlice.of(Array.empty[Byte]).reverseIterator(_                % 2 == 0).toList shouldBe Nil
      ByteSlice.of(Array(1)).reverseIterator(_                         % 2 == 0).toList shouldBe Nil
      ByteSlice.of(Array(2, 1)).reverseIterator(_                      % 2 == 0).toList shouldBe List(2)
      ByteSlice.of(Array(2, 1)).reverseIterator(_                      % 2 != 0).toList shouldBe List(1)
      ByteSlice.of(Array(1)).reverseIterator(_                         % 2 != 0).toList shouldBe List(1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).reverseIterator(_ % 2 == 0).toList shouldBe List(0, 2, 6, 4, 2)
    }

    "map slice of values" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).map(_ * 10).toArray[Int] shouldBe Array(10, 20, 30, 40, 50, 60, 20,
        0, 10)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).map(_ * 10).toArray[Int] shouldBe Array(30, 40, 50, 60)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 3).map(_ * 10).toArray[Int] shouldBe Array(30)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 2).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 0, 0).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      Slice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 9, 9).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
    }

    "count values in slice matching predicate" in {
      ByteSlice(0, 2, 0, 4, 5, 0, 2, 8, 0).count(_ == 0) shouldBe 4
      ByteSlice(3, 4, 5).count(_ == 0) shouldBe 0
      Slice(0, 0, 0, 0, 0).count(_ == 0) shouldBe 5
      ByteSlice(0).count(_ == 0) shouldBe 1
      ByteSlice(1).count(_ == 0) shouldBe 0
      ByteSlice().count(_ == 0) shouldBe 0
    }

    "top a value by an index" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).apply(3) shouldBe 4
      Slice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 9).apply(3) shouldBe 6
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 5, 9).apply(3) shouldBe 1
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).apply(3) shouldBe 6
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).apply(14)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).apply(-1)
      }
    }

    "update a value within a slice" in {
      ByteSlice.of(Array[Byte](1, 2, 3, 4)).update(3, 3.toByte).apply(3) shouldBe 3
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).update(3, 3.toByte).apply(4) shouldBe 5
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 9).update(3, 4.toByte).apply(3) shouldBe 4
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 9).update(3, 4.toByte).apply(2) shouldBe 5
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 5, 9).update(3, 0.toByte).apply(3) shouldBe 0
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).update(3, 1.toByte).apply(3) shouldBe 1
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).update(17, 0.toByte)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 6).update(-1, 0.toByte)
      }
    }

    "have a length" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).length shouldBe 9
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 0, 0).length shouldBe 0
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 0, 1).length shouldBe 1
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 8).length shouldBe 6
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 4, 5).length shouldBe 1
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 5, 5).length shouldBe 0
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 5, 9).length shouldBe 4
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 0, 1).length shouldBe 1
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 9, 9).length shouldBe 0
    }

    "have a slice" in {
      ByteSlice().slice(-5, 10) shouldBe ByteSlice()
      ByteSlice(1, 2, 3).slice(-5, 10) shouldBe ByteSlice(1, 2, 3)
      ByteSlice(1, 2, 3, 4, 5, 6, 2, 0, 1).slice(-5, 5) shouldBe ByteSlice(1, 2, 3, 4, 5)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 8).slice(-5, 5) shouldBe ByteSlice(3, 4, 5, 6, 2)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 2, 5).slice(5, 8) shouldBe ByteSlice()
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 5, 5).slice(1, 2) shouldBe ByteSlice()
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 3, 5).slice(1, 2) shouldBe ByteSlice(5)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1), 0, 9).slice(2, 3) shouldBe ByteSlice(3)
    }

    "have a drop" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).drop(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).drop(3).toList shouldBe List(4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).drop(8).toList shouldBe List(1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).drop(9).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3)).drop(3).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3)).drop(9).toList shouldBe Nil
      ByteSlice.of(Array(1, 2)).drop(3).toList shouldBe Nil
      ByteSlice.of(Array(1)).drop(3).toList shouldBe Nil
      ByteSlice.of(Array.empty[Byte]).drop(3).toList shouldBe Nil
    }

    "have a dropRight" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).dropRight(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).dropRight(3).toList shouldBe List(1, 2, 3, 4, 5, 6)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).dropRight(8).toList shouldBe List(1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).dropRight(9).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3)).dropRight(3).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3)).dropRight(9).toList shouldBe Nil
      ByteSlice.of(Array(1, 2)).dropRight(3).toList shouldBe Nil
      ByteSlice.of(Array(1)).dropRight(3).toList shouldBe Nil
      ByteSlice.of(Array.empty[Byte]).dropRight(3).toList shouldBe Nil
    }

    "have a take" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).take(0).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).take(3).toList shouldBe List(1, 2, 3)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).take(8).toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).take(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3)).take(3).toList shouldBe List(1, 2, 3)
      ByteSlice.of(Array(1, 2, 3)).take(9).toList shouldBe List(1, 2, 3)
      ByteSlice.of(Array(1, 2)).take(3).toList shouldBe List(1, 2)
      ByteSlice.of(Array(1)).take(3).toList shouldBe List(1)
      ByteSlice.of(Array.empty[Byte]).take(3).toList shouldBe Nil
    }

    "have a takeRight" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).takeRight(0).toList shouldBe Nil
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).takeRight(3).toList shouldBe List(2, 0, 1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).takeRight(8).toList shouldBe List(2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).takeRight(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(1, 2, 3)).takeRight(3).toList shouldBe List(1, 2, 3)
      ByteSlice.of(Array(1, 2, 3)).takeRight(9).toList shouldBe List(1, 2, 3)
      ByteSlice.of(Array(1, 2)).takeRight(3).toList shouldBe List(1, 2)
      ByteSlice.of(Array(1)).takeRight(3).toList shouldBe List(1)
      ByteSlice.of(Array.empty[Byte]).takeRight(3).toList shouldBe Nil
    }

    "have a head" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).head shouldBe 1
      ByteSlice.of(Array(4)).head shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ByteSlice.empty.head)
    }

    "have a headOption" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).headOption shouldBe Some(1)
      ByteSlice.of(Array(4)).headOption shouldBe Some(4)
      ByteSlice.empty.headOption shouldBe None
    }

    "have a tail" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).tail.toList shouldBe List(2, 3, 4, 5, 6, 2, 0, 1)
      ByteSlice.of(Array(4)).tail.toList shouldBe Nil
      ByteSlice.empty.tail.toList shouldBe Nil
    }

    "have a last" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).last shouldBe 1
      ByteSlice.of(Array(4)).last shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ByteSlice.empty.head)
    }

    "have a lastOption" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).lastOption shouldBe Some(1)
      ByteSlice.of(Array(4)).lastOption shouldBe Some(4)
      ByteSlice.empty.lastOption shouldBe None
    }

    "have an init" in {
      ByteSlice.of(Array(1, 2, 3, 4, 5, 6, 2, 0, 1)).init.toList shouldBe List(1, 2, 3, 4, 5, 6, 2, 0)
      ByteSlice.of(Array(4)).init.toList shouldBe Nil
      ByteSlice.empty.init.toList shouldBe Nil
    }

    "have a copyToArray" in {
      ByteSlice().copyToArray(0, new Array[Byte](0)) shouldBe Array.empty[Byte]
      ByteSlice(1, 2, 3).copyToArray(0, new Array[Byte](10)) shouldBe Array(1, 2, 3, 0, 0, 0, 0, 0, 0, 0)
      ByteSlice(1, 2, 3).copyToArray(5, new Array[Byte](10)) shouldBe Array(0, 0, 0, 0, 0, 1, 2, 3, 0, 0)
      ByteSlice(1, 1, 1, 1, 1).copyToArray(5, new Array[Byte](10)) shouldBe Array(0, 0, 0, 0, 0, 1, 1, 1, 1, 1)
      ByteSlice(3, 2, 1, 2, 3).copyToArray(0, new Array[Byte](5)) shouldBe Array(3, 2, 1, 2, 3)
    }

    "have a detach" in {
      val array = Array(1, 2, 3, 4, 5, 6).map(_.toByte)
      val slice = ByteSlice.of(array)
      slice(1) shouldBe 2
      array(1) = 3
      slice(1) shouldBe 3
      val detached = slice.detach
      slice(1) shouldBe 3
      detached(1) shouldBe 3
      array(1) = 7
      slice(1) shouldBe 7
      detached(1) shouldBe 3
    }

    "have a find" in {
      ByteSlice.empty.find(_ > 0) shouldBe None
      ByteSlice(0).find(_ > 0) shouldBe None
      ByteSlice(1).find(_ > 0) shouldBe Some(1)
      ByteSlice(1, 2, 3).find(_ > 0) shouldBe Some(1)
      ByteSlice(1, 2, 3).find(_ > 0) shouldBe Some(1)
      ByteSlice(1, 2, 3).find(_ > 1) shouldBe Some(2)
      ByteSlice(1, 2, 3).find(_ > 3) shouldBe None
    }

    "have an exists" in {
      ByteSlice.empty.exists(_ > 0) shouldBe false
      ByteSlice(0).exists(_ > 0) shouldBe false
      ByteSlice(1).exists(_ > 0) shouldBe true
      ByteSlice(1, 2, 3).exists(_ > 0) shouldBe true
      ByteSlice(1, 2, 3).exists(_ > 0) shouldBe true
      ByteSlice(1, 2, 3).exists(_ > 1) shouldBe true
      ByteSlice(1, 2, 3).exists(_ > 3) shouldBe false
    }
  }

}
