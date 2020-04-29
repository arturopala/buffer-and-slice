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

/** Lazy, specialized slice of the array of integers. */
final class IntSlice private (
  private[bufferandslice] val fromIndex: Int,
  private[bufferandslice] val toIndex: Int,
  private[bufferandslice] val array: Array[Int]
) extends Slice[Int] {

  /** Sliced range length. */
  val length: Int = toIndex - fromIndex

  /** Returns value at the given index withing the range. */
  def apply(index: Int): Int = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `apply` index in the interval [0,$length), but was $index.")
    array.apply(fromIndex + index)
  }

  /** Creates a copy of the slice with modified value. */
  def update(index: Int, value: Int): IntSlice = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    val modified = toArray[Int]
    modified.update(index, value)
    new IntSlice(0, length, modified)
  }

  /** Lazily composes mapping function and returns new Slice.
    * Does not modify nor copy underlying array. */
  def map[B](f: Int => B): Slice[B] = Slice.of[B, Int](fromIndex, toIndex, array, f)

  /** Counts values fulfilling the predicate. */
  def count(pred: Int => Boolean): Int = {
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
  def head: Int =
    if (length > 0) array(fromIndex)
    else throw new NoSuchElementException

  /** Returns the last value in the Slice. */
  def last: Int =
    if (length > 0) array(toIndex - 1)
    else throw new NoSuchElementException

  /** Returns first value in the Slice. */
  def headOption: Option[Int] =
    if (length > 0) Some(array(fromIndex))
    else None

  /** Returns the last value in the Slice. */
  def lastOption: Option[Int] =
    if (length > 0) Some(array(toIndex - 1))
    else None

  /** Returns the Slice without first value. */
  def tail: IntSlice = drop(1)

  /** Returns the Slice without last value. */
  def init: IntSlice = dropRight(1)

  /** Lazily narrows Slice to provided range. */
  def slice(from: Int, to: Int): IntSlice = {
    val t = fit(0, to, length)
    val f = fit(0, from, t)
    if (f == 0 && t == length) this
    else new IntSlice(fromIndex + f, fromIndex + t, array)
  }

  private def fit(lower: Int, value: Int, upper: Int): Int =
    Math.min(Math.max(lower, value), upper)

  /** Lazily narrows Slice to first N items. */
  def take(n: Int): IntSlice =
    new IntSlice(fromIndex, Math.min(fromIndex + Math.max(0, n), toIndex), array)

  /** Lazily narrows Slice to last N items. */
  def takeRight(n: Int): IntSlice =
    new IntSlice(Math.max(toIndex - Math.max(0, n), fromIndex), toIndex, array)

  /** Lazily narrows Slice to exclude first N items. */
  def drop(n: Int): IntSlice =
    new IntSlice(Math.min(fromIndex + Math.max(0, n), toIndex), toIndex, array)

  /** Lazily narrows Slice to exclude last N items. */
  def dropRight(n: Int): IntSlice =
    new IntSlice(fromIndex, Math.max(toIndex - Math.max(0, n), fromIndex), array)

  /** Returns iterator over Slice values. */
  def iterator: Iterator[Int] = new Iterator[Int] {

    var i: Int = fromIndex

    override def hasNext: Boolean = i < toIndex

    override def next(): Int = {
      val value = array(i)
      i = i + 1
      value
    }
  }

  /** Returns iterator over Slice values in the reverse order. */
  def reverseIterator: Iterator[Int] = new Iterator[Int] {

    var i: Int = toIndex - 1

    override def hasNext: Boolean = i >= fromIndex

    override def next(): Int = {
      val value = array(i)
      i = i - 1
      value
    }
  }

  /** Returns iterator over Slice values, fulfilling the predicate, in the reverse order. */
  def reverseIterator(pred: Int => Boolean): Iterator[Int] = new Iterator[Int] {

    var i: Int = toIndex - 1

    seekNext

    override def hasNext: Boolean = i >= fromIndex

    override def next(): Int = {
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

  /** Returns minimal copy of an underlying array, trimmed to the actual range.
    * @group Read */
  def toArray[T1 <: Int: ClassTag](implicit tag: ClassTag[Int]): Array[T1] = {
    val newArray = new Array[T1](length)
    Array.copy(array, fromIndex, newArray, 0, length)
    newArray
  }

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  def toBuffer(implicit tag: ClassTag[Int]): Buffer[Int] = {
    val newArray = new Array[Int](length)
    Array.copy(array, fromIndex, newArray, 0, length)
    new IntBuffer(length).appendArray(newArray)
  }

  /** Returns new list of Slice values.
    * @group Read */
  def toList: List[Int] = iterator.toList

  /** Returns new iterable of Slice values.
    * @group Read */
  def asIterable: Iterable[Int] = new AbstractIterable[Int] {
    override def iterator: Iterator[Int] = IntSlice.this.iterator
    override def toString(): String = IntSlice.this.toString
  }

  /** Returns a copy of the underlying array as a buffer.
    * @group Read */
  def toBuffer: IntBuffer =
    new IntBuffer(length)
      .insertArray(0, fromIndex, length, array)

  override def toString: String =
    iterator.take(Math.min(20, length)).mkString("Slice(", ",", if (length > 20) ", ... )" else ")")

  override def equals(obj: Any): Boolean = obj match {
    case other: IntSlice =>
      this.length == other.length &&
        sameElements(this.iterator, other.iterator)

    case _ => false
  }

  /** Checks if two iterators would return same elements. */
  def sameElements(iterator1: Iterator[Int], iterator2: Iterator[Int]): Boolean = {
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

object IntSlice {

  def apply(is: Int*): IntSlice = IntSlice.of(Array(is: _*))

  def of(array: Array[Int]): IntSlice = new IntSlice(0, array.length, array)

  def of(array: Array[Int], from: Int, to: Int): IntSlice = {
    assert(from >= 0, "When creating a Slice, parameter `from` must be greater or equal to 0.")
    assert(to <= array.length, "When creating a Slice, parameter `to` must be lower or equal to the array length.")
    assert(from <= to, "When creating a Slice, parameter `from` must be lower or equal to `to`.")
    new IntSlice(from, to, array)
  }

  def empty: IntSlice = IntSlice()

}
