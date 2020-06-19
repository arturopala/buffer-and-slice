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

/** Lazy and immutable slice of a sequence of values.
  * @tparam T type of the items of the sequence.
  *
  * @note As the slice usually wraps over a mutable structure,
  *       like an array or a java buffer, and it DOES NOT
  *       make an instant copy, any changes to the underlying source will
  *       directly affect the slice output unless detached.
  *       Detach is a one-time copy operation, and `detached` property is
  *       preserved across all operations returning a Slice.
  *
  * @groupprio Properties 0
  * @groupprio Access 1
  * @groupprio Find 2
  * @groupprio Transform 3
  * @groupprio Aggregate 4
  * @groupprio Iterate 5
  * @groupprio Export 6
  * @groupprio Internal 7
  */
trait Slice[T] extends (Int => T) {

  /** Returns value at the given index without checks.
    * @group Internal */
  protected def read(index: Int): T

  /** Returns value at the given index.
    * @group Access */
  final override def apply(index: Int): T = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `apply` index in the interval [0,$length), but was $index.")
    read(index)
  }

  /** Update a value at the given index.
    * Creates a copy of an underlying data.
    * @group Access */
  def update[T1 >: T](index: Int, value: T1): Slice[T1]

  /** Returns length of the Slice.
    * @group Properties */
  val length: Int

  /** Returns top index value (length-1).
    * @group Properties */
  @`inline` final def top: Int = length - 1

  /** Returns true if Slice has values, otherwise false.
    * @group Properties */
  final def isEmpty: Boolean = length <= 0

  /** Returns true if Slice has values, otherwise false.
    * @group Properties */
  @`inline` final def nonEmpty: Boolean = !isEmpty

  /** Lazily composes mapping function and returns new Slice.
    * Does not modify nor copy underlying array.
    * @group Transform */
  def map[K](f: T => K): Slice[K]

  /** Counts values fulfilling the predicate.
    * @group Aggregate */
  final def count(pred: T => Boolean): Int = {
    var a = 0
    var i = 0
    while (i < length) {
      if (pred(read(i))) a = a + 1
      i = i + 1
    }
    a
  }

  /** Folds from left to right all elements, starting with initial.
    * @group Aggregate */
  final def foldLeft[R](initial: R)(f: (R, T) => R): R = {
    var acc: R = initial
    var i = 0
    while (i < length) {
      acc = f(acc, read(i))
      i = i + 1
    }
    acc
  }

  /** Folds from right to left all elements, starting with initial.
    * @group Aggregate */
  final def foldRight[R](initial: R)(f: (T, R) => R): R = {
    var acc: R = initial
    var i = length - 1
    while (i >= 0) {
      acc = f(read(i), acc)
      i = i - 1
    }
    acc
  }

  /** Combines from left to right all elements, starting with initial.
    * @group Aggregate */
  final def fold(initial: T)(f: (T, T) => T): T = {
    var acc: T = initial
    var i = 0
    while (i < length) {
      acc = f(acc, read(i))
      i = i + 1
    }
    acc
  }

  /** Combines from right to left all elements.
    * @group Aggregate */
  final def reduce(f: (T, T) => T): T =
    if (isEmpty) throw new UnsupportedOperationException
    else {
      var acc: T = head
      var i = 1
      while (i < length) {
        acc = f(acc, read(i))
        i = i + 1
      }
      acc
    }

  /** Returns first value in the Slice.
    * @group Access */
  final def head: T =
    if (length > 0) read(0)
    else throw new NoSuchElementException

  /** Returns the last value in the Slice.
    * @group Access */
  final def last: T =
    if (length > 0) read(length - 1)
    else throw new NoSuchElementException

  /** Returns first value in the Slice.
    * @group Access */
  final def headOption: Option[T] =
    if (length > 0) Some(read(0))
    else None

  /** Returns the last value in the Slice.
    * @group Access */
  final def lastOption: Option[T] =
    if (length > 0) Some(read(length - 1))
    else None

  /** Returns Some of the value at the index,
    * or None if index outside of range. */
  final def get(index: Int): Option[T] =
    if (index < 0 || index >= length) None
    else Some(read(index))

  /** Returns Some of the first value fulfilling the predicate, or None. */
  final def find(pred: T => Boolean): Option[T] = {
    var result: Option[T] = None
    var i = 0
    while (i < length) {
      val v = read(i)
      if (pred(v)) {
        result = Some(v)
        i = length + 1
      } else {
        i = i + 1
      }
    }
    result
  }

  /** Returns true if any value fulfills the predicate, or false. */
  final def exists(pred: T => Boolean): Boolean = {
    var i = 0
    while (i >= 0 && i < length) {
      val v = read(i)
      if (pred(v)) i = -1
      else i = i + 1
    }
    i < 0
  }

  /** Lazily narrows Slice to provided range.
    * @group Transform */
  def slice(from: Int, to: Int): this.type

  /** Returns the Slice without first value.
    * @group Access */
  @`inline` final def tail: this.type = drop(1)

  /** Returns the Slice without last value.
    * @group Access */
  @`inline` final def init: this.type = dropRight(1)

  /** Lazily narrows Slice to first N items.
    * @group Transform */
  @`inline` final def take(n: Int): this.type = slice(0, n)

  /** Lazily narrows Slice to last N items.
    * @group Transform */
  @`inline` final def takeRight(n: Int): this.type = slice(length - n, length)

  /** Lazily narrows Slice to exclude first N items.
    * @group Transform */
  @`inline` final def drop(n: Int): this.type = slice(n, length)

  /** Lazily narrows Slice to exclude last N items.
    * @group Transform */
  @`inline` final def dropRight(n: Int): this.type = slice(0, length - n)

  /** Returns iterator over Slice values.
    * @group Iterate */
  final def iterator: Iterator[T] = new Iterator[T] {

    var i = 0

    def hasNext: Boolean = i < Slice.this.length

    def next(): T = {
      val value = read(i)
      i = i + 1
      value
    }
  }

  /** Returns iterator over Slice values fulfilling the predicate.
    * @group Iterate */
  final def iterator(pred: T => Boolean): Iterator[T] =
    indexIterator(pred).map(read)

  /** Returns iterator over Slice indexes of values fulfilling the predicate.
    * @group Iterate */
  final def indexIterator(pred: T => Boolean): Iterator[Int] = new Iterator[Int] {

    var i = 0

    seekNext

    def hasNext: Boolean = i < Slice.this.length

    def next(): Int = {
      val item = i
      i = i + 1
      seekNext
      item
    }

    def seekNext: Unit =
      if (i < Slice.this.length) {
        var v = read(i)
        while (!pred(v) && i < Slice.this.length) {
          i = i + 1
          if (i < Slice.this.length) v = read(i)
        }
      } else ()
  }

  /** Returns iterator over Slice values in the reverse order.
    * @group Iterate */
  final def reverseIterator: Iterator[T] = new Iterator[T] {

    var i = Slice.this.length - 1

    def hasNext: Boolean = i >= 0

    def next(): T = {
      val value = read(i)
      i = i - 1
      value
    }
  }

  /** Returns iterator over Slice values fulfilling the predicate, in the reverse order.
    * @group Iterate */
  final def reverseIterator(pred: T => Boolean): Iterator[T] =
    reverseIndexIterator(pred).map(read)

  /** Returns iterator over Slice indexes of values fulfilling the predicate, in the reverse order.
    * @group Iterate */
  final def reverseIndexIterator(pred: T => Boolean): Iterator[Int] = new Iterator[Int] {

    var i = Slice.this.length - 1

    seekNext

    def hasNext: Boolean = i >= 0

    def next(): Int = {
      val item = i
      i = i - 1
      seekNext
      item
    }

    def seekNext: Unit =
      if (i >= 0) {
        var v = read(i)
        while (!pred(v) && i >= 0) {
          i = i - 1
          if (i >= 0) v = read(i)
        }
      } else ()
  }

  /** Returns new list of Slice values.
    * @group Export */
  @`inline` final def toList: List[T] = iterator.toList

  /** Returns new sequence of Slice values.
    * @group Export */
  @`inline` final def toSeq: Seq[T] = iterator.toIndexedSeq

  /** Returns new iterable of Slice values.
    * @group Export */
  final def asIterable: Iterable[T] = new AbstractIterable[T] {
    override def iterator: Iterator[T] = Slice.this.iterator
    override def toString(): String = Slice.this.toString
  }

  /** Returns a trimmed copy of an underlying array.
    * @group Export */
  def toArray[T1 >: T: ClassTag]: Array[T1]

  /** Returns a trimmed copy of an underlying array.
    * @group Export */
  def asArray: Array[T]

  /** Detaches a slice creating a trimmed copy of an underlying data, if needed.
    * Subsequent detach operations will return the same instance without making new copies.
    * @group Access */
  def detach: this.type

  /** Dumps content to the array, starting from an index.
    * @group Export */
  def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1]

  /** Returns a buffer with a copy of this Slice.
    * @group Export */
  def toBuffer[T1 >: T]: Buffer[T1]

  /** Returns a buffer with a copy of this Slice.
    * @group Export */
  def asBuffer: Buffer[T]

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
    hash = hash * 31 + length
    for (i <- 0 until length by Math.max(1, length / 7)) {
      hash = hash * 31 + read(i).hashCode()
    }
    hash
  }

}

/** Slice companion object. Host factory methods. */
object Slice {

  /** Creates new detached Slice out of given value sequence. */
  def apply[T](head: T, tail: T*): Slice[T] =
    if (head.isInstanceOf[Int])
      IntSlice(head.asInstanceOf[Int], tail.asInstanceOf[scala.Seq[Int]]: _*).asInstanceOf[Slice[T]]
    else if (head.isInstanceOf[Byte])
      ByteSlice(head.asInstanceOf[Byte], tail.asInstanceOf[scala.Seq[Byte]]: _*).asInstanceOf[Slice[T]]
    else ArraySlice(head, tail: _*)

  /** Creates new Slice of given array values.
    *
    * @note This DOES NOT make a copy, any changes to the underlying array will
    * directly affect the slice output. For full immutability does not share
    * array reference with other components.
    */
  def of[T](array: Array[T]): Slice[T] =
    if (array.isInstanceOf[Array[Int]])
      IntSlice.of(array.asInstanceOf[Array[Int]]).asInstanceOf[Slice[T]]
    else if (array.isInstanceOf[Array[Byte]])
      ByteSlice.of(array.asInstanceOf[Array[Byte]]).asInstanceOf[Slice[T]]
    else ArraySlice.of(array)

  /** Creates new Slice of given subset of array values. */
  def of[T](array: Array[T], from: Int, to: Int): Slice[T] =
    if (array.isInstanceOf[Array[Int]])
      IntSlice.of(array.asInstanceOf[Array[Int]], from, to).asInstanceOf[Slice[T]]
    else if (array.isInstanceOf[Array[Byte]])
      ByteSlice.of(array.asInstanceOf[Array[Byte]], from, to).asInstanceOf[Slice[T]]
    else ArraySlice.of(array, from, to)

  /** Creates an empty Slice of given type. */
  def empty[T]: Slice[T] = ArraySlice.empty[T]

}
