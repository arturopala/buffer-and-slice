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

class IntSliceSpec extends AnyWordSpecCompat {

  "IntSlice" should {
    "wrap a whole array" in {
      IntSlice.of(Array(0, 1, 2, 3, 4)).toArray[Int] shouldBe Array(0, 1, 2, 3, 4)
      IntSlice.of(Array(7, 13, 17, 19, 23)).toArray[Int] shouldBe Array(7, 13, 17, 19, 23)
      IntSlice.of(Array.empty[Int]).toArray[Int] shouldBe Array.empty[Int]
    }

    "wrap a slice of an array" in {
      IntSlice.of(Array(0, 1, 2, 3, 4), 1, 3).toArray[Int] shouldBe Array(1, 2)
      IntSlice.of(Array(7, 13, 17, 19, 23), 2, 5).toArray[Int] shouldBe Array(17, 19, 23)
      IntSlice.of(Array(7, 13, 17, 19, 23), 4, 4).toArray[Int] shouldBe Array.empty[Int]
    }

    "have isEmpty" in {
      IntSlice.of(Array(0, 1, 2, 3, 4)).isEmpty shouldBe false
      IntSlice.of(Array(7, 13, 17, 19, 23)).isEmpty shouldBe false
      IntSlice.of(Array(7, 13, 17, 19, 23), 2, 4).isEmpty shouldBe false
      IntSlice.of(Array(7, 13, 17, 19, 23), 2, 2).isEmpty shouldBe true
      IntSlice.of(Array.empty[Int], 0, 0).isEmpty shouldBe true
    }

    "iterate over slice of values" in {
      IntSlice.of(Array(7, 13, 17, 19, 23)).iterator.toList shouldBe List(7, 13, 17, 19, 23)
      IntSlice.of(Array(7, 13, 17, 19, 23), 1, 5).iterator.toList shouldBe List(13, 17, 19, 23)
      IntSlice.of(Array(7, 13, 17, 19, 23), 1, 4).iterator.toList shouldBe List(13, 17, 19)
      IntSlice.of(Array(7, 13, 17, 19, 23), 2, 3).iterator.toList shouldBe List(17)
    }

    "reverse-iterate over slice of values" in {
      IntSlice.of(Array(7, 13, 17, 19, 23)).reverseIterator.toList shouldBe List(7, 13, 17, 19, 23).reverse
      IntSlice.of(Array(7, 13, 17, 19, 23), 1, 5).reverseIterator.toList shouldBe List(13, 17, 19, 23).reverse
      IntSlice.of(Array(7, 13, 17, 19, 23), 1, 4).reverseIterator.toList shouldBe List(13, 17, 19).reverse
      IntSlice.of(Array(7, 13, 17, 19, 23), 2, 3).reverseIterator.toList shouldBe List(17).reverse
      IntSlice.of(Array(7, 13, 17, 19, 23), 0, 0).reverseIterator.toList shouldBe List()
      IntSlice.of(Array(7, 13, 17, 19, 23), 5, 5).reverseIterator.toList shouldBe List()
    }

    "reverse-iterate with filter over slice of values" in {
      IntSlice.of(Array.empty[Int]).reverseIterator(_                       % 2 == 0).toList shouldBe Nil
      IntSlice.of(Array(1)).reverseIterator(_                               % 2 == 0).toList shouldBe Nil
      IntSlice.of(Array(2, 1)).reverseIterator(_                            % 2 == 0).toList shouldBe List(2)
      IntSlice.of(Array(2, 1)).reverseIterator(_                            % 2 != 0).toList shouldBe List(1)
      IntSlice.of(Array(1)).reverseIterator(_                               % 2 != 0).toList shouldBe List(1)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).reverseIterator(_       % 2 == 0).toList shouldBe List(8, 6, 4, 2)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).reverseIterator(_ % 2 == 0).toList shouldBe List(6, 4)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).reverseIterator(_ % 2 == 0).toList shouldBe List()
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).reverseIterator(_ % 2 == 0).toList shouldBe List()
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).reverseIterator(_ % 2 == 0).toList shouldBe List()
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).reverseIterator(_ % 2 == 0).toList shouldBe List(8, 6, 4, 2)
    }

    "map slice of values" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).map(_ * 10).toArray[Int] shouldBe Array(10, 20, 30, 40, 50, 60, 70,
        80, 90)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).map(_ * 10).toArray[Int] shouldBe Array(30, 40, 50, 60)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 3).map(_ * 10).toArray[Int] shouldBe Array(30)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 2).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      Slice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
    }

    "count values in slice matching predicate" in {
      IntSlice(0, 2, 0, 4, 5, 0, 7, 8, 0).count(_ == 0) shouldBe 4
      IntSlice(3, 4, 5).count(_ == 0) shouldBe 0
      Slice(0, 0, 0, 0, 0).count(_ == 0) shouldBe 5
      IntSlice(0).count(_ == 0) shouldBe 1
      IntSlice(1).count(_ == 0) shouldBe 0
      IntSlice().count(_ == 0) shouldBe 0
    }

    "top a value by an index" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).apply(3) shouldBe 4
      Slice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).apply(3) shouldBe 6
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).apply(3) shouldBe 9
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(3) shouldBe 6
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(4)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(-1)
      }
    }

    "update a value within a slice" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(3) shouldBe 12
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(4) shouldBe 5
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(3) shouldBe -13
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(2) shouldBe 5
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).update(3, 0).apply(3) shouldBe 0
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(3, -1).apply(3) shouldBe -1
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(4, 0)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(-1, 0)
      }
    }

    "have a length" in {
      IntSlice(1, 2, 3, 4, 5, 6, 7, 8, 9).length shouldBe 9
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).length shouldBe 0
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 1).length shouldBe 1
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 8).length shouldBe 6
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).length shouldBe 1
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).length shouldBe 0
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).length shouldBe 4
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).length shouldBe 1
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).length shouldBe 0
    }

    "have a slice" in {
      IntSlice().slice(-5, 10) shouldBe IntSlice()
      IntSlice(1, 2, 3).slice(-5, 10) shouldBe IntSlice(1, 2, 3)
      IntSlice(1, 2, 3, 4, 5, 6, 7, 8, 9).slice(-5, 5) shouldBe IntSlice(1, 2, 3, 4, 5)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).slice(-5, 5) shouldBe IntSlice(3, 4, 5, 6, 7)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).slice(5, 8) shouldBe IntSlice()
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).slice(1, 2) shouldBe IntSlice()
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).slice(1, 2) shouldBe IntSlice(5)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).slice(7, 12) shouldBe IntSlice(8, 9)
      IntSlice(1, 2, 3, 4, 5, 6, 7, 8, 9).slice(2, 7).slice(2, 4) shouldBe IntSlice(5, 6)
    }

    "have a drop" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(3).toList shouldBe List(4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(8).toList shouldBe List(9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(9).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3)).drop(3).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3)).drop(9).toList shouldBe Nil
      IntSlice.of(Array(1, 2)).drop(3).toList shouldBe Nil
      IntSlice.of(Array(1)).drop(3).toList shouldBe Nil
      IntSlice.of(Array.empty[Int]).drop(3).toList shouldBe Nil
    }

    "have a dropRight" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(3).toList shouldBe List(1, 2, 3, 4, 5, 6)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(8).toList shouldBe List(1)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(9).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3)).dropRight(3).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3)).dropRight(9).toList shouldBe Nil
      IntSlice.of(Array(1, 2)).dropRight(3).toList shouldBe Nil
      IntSlice.of(Array(1)).dropRight(3).toList shouldBe Nil
      IntSlice.of(Array.empty[Int]).dropRight(3).toList shouldBe Nil
    }

    "have a take" in {
      IntSlice(1, 2, 3, 4, 5, 6, 7, 8, 9).take(0).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(3).toList shouldBe List(1, 2, 3)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(8).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3)).take(3).toList shouldBe List(1, 2, 3)
      IntSlice.of(Array(1, 2, 3)).take(9).toList shouldBe List(1, 2, 3)
      IntSlice.of(Array(1, 2)).take(3).toList shouldBe List(1, 2)
      IntSlice.of(Array(1)).take(3).toList shouldBe List(1)
      IntSlice.of(Array.empty[Int]).take(3).toList shouldBe Nil
      IntSlice(1, 2, 3, 4, 5, 6, 7, 8, 9).take(5).drop(2) shouldBe IntSlice(3, 4, 5)
    }

    "have a takeRight" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(0).toList shouldBe Nil
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(3).toList shouldBe List(7, 8, 9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(8).toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(1, 2, 3)).takeRight(3).toList shouldBe List(1, 2, 3)
      IntSlice.of(Array(1, 2, 3)).takeRight(9).toList shouldBe List(1, 2, 3)
      IntSlice.of(Array(1, 2)).takeRight(3).toList shouldBe List(1, 2)
      IntSlice.of(Array(1)).takeRight(3).toList shouldBe List(1)
      IntSlice.of(Array.empty[Int]).takeRight(3).toList shouldBe Nil
    }

    "have a head" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).head shouldBe 1
      IntSlice.of(Array(4)).head shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(IntSlice.empty.head)
    }

    "have a headOption" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).headOption shouldBe Some(1)
      IntSlice.of(Array(4)).headOption shouldBe Some(4)
      IntSlice.empty.headOption shouldBe None
    }

    "have a tail" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).tail.toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      IntSlice.of(Array(4)).tail.toList shouldBe Nil
      IntSlice.empty.tail.toList shouldBe Nil
    }

    "have a last" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).last shouldBe 9
      IntSlice.of(Array(4)).last shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(IntSlice.empty.head)
    }

    "have a lastOption" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).lastOption shouldBe Some(9)
      IntSlice.of(Array(4)).lastOption shouldBe Some(4)
      IntSlice.empty.lastOption shouldBe None
    }

    "have an init" in {
      IntSlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).init.toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      IntSlice.of(Array(4)).init.toList shouldBe Nil
      IntSlice.empty.init.toList shouldBe Nil
    }

    "have a copyToArray" in {
      IntSlice().copyToArray(0, new Array[Int](0)) shouldBe Array.empty[Int]
      IntSlice(1, 2, 3).copyToArray(0, new Array[Int](10)) shouldBe Array(1, 2, 3, 0, 0, 0, 0, 0, 0, 0)
      IntSlice(1, 2, 3).copyToArray(5, new Array[Int](10)) shouldBe Array(0, 0, 0, 0, 0, 1, 2, 3, 0, 0)
      IntSlice(1, 1, 1, 1, 1).copyToArray(5, new Array[Int](10)) shouldBe Array(0, 0, 0, 0, 0, 1, 1, 1, 1, 1)
      IntSlice(3, 2, 1, 2, 3).copyToArray(0, new Array[Int](5)) shouldBe Array(3, 2, 1, 2, 3)
    }

    "have a detach" in {
      val array = Array(1, 2, 3, 4, 5, 6)
      val slice = IntSlice.of(array)
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

    "have hashCode" in {
      ArraySlice(1, 2, 3).hashCode() shouldBe IntSlice(1, 2, 3).hashCode()
      ArraySlice(1, 2, 3).hashCode() shouldBe Slice(1, 2, 3).hashCode()
      IntSlice(1, 2, 3).hashCode() shouldBe Slice(1, 2, 3).hashCode()
      MappedArraySlice(1, 2, 3).hashCode() shouldBe Slice(1, 2, 3).hashCode()
    }

    "have equals" in {
      ArraySlice(1, 2, 3) shouldBe ArraySlice(1, 2, 3)
      IntSlice(1, 2, 3) shouldBe IntSlice(1, 2, 3)
      MappedArraySlice(1, 2, 3) shouldBe MappedArraySlice(1, 2, 3)
    }

  }

}
