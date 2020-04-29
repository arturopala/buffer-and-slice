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

/** Lazy, immutable slice of a sequence of values.
  * @tparam T type of the items of the sequence.
  *
  * @groupprio Properties 0
  * @groupprio Access 1
  * @groupprio Transform 2
  * @groupprio Aggregate 3
  * @groupprio Read 4
  */
trait Slice[T] extends (Int => T) {

  /** Returns value at the given index.
    * @group Access */
  def apply(index: Int): T

  /** Returns length of the Slice.
    * @group Properties */
  def length: Int

  /** Returns true if Slice has values, otherwise false.
    * @group Properties */
  def isEmpty: Boolean

  /** Returns true if Slice has values, otherwise false.
    * @group Properties */
  @`inline` final def nonEmpty: Boolean = !isEmpty

  /** Lazily composes mapping function and returns new Slice.
    * Does not modify nor copy underlying array.
    * @group Transform */
  def map[K](f: T => K): Slice[K]

  /** Counts values fulfilling the predicate.
    * @group Aggregate */
  def count(pred: T => Boolean): Int

  /** Returns first value in the Slice.
    * @group Access */
  def head: T

  /** Returns the last value in the Slice.
    * @group Access */
  def last: T

  /** Returns first value in the Slice.
    * @group Access */
  def headOption: Option[T]

  /** Returns the last value in the Slice.
    * @group Access */
  def lastOption: Option[T]

  /** Returns the Slice without first value.
    * @group Access */
  def tail: Slice[T]

  /** Returns the Slice without last value.
    * @group Access */
  def init: Slice[T]

  /** Lazily narrows Slice to provided range.
    * @group Transform */
  def slice(from: Int, to: Int): Slice[T]

  /** Lazily narrows Slice to first N items.
    * @group Transform */
  def take(n: Int): Slice[T]

  /** Lazily narrows Slice to last N items.
    * @group Transform */
  def takeRight(n: Int): Slice[T]

  /** Lazily narrows Slice to exclude first N items.
    * @group Transform */
  def drop(n: Int): Slice[T]

  /** Lazily narrows Slice to exclude last N items.
    * @group Transform */
  def dropRight(n: Int): Slice[T]

  /** Returns iterator over Slice values.
    * @group Read */
  def iterator: Iterator[T]

  /** Returns iterator over Slice values in the reverse order.
    * @group Read */
  def reverseIterator: Iterator[T]

  /** Returns iterator over Slice values, fulfilling the predicate, in the reverse order.
    * @group Read */
  def reverseIterator(pred: T => Boolean): Iterator[T]

  /** Returns new list of Slice values.
    * @group Read */
  def toList: List[T]

  /** Returns new iterable of Slice values.
    * @group Read */
  def asIterable: Iterable[T]

  /** Returns minimal copy of an underlying array, trimmed to the actual range.
    * @group Read */
  def toArray[T1 <: T: ClassTag](implicit tag: ClassTag[T]): Array[T1]

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  def toBuffer(implicit tag: ClassTag[T]): Buffer[T]

}

object Slice {

  /** Creates new Slice of given values. */
  def apply[T: ClassTag](is: T*): Slice[T] = Slice.of(Array(is: _*))

  private[bufferandslice] def of[T, K](fromIndex: Int, toIndex: Int, _array: Array[K], _mapF: K => T): ArraySlice[T] =
    new ArraySlice[T](fromIndex, toIndex) {
      type A = K
      val array: Array[A] = _array
      val mapF: A => T = _mapF
    }

  /** Creates new Slice of given array values. */
  def of[T](array: Array[T]): ArraySlice[T] = Slice.of[T, T](0, array.length, array, identity)

  /** Creates new Slice of given subset of array values. */
  def of[T](array: Array[T], from: Int, to: Int): ArraySlice[T] = {
    assert(from >= 0, "When creating a Slice, parameter `from` must be greater or equal to zero.")
    assert(to <= array.length, "When creating a Slice, parameter `to` must be lower or equal to the array length.")
    assert(from <= to, "When creating a Slice, parameter `from` must be lower or equal to the parameter `to`.")
    Slice.of[T, T](from, to, array, identity)
  }

  /** Creates an empty Slice of given type. */
  def empty[T: ClassTag]: Slice[T] = Slice.of(Array.empty[T])

}
