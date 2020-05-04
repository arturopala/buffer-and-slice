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

class ArraySliceSpec extends AnyWordSpecCompat {

  "ArraySlice" should {
    "wrap a whole array" in {
      ArraySlice.of(Array("a", "b", "c", "d", "e")).toArray[String] shouldBe Array("a", "b", "c", "d", "e")
      ArraySlice.of(Array.empty[String]).toArray[String] shouldBe Array.empty[String]
    }

    "wrap a slice of an array" in {
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 5).toArray[String] shouldBe Array("c", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 4, 4).toArray[String] shouldBe Array.empty[String]
    }

    "have isEmpty" in {
      ArraySlice.of(Array("a", "b", "c", "d", "e")).isEmpty shouldBe false
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 4).isEmpty shouldBe false
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 2).isEmpty shouldBe true
      ArraySlice.of(Array.empty[String], 0, 0).isEmpty shouldBe true
    }

    "iterate over slice of values" in {
      ArraySlice.of(Array("a", "b", "c", "d", "e")).iterator.toList shouldBe List("a", "b", "c", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 5).iterator.toList shouldBe List("b", "c", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 4).iterator.toList shouldBe List("b", "c", "d")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 3).iterator.toList shouldBe List("c")
    }

    "iterate with filter over slice of values" in {
      val f = Set("b", "d", "e").contains _
      ArraySlice.of(Array("a", "b", "c", "d", "e")).iterator(f).toList shouldBe List("b", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 5).iterator(f).toList shouldBe List("b", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 4).iterator(f).toList shouldBe List("b", "d")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 3).iterator(f).toList shouldBe List()
    }

    "reverse-iterate over slice of values" in {
      Slice
        .of(Array("a", "b", "c", "d", "e"))
        .reverseIterator
        .toList shouldBe List("a", "b", "c", "d", "e").reverse
      Slice
        .of(Array("a", "b", "c", "d", "e"), 1, 5)
        .reverseIterator
        .toList shouldBe List("b", "c", "d", "e").reverse
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 4).reverseIterator.toList shouldBe List("b", "c", "d").reverse
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 3).reverseIterator.toList shouldBe List("c").reverse
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 0, 0).reverseIterator.toList shouldBe List()
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 5, 5).reverseIterator.toList shouldBe List()
    }

    "reverse-iterate with filter over slice of values" in {
      ArraySlice.of(Array.empty[Int]).reverseIterator(_                       % 2 == 0).toList shouldBe Nil
      ArraySlice.of(Array(1)).reverseIterator(_                               % 2 == 0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2)).reverseIterator(_                            % 2 != 0).toList shouldBe List(1)
      ArraySlice.of(Array(1)).reverseIterator(_                               % 2 != 0).toList shouldBe List(1)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).reverseIterator(_       % 2 == 0).toList shouldBe List(8, 6, 4, 2)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).reverseIterator(_ % 2 == 0).toList shouldBe List(6, 4)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).reverseIterator(_ % 2 == 0).toList shouldBe List(8, 6, 4, 2)
    }

    "map slice of values" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).map(_ * 10).toArray[Int] shouldBe Array(10, 20, 30, 40, 50, 60,
        70, 80, 90)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).map(_ * 10).toArray[Int] shouldBe Array(30, 40, 50, 60)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 3).map(_ * 10).toArray[Int] shouldBe Array(30)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 2).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
    }

    "count values in slice matching predicate" in {
      Slice(0, 2, 0, 4, 5, 0, 7, 8, 0).count(_ == 0) shouldBe 4
      Slice(3, 4, 5).count(_ == 0) shouldBe 0
      Slice(0, 0, 0, 0, 0).count(_ == 0) shouldBe 5
      Slice(0).count(_ == 0) shouldBe 1
      Slice(1).count(_ == 0) shouldBe 0
      Slice[Int]().count(_ == 0) shouldBe 0
    }

    "top a value by an index" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).apply(3) shouldBe 4
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).apply(3) shouldBe 6
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).apply(3) shouldBe 9
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(3) shouldBe 6
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(4)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(-1)
      }
    }

    "update a value within a slice" in {
      ArraySlice.of(Array("a", "b", "c")).update(1, "d").apply(1) shouldBe "d"
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(3) shouldBe 12
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(4) shouldBe 5
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(3) shouldBe -13
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(2) shouldBe 5
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).update(3, 0).apply(3) shouldBe 0
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(3, -1).apply(3) shouldBe -1
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(4, 0)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(-1, 0)
      }
    }

    "have a length" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).length shouldBe 9
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).length shouldBe 0
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 1).length shouldBe 1
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 8).length shouldBe 6
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).length shouldBe 1
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).length shouldBe 0
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).length shouldBe 4
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).length shouldBe 1
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).length shouldBe 0
    }

    "have a slice" in {
      Slice[Int]().slice(-5, 10) shouldBe Slice[Int]()
      Slice(1, 2, 3).slice(-5, 10) shouldBe Slice(1, 2, 3)
      Slice(1, 2, 3, 4, 5, 6, 7, 8, 9).slice(-5, 5) shouldBe Slice(1, 2, 3, 4, 5)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).slice(-5, 5) shouldBe ArraySlice(3, 4, 5, 6, 7)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).slice(5, 8) shouldBe ArraySlice[Int]()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).slice(1, 2) shouldBe ArraySlice[Int]()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).slice(1, 2) shouldBe ArraySlice(5)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).slice(7, 12) shouldBe ArraySlice(8, 9)
    }

    "have a drop" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(3).toList shouldBe List(4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(8).toList shouldBe List(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(9).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3)).drop(3).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3)).drop(9).toList shouldBe Nil
      ArraySlice.of(Array(1, 2)).drop(3).toList shouldBe Nil
      ArraySlice.of(Array(1)).drop(3).toList shouldBe Nil
      ArraySlice.of(Array.empty[Int]).drop(3).toList shouldBe Nil
    }

    "have a dropRight" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(3).toList shouldBe List(1, 2, 3, 4, 5, 6)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(8).toList shouldBe List(1)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(9).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3)).dropRight(3).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3)).dropRight(9).toList shouldBe Nil
      ArraySlice.of(Array(1, 2)).dropRight(3).toList shouldBe Nil
      ArraySlice.of(Array(1)).dropRight(3).toList shouldBe Nil
      ArraySlice.of(Array.empty[Int]).dropRight(3).toList shouldBe Nil
    }

    "have a take" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(3).toList shouldBe List(1, 2, 3)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(8).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3)).take(3).toList shouldBe List(1, 2, 3)
      ArraySlice.of(Array(1, 2, 3)).take(9).toList shouldBe List(1, 2, 3)
      ArraySlice.of(Array(1, 2)).take(3).toList shouldBe List(1, 2)
      ArraySlice.of(Array(1)).take(3).toList shouldBe List(1)
      ArraySlice.of(Array.empty[Int]).take(3).toList shouldBe Nil
    }

    "have a takeRight" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(0).toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(3).toList shouldBe List(7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(8).toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(1, 2, 3)).takeRight(3).toList shouldBe List(1, 2, 3)
      ArraySlice.of(Array(1, 2, 3)).takeRight(9).toList shouldBe List(1, 2, 3)
      ArraySlice.of(Array(1, 2)).takeRight(3).toList shouldBe List(1, 2)
      ArraySlice.of(Array(1)).takeRight(3).toList shouldBe List(1)
      ArraySlice.of(Array.empty[Int]).takeRight(3).toList shouldBe Nil
    }

    "have a head" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).head shouldBe 1
      ArraySlice.of(Array(4)).head shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ArraySlice.of(Array.empty[String]).head)
    }

    "have a headOption" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).headOption shouldBe Some(1)
      ArraySlice.of(Array(4)).headOption shouldBe Some(4)
      ArraySlice.of(Array.empty[String]).headOption shouldBe None
    }

    "have a tail" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).tail.toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(4)).tail.toList shouldBe Nil
      ArraySlice.of(Array.empty[String]).tail.toList shouldBe Nil
    }

    "have a last" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).last shouldBe 9
      ArraySlice.of(Array(4)).last shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ArraySlice.of(Array.empty[String]).head)
    }

    "have a lastOption" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).lastOption shouldBe Some(9)
      ArraySlice.of(Array(4)).lastOption shouldBe Some(4)
      ArraySlice.of(Array.empty[String]).lastOption shouldBe None
    }

    "have an init" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).init.toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      ArraySlice.of(Array(4)).init.toList shouldBe Nil
      ArraySlice.of(Array.empty[String]).init.toList shouldBe Nil
    }

    "return an Array of a wider type" in {
      class A
      class B extends A
      val slice = Slice(new B, new B, new B)
      slice.toArray[B]
      slice.toArray[A]
    }

    "have a copyToArray" in {
      Slice.empty[String].copyToArray(0, new Array[String](0)) shouldBe Array.empty[String]
      Slice("a", "b", "c")
        .copyToArray(0, new Array[String](10)) shouldBe Array("a", "b", "c", null, null, null, null, null, null, null)
      Slice("a", "b", "c")
        .copyToArray(5, new Array[String](10)) shouldBe Array(null, null, null, null, null, "a", "b", "c", null, null)
    }

    "have a detach" in {
      val array = Array(1, 2, 3, 4, 5, 6)
      val slice = ArraySlice.of(array)
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
  }

}
