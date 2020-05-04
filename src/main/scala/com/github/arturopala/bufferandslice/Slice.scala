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

/** Lazy, possibly immutable slice of a sequence of values.
  * @tparam T type of the items of the sequence.
  *
  * @note As the slice usually wraps over a mutable structure,
  *       like an array or a java buffer, and it DOES NOT
  *       make a copy, any changes to the underlying source will
  *       directly affect the slice output. Keep it in mind.
  *
  * @groupprio Properties 0
  * @groupprio Access 1
  * @groupprio Transform 2
  * @groupprio Aggregate 3
  * @groupprio Read 4
  * @groupprio Internal 5
  */
trait Slice[T] extends (Int => T) {

  /** Returns value at the given index.
    * @group Access */
  def apply(index: Int): T

  /** Update a value at the given index.
    * Creates a copy of an underlying data.
    * @group Access */
  def update[T1 >: T: ClassTag](index: Int, value: T1): Slice[T1]

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

  /** Returns iterator over Slice values fulfilling the predicate. */
  def iterator(pred: T => Boolean): Iterator[T]

  /** Returns iterator over Slice values in the reverse order.
    * @group Read */
  def reverseIterator: Iterator[T]

  /** Returns iterator over Slice values fulfilling the predicate, in the reverse order.
    * @group Read */
  def reverseIterator(pred: T => Boolean): Iterator[T]

  /** Returns new list of Slice values.
    * @group Read */
  def toList: List[T]

  /** Returns new sequence of Slice values.
    * @group Read */
  def toSeq: Seq[T]

  /** Returns new iterable of Slice values.
    * @group Read */
  def asIterable: Iterable[T]

  /** Returns an array of a trimmed copy of an underlying data.
    * @group Read */
  def toArray[T1 >: T: ClassTag]: Array[T1]

  /** Detaches a slice creating a trimmed copy of an underlying data.
    * @group Access */
  def detach(implicit tag: ClassTag[T]): Slice[T]

  /** Dumps content to the array, starting from an index.
    * @group Read */
  def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1]

  /** Returns a buffer with a copy of this Slice.
    * @group Read */
  def toBuffer(implicit tag: ClassTag[T]): Buffer[T]

}

/** Slice companion object. Host factory methods. */
object Slice {

  /** Creates new Slice of given values. */
  def apply[T: ClassTag](is: T*): Slice[T] = ArraySlice.of(Array(is: _*))

  /** Creates new Slice of given array values.
    *
    * @note This DOES NOT make a copy, any changes to the underlying array will
    * directly affect the slice output. For full immutability does not share
    * array reference with other components.
    */
  def of[T: ClassTag](array: Array[T]): Slice[T] =
    if (array.isInstanceOf[Array[Int]])
      IntSlice.of(array.asInstanceOf[Array[Int]]).asInstanceOf[Slice[T]]
    else if (array.isInstanceOf[Array[Byte]])
      ByteSlice.of(array.asInstanceOf[Array[Byte]]).asInstanceOf[Slice[T]]
    else ArraySlice.of(array)

  /** Creates new Slice of given subset of array values. */
  def of[T: ClassTag](array: Array[T], from: Int, to: Int): Slice[T] =
    if (array.isInstanceOf[Array[Int]])
      IntSlice.of(array.asInstanceOf[Array[Int]], from, to).asInstanceOf[Slice[T]]
    else if (array.isInstanceOf[Array[Byte]])
      ByteSlice.of(array.asInstanceOf[Array[Byte]], from, to).asInstanceOf[Slice[T]]
    else ArraySlice.of(array, from, to)

  /** Creates an empty Slice of given type. */
  def empty[T: ClassTag]: Slice[T] = ArraySlice.empty[T]

}
