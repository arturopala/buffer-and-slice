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

trait ArraySliceLike[T] extends Slice[T] {

  /** @group Internal */
  protected def fromIndex: Int

  /** @group Internal */
  protected def toIndex: Int

  /** @group Internal */
  protected def array: Array[T]

  /** @group Internal */
  protected def create(fromIndex: Int, toIndex: Int, array: Array[T]): Slice[T]

  /** Sliced range length. */
  @`inline` final override val length: Int = toIndex - fromIndex

  /** Returns value at the given index withing the range. */
  final override def apply(index: Int): T = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `apply` index in the interval [0,$length), but was $index.")
    array.apply(fromIndex + index)
  }

  /** Creates a copy of the slice with modified value. */
  final override def update[T1 >: T: ClassTag](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    val modified = toArray[T1]
    modified.update(index, value)
    Slice.of(modified)
  }

  /** Lazily composes mapping function and returns new [[MappedArraySlice]].
    * Does not modify nor copy underlying array. */
  @`inline` final override def map[K](f: T => K): Slice[K] =
    MappedArraySlice.lazyMapped[K, T](fromIndex, toIndex, array, f)

  /** Counts values fulfilling the predicate. */
  final override def count(pred: T => Boolean): Int = {
    var a = 0
    var i = fromIndex
    while (i < toIndex) {
      if (pred(array(i))) a = a + 1
      i = i + 1
    }
    a
  }

  /** Returns true if Slice has values, otherwise false. */
  @`inline` final override def isEmpty: Boolean = length <= 0

  /** Returns first value in the Slice. */
  final override def head: T =
    if (length > 0) array(fromIndex)
    else throw new NoSuchElementException

  /** Returns the last value in the Slice. */
  final override def last: T =
    if (length > 0) array(toIndex - 1)
    else throw new NoSuchElementException

  /** Returns first value in the Slice. */
  final override def headOption: Option[T] =
    if (length > 0) Some(array(fromIndex))
    else None

  /** Returns the last value in the Slice. */
  final override def lastOption: Option[T] =
    if (length > 0) Some(array(toIndex - 1))
    else None

  /** Lazily narrows Slice to provided range. */
  final override def slice(from: Int, to: Int): Slice[T] = {
    val t = fit(to, length)
    val f = fit(from, t)
    if (f == 0 && t == length) this
    else create(fromIndex + f, fromIndex + t, array)
  }

  @`inline` private def fit(value: Int, upper: Int): Int =
    Math.min(Math.max(0, value), upper)

  /** Returns the Slice without first value. */
  @`inline` final override def tail: Slice[T] = drop(1)

  /** Returns the Slice without last value. */
  @`inline` final override def init: Slice[T] = dropRight(1)

  /** Lazily narrows Slice to first N items. */
  @`inline` final override def take(n: Int): Slice[T] = slice(0, n)

  /** Lazily narrows Slice to last N items. */
  @`inline` final override def takeRight(n: Int): Slice[T] = slice(length - n, length)

  /** Lazily narrows Slice to exclude first N items. */
  @`inline` final override def drop(n: Int): Slice[T] = slice(n, length)

  /** Lazily narrows Slice to exclude last N items. */
  @`inline` final override def dropRight(n: Int): Slice[T] = slice(0, length - n)

  /** Returns iterator over slice's values. */
  final def iterator: Iterator[T] = new Iterator[T] {

    var i: Int = fromIndex

    def hasNext: Boolean = i < toIndex

    def next(): T = {
      val value = array(i)
      i = i + 1
      value
    }
  }

  /** Returns iterator over slice's values fulfilling the predicate. */
  final override def iterator(pred: T => Boolean): Iterator[T] = new Iterator[T] {

    var i: Int = fromIndex

    seekNext

    def hasNext: Boolean = i < toIndex

    def next(): T = {
      val value = array(i)
      i = i + 1
      seekNext
      value
    }

    def seekNext: Unit =
      if (i < toIndex) {
        var v = array(i)
        while (!pred(v) && i < toIndex) {
          i = i + 1
          if (i < toIndex) v = array(i)
        }
      } else ()
  }

  /** Returns iterator over slice's values in the reverse order. */
  final override def reverseIterator: Iterator[T] = new Iterator[T] {

    var i: Int = toIndex - 1

    def hasNext: Boolean = i >= fromIndex

    def next(): T = {
      val value = array(i)
      i = i - 1
      value
    }
  }

  /** Returns iterator over slice's values, fulfilling the predicate, in the reverse order. */
  final override def reverseIterator(pred: T => Boolean): Iterator[T] = new Iterator[T] {

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
  final override def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
    newArray
  }

  /** Detaches a slice creating a trimmed copy of an underlying data. */
  final override def detach(implicit tag: ClassTag[T]): Slice[T] = Slice.of(toArray[T])

  /** Dumps content to the array, starting from an index.
    * @group Read*/
  @`inline` final override def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1] = {
    java.lang.System.arraycopy(array, fromIndex, targetArray, targetIndex, length)
    targetArray
  }

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  @`inline` final override def toBuffer(implicit tag: ClassTag[T]): Buffer[T] = new ArrayBuffer(toArray)

  /** Returns new list of Slice values.
    * @group Read */
  @`inline` final override def toList: List[T] = iterator.toList

  /** Returns new sequence of Slice values.
    * @group Read */
  @`inline` final override def toSeq: Seq[T] = iterator.toIndexedSeq

  /** Returns new iterable of Slice values.
    * @group Read */
  final override def asIterable: Iterable[T] = new AbstractIterable[T] {
    override def iterator: Iterator[T] = ArraySliceLike.this.iterator
    override def toString(): String = ArraySliceLike.this.toString
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
  private def sameElements(iterator1: Iterator[T], iterator2: Iterator[T]): Boolean = {
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
