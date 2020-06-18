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

import scala.reflect.ClassTag

class LazyMapArraySliceSpec extends AnyWordSpecCompat {

  class A
  case class B() extends A

  def lazyMapArraySliceOf[T](array: Array[T], from: Int, to: Int): LazyMapArraySlice[T] =
    LazyMapArraySlice.lazilyMapped(from, to, array, identity[T], detached = true)

  def lazyMapArraySliceOf[T](array: Array[T]): LazyMapArraySlice[T] =
    LazyMapArraySlice.lazilyMapped(0, array.length, array, identity[T], detached = true)

  def lazyMapArraySliceOf[T: ClassTag](items: T*): LazyMapArraySlice[T] =
    LazyMapArraySlice.lazilyMapped(0, items.length, items.toArray[T], identity[T], detached = true)

  def emptyLazyMapArraySlice[T: ClassTag]: LazyMapArraySlice[T] =
    LazyMapArraySlice.lazilyMapped(0, 0, Array.empty[T], identity[T], detached = true)

  "LazyMapArraySlice" should {
    "wrap a whole array" in {
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e")).toArray[String] shouldBe Array("a", "b", "c", "d", "e")
      lazyMapArraySliceOf(Array.empty[String]).toArray[String] shouldBe Array.empty[String]
      lazyMapArraySliceOf(1, 2, 3).hashCode() shouldBe Slice(1, 2, 3).hashCode()
      lazyMapArraySliceOf(1, 2, 3) shouldBe lazyMapArraySliceOf(1, 2, 3)
    }

    "wrap a slice of an array" in {
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 2, 5).toArray[String] shouldBe Array("c", "d", "e")
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 4, 4).toArray[String] shouldBe Array.empty[String]
    }

    "have isEmpty" in {
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e")).isEmpty shouldBe false
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 2, 4).isEmpty shouldBe false
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 2, 2).isEmpty shouldBe true
      lazyMapArraySliceOf(Array.empty[String], 0, 0).isEmpty shouldBe true
    }

    "iterate over slice of values" in {
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e")).iterator.toList shouldBe List("a", "b", "c", "d", "e")
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 1, 5).iterator.toList shouldBe List("b", "c", "d", "e")
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 1, 4).iterator.toList shouldBe List("b", "c", "d")
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 2, 3).iterator.toList shouldBe List("c")
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
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 1, 4).reverseIterator.toList shouldBe List("b", "c", "d").reverse
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 2, 3).reverseIterator.toList shouldBe List("c").reverse
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 0, 0).reverseIterator.toList shouldBe List()
      lazyMapArraySliceOf(Array("a", "b", "c", "d", "e"), 5, 5).reverseIterator.toList shouldBe List()
    }

    "reverse-iterate with filter over slice of values" in {
      lazyMapArraySliceOf(Array.empty[Int]).reverseIterator(_                 % 2 == 0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1)).reverseIterator(_                         % 2 == 0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2)).reverseIterator(_                      % 2 != 0).toList shouldBe List(1)
      lazyMapArraySliceOf(Array(1)).reverseIterator(_                         % 2 != 0).toList shouldBe List(1)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).reverseIterator(_ % 2 == 0).toList shouldBe List(
        8,
        6,
        4,
        2
      )
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).reverseIterator(_ % 2 == 0).toList shouldBe List(
        6,
        4
      )
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).reverseIterator(_ % 2 == 0).toList shouldBe List(4)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).reverseIterator(_ % 2 == 0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).reverseIterator(_ % 2 == 0).toList shouldBe List(
        8,
        6,
        4,
        2
      )
    }

    "map slice of values" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).map(_ * 10).toArray[Int] shouldBe Array(10, 20, 30, 40, 50,
        60, 70, 80, 90)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).map(_ * 10).toArray[Int] shouldBe Array(
        30,
        40,
        50,
        60
      )
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 3).map(_ * 10).toArray[Int] shouldBe Array(30)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 2).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).map(_ * 10).toArray[Int] shouldBe Array.empty[Int]
    }

    "count values in slice matching predicate" in {
      Slice(0, 2, 0, 4, 5, 0, 7, 8, 0).count(_ == 0) shouldBe 4
      Slice(3, 4, 5).count(_ == 0) shouldBe 0
      Slice(0, 0, 0, 0, 0).count(_ == 0) shouldBe 5
      Slice(0).count(_ == 0) shouldBe 1
      Slice(1).count(_ == 0) shouldBe 0
      Slice.empty[Int].count(_ == 0) shouldBe 0
    }

    "top a value by an index" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).apply(3) shouldBe 4
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).apply(3) shouldBe 6
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).apply(3) shouldBe 9
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(3) shouldBe 6
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(4)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).apply(-1)
      }
    }

    "update a value within a slice" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(3) shouldBe 12
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).update(3, 12).apply(4) shouldBe 5
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(3) shouldBe -13
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 9).update(3, -13).apply(2) shouldBe 5
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).update(3, 0).apply(3) shouldBe 0
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(3, -1).apply(3) shouldBe -1
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(4, 0)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 6).update(-1, 0)
      }

      val b = new B
      val b1 = new B
      val a = new A
      lazyMapArraySliceOf(b, b, b).update(2, b1).toArray shouldBe Array(b, b, b1)
      lazyMapArraySliceOf(b, b, b).update(2, a).toArray shouldBe Array(b, b, a)
    }

    "have a length" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).length shouldBe 9
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 0).length shouldBe 0
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 1).length shouldBe 1
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 8).length shouldBe 6
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 4, 5).length shouldBe 1
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).length shouldBe 0
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 9).length shouldBe 4
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 8, 9).length shouldBe 1
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9, 9).length shouldBe 0
    }

    "have a slice" in {
      Slice.empty[Int].slice(-5, 10) shouldBe Slice.empty[Int]
      Slice(1, 2, 3).slice(-5, 10) shouldBe Slice(1, 2, 3)
      Slice(1, 2, 3, 4, 5, 6, 7, 8, 9).slice(-5, 5) shouldBe Slice(1, 2, 3, 4, 5)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 7).slice(-5, 5) shouldBe
        lazyMapArraySliceOf(3, 4, 5, 6, 7)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 2, 5).slice(5, 8) shouldBe lazyMapArraySliceOf[Int]()
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 5, 5).slice(1, 2) shouldBe lazyMapArraySliceOf[Int]()
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 5).slice(1, 2) shouldBe lazyMapArraySliceOf(5)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 0, 9).slice(7, 12) shouldBe lazyMapArraySliceOf(8, 9)
    }

    "have a drop" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(3).toList shouldBe List(4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(8).toList shouldBe List(9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).drop(9).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3)).drop(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3)).drop(9).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2)).drop(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1)).drop(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array.empty[Int]).drop(3).toList shouldBe Nil
    }

    "have a dropRight" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(0).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(3).toList shouldBe List(1, 2, 3, 4, 5, 6)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(8).toList shouldBe List(1)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).dropRight(9).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3)).dropRight(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3)).dropRight(9).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2)).dropRight(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1)).dropRight(3).toList shouldBe Nil
      lazyMapArraySliceOf(Array.empty[Int]).dropRight(3).toList shouldBe Nil
    }

    "have a take" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(3).toList shouldBe List(1, 2, 3)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(8).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).take(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3)).take(3).toList shouldBe List(1, 2, 3)
      lazyMapArraySliceOf(Array(1, 2, 3)).take(9).toList shouldBe List(1, 2, 3)
      lazyMapArraySliceOf(Array(1, 2)).take(3).toList shouldBe List(1, 2)
      lazyMapArraySliceOf(Array(1)).take(3).toList shouldBe List(1)
      lazyMapArraySliceOf(Array.empty[Int]).take(3).toList shouldBe Nil
    }

    "have a takeRight" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(0).toList shouldBe Nil
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(3).toList shouldBe List(7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(8).toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).takeRight(9).toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(1, 2, 3)).takeRight(3).toList shouldBe List(1, 2, 3)
      lazyMapArraySliceOf(Array(1, 2, 3)).takeRight(9).toList shouldBe List(1, 2, 3)
      lazyMapArraySliceOf(Array(1, 2)).takeRight(3).toList shouldBe List(1, 2)
      lazyMapArraySliceOf(Array(1)).takeRight(3).toList shouldBe List(1)
      lazyMapArraySliceOf(Array.empty[Int]).takeRight(3).toList shouldBe Nil
    }

    "have a head" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).head shouldBe 1
      lazyMapArraySliceOf(Array(4)).head shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(lazyMapArraySliceOf(Array.empty[String]).head)
    }

    "have a headOption" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).headOption shouldBe Some(1)
      lazyMapArraySliceOf(Array(4)).headOption shouldBe Some(4)
      lazyMapArraySliceOf(Array.empty[String]).headOption shouldBe None
    }

    "have a tail" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).tail.toList shouldBe List(2, 3, 4, 5, 6, 7, 8, 9)
      lazyMapArraySliceOf(Array(4)).tail.toList shouldBe Nil
      lazyMapArraySliceOf(Array.empty[String]).tail.toList shouldBe Nil
    }

    "have a last" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).last shouldBe 9
      lazyMapArraySliceOf(Array(4)).last shouldBe 4
      an[NoSuchElementException] shouldBe thrownBy(lazyMapArraySliceOf(Array.empty[String]).head)
    }

    "have a lastOption" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).lastOption shouldBe Some(9)
      lazyMapArraySliceOf(Array(4)).lastOption shouldBe Some(4)
      lazyMapArraySliceOf(Array.empty[String]).lastOption shouldBe None
    }

    "have an init" in {
      lazyMapArraySliceOf(Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).init.toList shouldBe List(1, 2, 3, 4, 5, 6, 7, 8)
      lazyMapArraySliceOf(Array(4)).init.toList shouldBe Nil
      lazyMapArraySliceOf(Array.empty[String]).init.toList shouldBe Nil
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

    "have get" in {
      lazyMapArraySliceOf(1, 2, 3).get(-1) shouldBe None
      lazyMapArraySliceOf(1, 2, 3).get(0) shouldBe Some(1)
      lazyMapArraySliceOf(1, 2, 3).get(1) shouldBe Some(2)
      lazyMapArraySliceOf(1, 2, 3).get(2) shouldBe Some(3)
      lazyMapArraySliceOf(1, 2, 3).get(4) shouldBe None
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).get(-1) shouldBe None
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).get(0) shouldBe Some("1")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).get(1) shouldBe Some("2")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).get(2) shouldBe Some("3")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).get(4) shouldBe None
    }

    "have a find" in {
      emptyLazyMapArraySlice[Int].find(_ > 0) shouldBe None
      lazyMapArraySliceOf(0).find(_ > 0) shouldBe None
      lazyMapArraySliceOf(1).find(_ > 0) shouldBe Some(1)
      lazyMapArraySliceOf(1, 2, 3).find(_ > 0) shouldBe Some(1)
      lazyMapArraySliceOf(1, 2, 3).find(_ > 0) shouldBe Some(1)
      lazyMapArraySliceOf(1, 2, 3).find(_ > 1) shouldBe Some(2)
      lazyMapArraySliceOf(1, 2, 3).find(_ > 3) shouldBe None

      emptyLazyMapArraySlice[Int].map(String.valueOf).find("123".contains) shouldBe None
      lazyMapArraySliceOf(0).map(String.valueOf).find("123".contains) shouldBe None
      lazyMapArraySliceOf(1).map(String.valueOf).find("123".contains) shouldBe Some("1")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).find("123".contains) shouldBe Some("1")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).find("234".contains) shouldBe Some("2")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).find("345".contains) shouldBe Some("3")
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).find("456".contains) shouldBe None
    }

    "have an exists" in {
      emptyLazyMapArraySlice[Int].exists(_ > 0) shouldBe false
      lazyMapArraySliceOf(0).exists(_ > 0) shouldBe false
      lazyMapArraySliceOf(1).exists(_ > 0) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).exists(_ > 0) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).exists(_ > 0) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).exists(_ > 1) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).exists(_ > 3) shouldBe false

      emptyLazyMapArraySlice[Int].map(String.valueOf).exists("123".contains) shouldBe false
      lazyMapArraySliceOf(0).map(String.valueOf).exists("123".contains) shouldBe false
      lazyMapArraySliceOf(1).map(String.valueOf).exists("123".contains) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).exists("123".contains) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).exists("234".contains) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).exists("345".contains) shouldBe true
      lazyMapArraySliceOf(1, 2, 3).map(String.valueOf).exists("456".contains) shouldBe false
    }

    "have asArray" in {
      lazyMapArraySliceOf[Int](0, 1, 2, 3).asArray shouldBe Array(0, 1, 2, 3)
      lazyMapArraySliceOf[String]("a", "b", "c", "d").asArray shouldBe
        Array("a", "b", "c", "d")
      lazyMapArraySliceOf[Item](Item("a"), Item("b"), Item("c"), Item("d")).asArray shouldBe
        Array(Item("a"), Item("b"), Item("c"), Item("d"))
      lazyMapArraySliceOf[String]("a", "b", "c", "d").map(Item.apply).asArray shouldBe
        Array(Item("a"), Item("b"), Item("c"), Item("d"))
      lazyMapArraySliceOf[Byte](0.toByte, 1.toByte, 2.toByte, 3.toByte).asArray shouldBe
        Array(0.toByte, 1.toByte, 2.toByte, 3.toByte)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toByte).asArray shouldBe
        Array(0.toByte, 1.toByte, 2.toByte, 3.toByte)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toDouble).asArray shouldBe
        Array(0d, 1d, 2d, 3d)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toFloat).asArray shouldBe
        Array(0f, 1f, 2f, 3f)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toShort).asArray shouldBe
        Array(0.toShort, 1.toShort, 2.toShort, 3.toShort)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toChar).asArray shouldBe
        Array(0.toChar, 1.toChar, 2.toChar, 3.toChar)
      lazyMapArraySliceOf[Boolean](true, false, false, true).asArray shouldBe
        Array(true, false, false, true)
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_ % 2 == 0).asArray shouldBe
        Array(true, false, true, false)
      lazyMapArraySliceOf[Long](0L, 1L, 2L, 3L).map(_ % 2 == 0).asArray shouldBe
        Array(true, false, true, false)
    }

    "have asBuffer" in {
      lazyMapArraySliceOf[Int](0, 1, 2, 3).asBuffer.push(4).asArray shouldBe Array(0, 1, 2, 3, 4)
      lazyMapArraySliceOf[String]("a", "b", "c", "d").asBuffer.push("e").asArray shouldBe
        Array("a", "b", "c", "d", "e")
      lazyMapArraySliceOf[Int](0, 1, 2, 3).map(_.toDouble).asBuffer.push(4d).asArray shouldBe
        Array(0d, 1d, 2d, 3d, 4d)
    }
  }

  case class Item(s: String)

}
