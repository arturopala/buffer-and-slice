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

/** Lazily mapped slice of an underlying array.
  * @note Truly immutable only if an underlying array kept private, or if detached.
  * @tparam T type of the array's items
  */
abstract class LazyMapArraySlice[T] private (fromIndex: Int, toIndex: Int, detached: Boolean) extends Slice[T] {

  /** Type of the underlying array items. */
  type A

  /** Underlying array. */
  val array: Array[A]

  /** Value mapping function. */
  val mapF: A => T

  /** Sliced range length. */
  final override val length: Int = toIndex - fromIndex

  /** Returns value at the given index withing the range. */
  final override def apply(index: Int): T = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `apply` index in the interval [0,$length), but was $index.")
    mapF(array.apply(fromIndex + index))
  }

  /** Creates a copy of the slice with modified value. */
  final override def update[T1 >: T: ClassTag](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    val modified: Array[T1] = toArray[T1]
    modified.update(index, value)
    LazyMapArraySlice.lazyMapped[T1, T1](0, length, modified, identity, true)
  }

  /** Lazily composes mapping function and returns new Slice.
    * Does not modify nor copy underlying array. */
  final override def map[K](f: T => K): Slice[K] =
    LazyMapArraySlice.lazyMapped[K, A](fromIndex, toIndex, array, mapF.andThen(f), detached)

  /** Counts values fulfilling the predicate. */
  final override def count(pred: T => Boolean): Int = {
    var a = 0
    var i = fromIndex
    while (i < toIndex) {
      if (pred(mapF(array(i)))) a = a + 1
      i = i + 1
    }
    a
  }

  /** Returns true if Slice has values, otherwise false. */
  final override def isEmpty: Boolean = length <= 0

  /** Returns first value in the Slice. */
  final override def head: T =
    if (length > 0) mapF(array(fromIndex))
    else throw new NoSuchElementException

  /** Returns the last value in the Slice. */
  final override def last: T =
    if (length > 0) mapF(array(toIndex - 1))
    else throw new NoSuchElementException

  /** Returns first value in the Slice. */
  final override def headOption: Option[T] =
    if (length > 0) Some(mapF(array(fromIndex)))
    else None

  /** Returns the last value in the Slice. */
  final override def lastOption: Option[T] =
    if (length > 0) Some(mapF(array(toIndex - 1)))
    else None

  /** Returns Some of the value at the index,
    * or None if index outside of range. */
  final override def get(index: Int): Option[T] =
    if (index < 0 || index >= length) None
    else Some(mapF(array.apply(index)))

  /** Lazily narrows Slice to provided range. */
  final override def slice(from: Int, to: Int): this.type = {
    val t = fit(to, length)
    val f = fit(from, t)
    if (f == 0 && t == length) this
    else
      LazyMapArraySlice
        .lazyMapped[T, A](fromIndex + f, fromIndex + t, array, mapF, detached)
        .asInstanceOf[this.type]
  }

  private def fit(value: Int, upper: Int): Int =
    Math.min(Math.max(0, value), upper)

  /** Returns the Slice without first value. */
  @`inline` final override def tail: this.type = drop(1)

  /** Returns the Slice without last value. */
  @`inline` final override def init: this.type = dropRight(1)

  /** Lazily narrows Slice to first N items. */
  @`inline` final override def take(n: Int): this.type = slice(0, n)

  /** Lazily narrows Slice to last N items. */
  @`inline` final override def takeRight(n: Int): this.type = slice(length - n, length)

  /** Lazily narrows Slice to exclude first N items. */
  @`inline` final override def drop(n: Int): this.type = slice(n, length)

  /** Lazily narrows Slice to exclude last N items. */
  @`inline` final override def dropRight(n: Int): this.type = slice(0, length - n)

  /** Returns iterator over Slice values. */
  final override def iterator: Iterator[T] = new Iterator[T] {

    var i: Int = fromIndex

    def hasNext: Boolean = i < toIndex

    def next(): T = {
      val value = mapF(array(i))
      i = i + 1
      value
    }
  }

  /** Returns iterator over Slice values fulfilling the predicate. */
  final override def iterator(pred: T => Boolean): Iterator[T] = new Iterator[T] {

    var i: Int = fromIndex

    seekNext

    def hasNext: Boolean = i < toIndex

    def next(): T = {
      val value = mapF(array(i))
      i = i + 1
      seekNext
      value
    }

    def seekNext: Unit =
      if (i < toIndex) {
        var v = mapF(array(i))
        while (!pred(v) && i < toIndex) {
          i = i + 1
          if (i < toIndex) v = mapF(array(i))
        }
      } else ()
  }

  /** Returns iterator over Slice values in the reverse order. */
  final override def reverseIterator: Iterator[T] = new Iterator[T] {

    var i: Int = toIndex - 1

    def hasNext: Boolean = i >= fromIndex

    def next(): T = {
      val value = mapF(array(i))
      i = i - 1
      value
    }
  }

  /** Returns iterator over Slice values fulfilling the predicate, in the reverse order. */
  final override def reverseIterator(pred: T => Boolean): Iterator[T] = new Iterator[T] {

    var i: Int = toIndex - 1

    seekNext

    def hasNext: Boolean = i >= fromIndex

    def next(): T = {
      val value = mapF(array(i))
      i = i - 1
      seekNext
      value
    }

    def seekNext: Unit =
      if (i >= fromIndex) {
        var v = mapF(array(i))
        while (!pred(v) && i >= fromIndex) {
          i = i - 1
          if (i >= fromIndex) v = mapF(array(i))
        }
      } else ()
  }

  /** Returns minimal copy of an underlying array, trimmed to the actual range. */
  final override def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    copyToArray(0, newArray)
    newArray
  }

  /** Detaches a slice creating a trimmed copy of an underlying data. */
  final override def detach: this.type =
    if (detached) this
    else {
      val newArray = ArrayOps.copyOf(array, length)
      java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
      LazyMapArraySlice
        .lazyMapped[T, A](0, length, newArray, mapF, detached = true)
        .asInstanceOf[this.type]
    }

  /** Dumps content to the array, starting from an index. */
  final override def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1] = {
    var i = 0
    while (i < length) {
      targetArray(targetIndex + i) = mapF(array(fromIndex + i))
      i = i + 1
    }
    targetArray
  }

  /** Returns buffer with a copy of this Slice. */
  final override def toBuffer(implicit tag: ClassTag[T]): Buffer[T] =
    new ArrayBuffer(toArray)

  /** Returns new list of Slice values. */
  @`inline` final override def toList: List[T] = iterator.toList

  /** Returns new sequence of Slice values. */
  @`inline` final override def toSeq: Seq[T] = iterator.toIndexedSeq

  /** Returns new iterable of Slice values. */
  final override def asIterable: Iterable[T] = new AbstractIterable[T] {
    override def iterator: Iterator[T] = LazyMapArraySlice.this.iterator
    override def toString(): String = LazyMapArraySlice.this.toString
  }

  final override def toString: String =
    iterator.take(Math.min(20, length)).mkString("Slice(", ",", if (length > 20) ", ... )" else ")")

  final override def equals(obj: Any): Boolean = obj match {
    case other: Slice[T] =>
      this.length == other.length &&
        sameElements(this.iterator, other.iterator)

    case _ => false
  }

  /** Checks if two iterators would return same elements. */
  final def sameElements(iterator1: Iterator[T], iterator2: Iterator[T]): Boolean = {
    var result: Boolean = true
    while (result && iterator1.hasNext && iterator2.hasNext) {
      val t1 = iterator1.next()
      val t2 = iterator2.next()
      result = result && t1 == t2
    }
    result
  }

  final override def hashCode(): Int = {
    var hash = 17
    hash = hash * 31 + this.length
    for (i <- fromIndex until toIndex by Math.max(1, length / 7)) {
      hash = hash * 31 + array(i).hashCode()
    }
    hash
  }
}

object LazyMapArraySlice {

  /** Creates new detached LazyMapArraySlice out of given value sequence. */
  def apply[T: ClassTag](is: T*): LazyMapArraySlice[T] = {
    val _array = Array(is: _*)
    new LazyMapArraySlice[T](0, _array.length, detached = true) {
      type A = T
      val array: Array[A] = _array
      val mapF: A => T = identity
    }
  }

  private[bufferandslice] def lazyMapped[T, K](
    fromIndex: Int,
    toIndex: Int,
    _array: Array[K],
    _mapF: K => T,
    detached: Boolean
  ): LazyMapArraySlice[T] =
    new LazyMapArraySlice[T](fromIndex, toIndex, detached) {
      type A = K
      val array: Array[A] = _array
      val mapF: A => T = _mapF
    }

  /** Creates new LazyMapArraySlice of given array values. */
  def of[T](_array: Array[T]): LazyMapArraySlice[T] = new LazyMapArraySlice[T](0, _array.length, detached = false) {
    type A = T
    val array: Array[A] = _array
    val mapF: A => T = identity
  }

  /** Creates new LazyMapArraySlice of given subset of array values. */
  def of[T](_array: Array[T], from: Int, to: Int): LazyMapArraySlice[T] = {
    assert(from >= 0, "When creating a LazyMapArraySlice, parameter `from` must be greater or equal to zero.")
    assert(
      to <= _array.length,
      "When creating a LazyMapArraySlice, parameter `to` must be lower or equal to the array length."
    )
    assert(
      from <= to,
      "When creating a LazyMapArraySlice, parameter `from` must be lower or equal to the parameter `to`."
    )
    new LazyMapArraySlice[T](from, to, detached = false) {
      type A = T
      val array: Array[A] = _array
      val mapF: A => T = identity
    }
  }

  /** Creates an empty LazyMapArraySlice of given type. */
  def empty[T: ClassTag]: LazyMapArraySlice[T] = LazyMapArraySlice.of(Array.empty[T])

}
