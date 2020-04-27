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

trait AnyWordSpecCompat extends munit.FunSuite {

  implicit class NameExt[T](name: String) {

    def in(body: => T): Unit = test(name)(body)

    def should(body: => T): Unit = {
      val pos0 = munitTestsBuffer.length
      body
      val pos1 = munitTestsBuffer.length
      for (i <- pos0 until pos1) {
        munitTestsBuffer(i) = {
          val test = munitTestsBuffer(i)
          test.withName(name + " should " + test.name)
        }
      }
    }
  }

  implicit class IterableExt[T](iterable: Iterable[T]) {

    def shouldBe(expected: Iterable[T])(implicit loc: munit.Location): Unit = {
      assert(iterable.size == expected.size, "both collections must have the same size")
      iterable.zip(expected).foreach { case (a, b) => assertEquals(a, b) }
    }
  }

  implicit class ValueExt[T](value: T) {

    def shouldBe(expected: T)(implicit loc: munit.Location): Unit =
      assertEquals(value, expected)

    def should(word: not.type): NotWord[T] = NotWord(value)
  }

  case object not
  case object thrownBy {
    def apply[T](body: => T): ThrownByWord[T] = ThrownByWord(() => body)
  }

  case class NotWord[T](value: T) {
    def be(expected: T)(implicit loc: munit.Location): Unit =
      assertNotEquals(value, expected)
  }

  def an[E <: Throwable: ClassTag]: AnWord[E] = new AnWord[E]

  class AnWord[E <: Throwable: ClassTag] {
    def shouldBe[T](thrownBy: ThrownByWord[T]): Unit =
      intercept[E](thrownBy.body())
  }

  case class ThrownByWord[T](body: () => T)

}
