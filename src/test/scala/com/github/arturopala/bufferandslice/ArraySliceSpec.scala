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

  class A
  case class B() extends A

  "ArraySlice" should {

    "return an Array of a wider type" in {
      val slice = Slice(new B, new B, new B)
      slice.toArray[B]
      slice.toArray[A]
    }

    "have an empty" in {
      ArraySlice.empty[String].length shouldBe 0
      ArraySlice.empty[Double].length shouldBe 0
    }

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
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 3).isEmpty shouldBe false
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 1).isEmpty shouldBe true
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 4).isEmpty shouldBe false
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 2).isEmpty shouldBe true
      ArraySlice.of(Array.empty[String], 0, 0).isEmpty shouldBe true
    }

    "iterate over slice of values" in {
      ArraySlice.of(Array("a", "b", "c", "d", "e")).iterator.toList shouldBe List("a", "b", "c", "d", "e")
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 1).iterator.toList shouldBe List()
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

    "iterate with filter over slice of values" in {
      val f = Set("b", "d", "e").contains _
      ArraySlice.of(Array("a", "b", "c", "d", "e")).indexIterator(f).toList shouldBe List(1, 3, 4)
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 5).indexIterator(f).toList shouldBe List(0, 2, 3)
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 1, 4).indexIterator(f).toList shouldBe List(0, 2)
      ArraySlice.of(Array("a", "b", "c", "d", "e"), 2, 3).indexIterator(f).toList shouldBe List()
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
      Slice.empty[Int].count(_ == 0) shouldBe 0
      Slice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).count(_ => true) shouldBe 0
      Slice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).count(_ => true) shouldBe 1
      Slice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).count(_ => false) shouldBe 0
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

      val b = new B
      val b1 = new B
      val a = new A
      ArraySlice(b, b, b).update(2, b1).toArray shouldBe Array(b, b, b1)
      ArraySlice(b, b, b).update(2, a).toArray shouldBe Array(b, b, a)
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
      Slice.empty[Int].slice(-5, 10) shouldBe Slice.empty[Int]
      Slice(1, 2, 3).slice(-5, 10) shouldBe Slice(1, 2, 3)
      Slice(1, 2, 3, 4, 5, 6, 7, 8, 9).slice(-5, 5) shouldBe Slice(1, 2, 3, 4, 5)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).slice(-5, 5) shouldBe ArraySlice(3, 4, 5, 6, 7)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).slice(5, 8) shouldBe ArraySlice.empty[Int]
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).slice(1, 2) shouldBe ArraySlice.empty[Int]
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
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).drop(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 8).drop(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).drop(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 8).drop(2).toList shouldBe List(8)
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
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).dropRight(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 8).dropRight(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).dropRight(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 8).dropRight(2).toList shouldBe List(6)
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
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).take(2).toList shouldBe List(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 8).take(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).take(2).toList shouldBe List(6)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 8).take(2).toList shouldBe List(6, 7)
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
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).takeRight(2).toList shouldBe List(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 8).takeRight(2).toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).takeRight(2).toList shouldBe List(6)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 8).takeRight(2).toList shouldBe List(7, 8)
    }

    "have a head" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).head shouldBe 1
      ArraySlice.of(Array(4)).head shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ArraySlice.of(Array.empty[String]).head)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).head shouldBe 9
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 8).head shouldBe 8
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).head shouldBe 6
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 1).head shouldBe 1
    }

    "have a headOption" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).headOption shouldBe Some(1)
      ArraySlice.of(Array(4)).headOption shouldBe Some(4)
      ArraySlice.of(Array.empty[String]).headOption shouldBe None
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).headOption shouldBe Some(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 8).headOption shouldBe Some(8)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 6).headOption shouldBe Some(6)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 1).headOption shouldBe Some(1)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 1, 1).headOption shouldBe None
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 11, 13).headOption shouldBe None
    }

    "have a tail" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).tail.toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      ArraySlice.of(Array(4)).tail.toList shouldBe Nil
      ArraySlice.of(Array.empty[String]).tail.toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).tail.toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 9).tail.toList shouldBe List(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 13).tail.toList shouldBe List(7, 8, 9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 7).tail.toList shouldBe List(2, 3, 4, 5, 6, 7)
    }

    "have a last" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).last shouldBe 9
      ArraySlice.of(Array(4)).last shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(ArraySlice.of(Array.empty[String]).head)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).last shouldBe 9
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 8).last shouldBe 8
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 13).last shouldBe 9
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 7).last shouldBe 7
    }

    "have a lastOption" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).lastOption shouldBe Some(9)
      ArraySlice.of(Array(4)).lastOption shouldBe Some(4)
      ArraySlice.of(Array.empty[String]).lastOption shouldBe None
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).lastOption shouldBe Some(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 8).lastOption shouldBe Some(8)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 13).lastOption shouldBe Some(9)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 7).lastOption shouldBe Some(7)
    }

    "have an init" in {
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).init.toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      ArraySlice.of(Array(4)).init.toList shouldBe Nil
      ArraySlice.of(Array.empty[String]).init.toList shouldBe Nil
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).init.toList shouldBe List()
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 7, 9).init.toList shouldBe List(8)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 13).init.toList shouldBe List(6, 7, 8)
      ArraySlice.of(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 7).init.toList shouldBe List(1, 2, 3, 4, 5, 6)
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

    "have a get" in {
      ArraySlice(1, 2, 3).get(-1) shouldBe None
      ArraySlice(1, 2, 3).get(0) shouldBe Some(1)
      ArraySlice(1, 2, 3).get(1) shouldBe Some(2)
      ArraySlice(1, 2, 3).get(2) shouldBe Some(3)
      ArraySlice(1, 2, 3).get(4) shouldBe None
    }

    "have a find" in {
      ArraySlice.empty[Int].find(_ > 0) shouldBe None
      ArraySlice(0).find(_ > 0) shouldBe None
      ArraySlice(1).find(_ > 0) shouldBe Some(1)
      ArraySlice(1, 2, 3).find(_ > 0) shouldBe Some(1)
      ArraySlice(1, 2, 3).find(_ > 0) shouldBe Some(1)
      ArraySlice(1, 2, 3).find(_ > 1) shouldBe Some(2)
      ArraySlice(1, 2, 3).find(_ > 3) shouldBe None
    }

    "have an exists" in {
      ArraySlice.empty[Int].exists(_ > 0) shouldBe false
      ArraySlice(0).exists(_ > 0) shouldBe false
      ArraySlice(1).exists(_ > 0) shouldBe true
      ArraySlice(1, 2, 3).exists(_ > 0) shouldBe true
      ArraySlice(1, 2, 3).exists(_ > 0) shouldBe true
      ArraySlice(1, 2, 3).exists(_ > 1) shouldBe true
      ArraySlice(1, 2, 3).exists(_ > 3) shouldBe false
    }

    "have toBuffer" in {
      ArraySlice.empty[Int].toBuffer.push(4).asArray shouldBe Array(4)
      ArraySlice(1, 2, 3).toBuffer.push(4).asArray shouldBe Array(1, 2, 3, 4)
      ArraySlice.empty[String].toBuffer.push("a").asArray shouldBe Array("a")
      ArraySlice("a", "b", "c").toBuffer.push("d").asArray shouldBe Array("a", "b", "c", "d")

      val b = new B
      ArraySlice(b, b, b).toBuffer.asArray shouldBe Array(b, b, b)
      ArraySlice(b, b, b).toBuffer[A].asArray shouldBe Array(b, b, b)
    }

    "have asBuffer" in {
      ArraySlice.empty[Int].asBuffer.push(4).asArray shouldBe Array(4)
      ArraySlice(1, 2, 3).asBuffer.push(4).asArray shouldBe Array(1, 2, 3, 4)
      ArraySlice.empty[String].asBuffer.push("a").asArray shouldBe Array("a")
      ArraySlice("a", "b", "c").asBuffer.push("d").asArray shouldBe Array("a", "b", "c", "d")

      val b = new B
      ArraySlice(b, b, b).asBuffer.asArray shouldBe Array(b, b, b)
      ArraySlice(b, b, b).asBuffer.toArray[A] shouldBe Array(b, b, b)
    }

    "have foldLeft" in {
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldLeft(0)(_ + _) shouldBe 45
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldLeft("x")(_.toString + _) shouldBe "x0123456789"
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldLeft(5)(_ + _) shouldBe 50
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8).foldLeft(0)(_ + _) shouldBe 36
      ArraySlice("a", "b", "c", "d", "e", "f").foldLeft("-")(_ + _) shouldBe "-abcdef"
      ArraySlice.empty[String].foldLeft("-")(_ + _) shouldBe "-"
      ArraySlice.empty[Int].foldLeft(7)(_ + _) shouldBe 7
    }

    "have foldRight" in {
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldRight(0)(_ + _) shouldBe 45
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldRight("x")(_.toString + _) shouldBe "0123456789x"
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).foldRight(6)(_ + _) shouldBe 51
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8).foldRight(0)(_ + _) shouldBe 36
      ArraySlice("a", "b", "c", "d", "e", "f").foldRight("-")(_ + _) shouldBe "abcdef-"
      ArraySlice.empty[String].foldRight("-")(_ + _) shouldBe "-"
      ArraySlice.empty[Int].foldRight(7)(_ + _) shouldBe 7
    }

    "have fold" in {
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).fold(0)(_ + _) shouldBe 45
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).fold(5)(_ + _) shouldBe 50
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8).fold(0)(_ + _) shouldBe 36
      ArraySlice("a", "b", "c", "d", "e", "f").fold("-")(_ + _) shouldBe "-abcdef"
      ArraySlice.empty[String].fold("-")(_ + _) shouldBe "-"
      ArraySlice.empty[Int].fold(7)(_ + _) shouldBe 7
    }

    "have reduce" in {
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).reduce(_ + _) shouldBe 45
      ArraySlice(0, 1, 2, 3, 4, 5, 6, 7, 8).reduce(_ + _) shouldBe 36
      ArraySlice(2, 3, 4, 5, 6, 7, 8).reduce(_ + _) shouldBe 35
      ArraySlice("a", "b", "c", "d", "e", "f").reduce(_ + _) shouldBe "abcdef"
      an[UnsupportedOperationException] shouldBe thrownBy {
        ArraySlice.empty[String].reduce(_ + _)
      }
      an[UnsupportedOperationException] shouldBe thrownBy {
        ArraySlice.empty[Int].reduce(_ + _)
      }
    }
  }

}
