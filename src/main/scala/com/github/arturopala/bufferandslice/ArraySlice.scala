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

import java.util.NoSuchElementException

import scala.collection.AbstractIterable
import scala.reflect.ClassTag

/** Lazy, immutable slice of an underlying array.
  *
  * @tparam T type of the array's items */
final class ArraySlice[T] private (fromIndex: Int, toIndex: Int, array: Array[T]) extends Slice[T] {

  /** Sliced range length. */
  val length: Int = toIndex - fromIndex

  /** Returns value at the given index withing the range. */
  def apply(index: Int): T = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `apply` index in the interval [0,$length), but was $index.")
    array.apply(fromIndex + index)
  }

  /** Creates a copy of the slice with modified value. */
  def update[T1 >: T: ClassTag](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    val modified: Array[T1] = toArray[T1]
    modified.update(index, value)
    ArraySlice.of(modified)
  }

  /** Lazily composes mapping function and returns new [[MappedArraySlice]].
    * Does not modify nor copy underlying array. */
  def map[K](f: T => K): Slice[K] = MappedArraySlice.lazyMapped[K, T](fromIndex, toIndex, array, f)

  /** Counts values fulfilling the predicate. */
  def count(pred: T => Boolean): Int = {
    var a = 0
    var i = fromIndex
    while (i < toIndex) {
      if (pred(array(i))) a = a + 1
      i = i + 1
    }
    a
  }

  /** Returns true if Slice has values, otherwise false. */
  def isEmpty: Boolean = length <= 0

  /** Returns first value in the Slice. */
  def head: T =
    if (length > 0) array(fromIndex)
    else throw new NoSuchElementException

  /** Returns the last value in the Slice. */
  def last: T =
    if (length > 0) array(toIndex - 1)
    else throw new NoSuchElementException

  /** Returns first value in the Slice. */
  def headOption: Option[T] =
    if (length > 0) Some(array(fromIndex))
    else None

  /** Returns the last value in the Slice. */
  def lastOption: Option[T] =
    if (length > 0) Some(array(toIndex - 1))
    else None

  /** Returns the Slice without first value. */
  def tail: Slice[T] = drop(1)

  /** Returns the Slice without last value. */
  def init: Slice[T] = dropRight(1)

  /** Lazily narrows Slice to provided range. */
  def slice(from: Int, to: Int): Slice[T] = {
    val t = fit(to, length)
    val f = fit(from, t)
    if (f == 0 && t == length) this
    else
      new ArraySlice(fromIndex + f, fromIndex + t, array)
  }

  private def fit(value: Int, upper: Int): Int =
    Math.min(Math.max(0, value), upper)

  /** Lazily narrows Slice to first N items. */
  def take(n: Int): Slice[T] =
    new ArraySlice(fromIndex, Math.min(fromIndex + Math.max(0, n), toIndex), array)

  /** Lazily narrows Slice to last N items. */
  def takeRight(n: Int): Slice[T] =
    new ArraySlice(Math.max(toIndex - Math.max(0, n), fromIndex), toIndex, array)

  /** Lazily narrows Slice to exclude first N items. */
  def drop(n: Int): Slice[T] =
    new ArraySlice(Math.min(fromIndex + Math.max(0, n), toIndex), toIndex, array)

  /** Lazily narrows Slice to exclude last N items. */
  def dropRight(n: Int): Slice[T] =
    new ArraySlice(fromIndex, Math.max(toIndex - Math.max(0, n), fromIndex), array)

  /** Returns iterator over Slice values. */
  def iterator: Iterator[T] = new Iterator[T] {

    var i: Int = fromIndex

    def hasNext: Boolean = i < toIndex

    def next(): T = {
      val value = array(i)
      i = i + 1
      value
    }
  }

  /** Returns iterator over Slice values in the reverse order. */
  def reverseIterator: Iterator[T] = new Iterator[T] {

    var i: Int = toIndex - 1

    def hasNext: Boolean = i >= fromIndex

    def next(): T = {
      val value = array(i)
      i = i - 1
      value
    }
  }

  /** Returns iterator over Slice values, fulfilling the predicate, in the reverse order. */
  def reverseIterator(pred: T => Boolean): Iterator[T] = new Iterator[T] {

    var i: Int = toIndex - 1

    seekNext

    def hasNext: Boolean = i >= fromIndex

    def next(): T = {
      val value = array(i)
      i = i - 1
      seekNext
      value
    }

    def seekNext: Unit =
      if (i >= fromIndex) {
        var v = array(i)
        while (!pred(v) && i >= fromIndex) {
          i = i - 1
          if (i >= fromIndex) v = array(i)
        }
      } else ()
  }

  /** Returns a minimal copy of an underlying array, trimmed to the actual range.
    * @group Read */
  def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
    newArray
  }

  /** Dumps content to the array, starting from an index.
    * @group Read*/
  @`inline` def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1] = {
    java.lang.System.arraycopy(array, fromIndex, targetArray, targetIndex, length)
    targetArray
  }

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  def toBuffer(implicit tag: ClassTag[T]): Buffer[T] =
    new ArrayBuffer(toArray)

  /** Returns new list of Slice values.
    * @group Read */
  def toList: List[T] = iterator.toList

  /** Returns new iterable of Slice values.
    * @group Read */
  def asIterable: Iterable[T] = new AbstractIterable[T] {
    override def iterator: Iterator[T] = ArraySlice.this.iterator
    override def toString(): String = ArraySlice.this.toString
  }

  override def toString: String =
    iterator.take(Math.min(20, length)).mkString("Slice(", ",", if (length > 20) ", ... )" else ")")

  override def equals(obj: Any): Boolean = obj match {
    case other: Slice[T] =>
      this.length == other.length &&
        sameElements(this.iterator, other.iterator)

    case _ => false
  }

  /** Checks if two iterators would return same elements. */
  def sameElements(iterator1: Iterator[T], iterator2: Iterator[T]): Boolean = {
    var result: Boolean = true
    while (result && iterator1.hasNext && iterator2.hasNext) {
      val t1 = iterator1.next()
      val t2 = iterator2.next()
      result = result && t1 == t2
    }
    result
  }

  override def hashCode(): Int = {
    var hash = 17
    hash = hash * 31 + this.length
    for (i <- fromIndex to toIndex by (length / 7)) {
      hash = hash * 31 + array(i).hashCode()
    }
    hash
  }
}

object ArraySlice {

  def apply[T: ClassTag](is: T*): ArraySlice[T] = ArraySlice.of(Array(is: _*))

  def of[T](array: Array[T]): ArraySlice[T] = new ArraySlice(0, array.length, array)

  def of[T](array: Array[T], from: Int, to: Int): ArraySlice[T] = {
    assert(from >= 0, "When creating an ArraySlice, parameter `from` must be greater or equal to 0.")
    assert(
      to <= array.length,
      "When creating an ArraySlice, parameter `to` must be lower or equal to the array length."
    )
    assert(from <= to, "When creating an ArraySlice, parameter `from` must be lower or equal to `to`.")
    new ArraySlice(from, to, array)
  }

  def empty[T: ClassTag]: ArraySlice[T] = ArraySlice()

}
