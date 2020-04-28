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

/** Mutable indexed buffer abstraction.
  *
  * @groupprio Abstract 0
  * @groupprio Properties 1
  * @groupprio Update 2
  * @groupprio Append 3
  * @groupprio Insert 4
  * @groupprio Shift 5
  * @groupprio Stack Ops 6
  * @groupprio Limit 7
  */
trait Buffer[T] extends (Int => T) {

  /** Index of the last accessible value in the buffer, or -1 if empty. */
  private var topIndex: Int = -1

  /** Returns value at the provided index.
    * @group Abstract */
  def apply(index: Int): T

  /** Updates value at the provided index.
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Abstract */
  def update(index: Int, value: T): this.type

  /** Ensures buffer capacity to address provided index.
    * @group Abstract */
  protected def ensureIndex(index: Int): Unit

  /** Returns an Array representing accessible buffer range.
    * @group Abstract */
  def toArray: Array[T]

  /** Returns a Slice representing accessible buffer range.
    * @group Abstract */
  def toSlice: Slice[T]

  /** Updates value at the provided index using the function.
    * Index must fall within range [0,length).
    * @param index value's index
    * @param map map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def modify(index: Int, map: T => T): this.type = {
    if (index >= 0 && index < length) {
      update(index, map(apply(index)))
      topIndex = Math.max(index, topIndex)
    }
    this
  }

  /** Updates all values in the range [0,length) using the function.
    * @param map map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def modifyAll(map: T => T): this.type = {
    var i = 0
    while (i < length) {
      update(i, map(apply(i)))
      i = i + 1
    }
    this
  }

  /** Updates all accepted values in the range [0,length) using the function.
    * @param map map function
    * @param pred filter function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def modifyAllWhen(map: T => T, pred: T => Boolean): this.type = {
    var i = 0
    while (i < length) {
      val v = apply(i)
      if (pred(v)) update(i, map(v))
      i = i + 1
    }
    this
  }

  /** Updates values in the range [startIndex,toIndex)^^[0,length) using the function.
    * One of {startIndex,toIndex} must fall within range [0,length).
    * @param fromIndex index of the first value inclusive
    * @param toIndex index of the last value exclusive
    * @param map      map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def modifyRange(fromIndex: Int, toIndex: Int, map: T => T): this.type = {
    if (toIndex > 0 && fromIndex < length) {
      var i = Math.max(0, fromIndex)
      val limit = Math.min(length, toIndex)
      while (i < limit) {
        update(i, map(apply(i)))
        i = i + 1
      }
      topIndex = Math.max(topIndex, i - 1)

    }
    this
  }

  /** Updates values in the range [startIndex,toIndex)^^[0,length) using the function.
    * One of {startIndex,toIndex} must fall within range [0,length).
    * @param fromIndex index of the first value inclusive
    * @param toIndex index of the last value exclusive
    * @param map map function
    * @param pred filter function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def modifyRangeWhen(fromIndex: Int, toIndex: Int, map: T => T, pred: T => Boolean): this.type = {
    if (toIndex > 0 && fromIndex < length) {
      var i = Math.max(0, fromIndex)
      val limit = Math.min(length, toIndex)
      while (i < limit) {
        val v = apply(i)
        if (pred(v)) update(i, map(v))
        i = i + 1
      }
      topIndex = Math.max(topIndex, i - 1)

    }
    this
  }

  /** Length of the accessible part of the buffer.
    * @group Properties */
  final def length: Int = topIndex + 1

  /** Is the accessible part of the buffer empty?
    * @group Properties */
  @`inline` final def isEmpty: Boolean = length == 0

  /** Is the accessible part of the buffer non empty?
    * @group Properties */
  @`inline` final def nonEmpty: Boolean = length > 0

  /** Sets topIndex value.
    * @group Limit */
  final def set(index: Int): this.type = {
    ensureIndex(index)
    topIndex = Math.max(-1, index)
    this
  }

  /** Returns topIndex value.
    * @group Limit */
  final def top: Int = topIndex

  /** Resets buffer, sets topIndex to -1.
    * Does not clear existing values.
    * @return previous topIndex
    * @group Limit */
  final def reset: Int = {
    val i = topIndex
    topIndex = -1
    i
  }

  /** Appends value at the end of buffer and advances topIndex.
    * @group Append */
  @`inline` final def append(value: T): this.type = {
    update(length, value)
    this
  }

  /** Appends values from the given array at the end of buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendArray(values: Array[T]): this.type =
    insertArray(length, 0, values.length, values)

  /** Appends values from the given sequence at the end of buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendSequence(values: IndexedSeq[T]): this.type =
    insertValues(length, 0, values.length, values)

  /** Appends values from the given iterator at the end of buffer and advances topIndex.
    * @group Append */
  final def appendFromIterator(iterator: Iterator[T]): this.type = {
    while (iterator.hasNext) {
      append(iterator.next())
    }
    this
  }

  /** Appends values from the given iterable at the end of buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendIterable(iterable: Iterable[T]): this.type = appendFromIterator(iterable.iterator)

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * and copies array chunk into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  def insertArray(index: Int, sourceIndex: Int, insertLength: Int, sourceArray: Array[T]): this.type

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * iterates over the source indexes and copies values into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertValues(index: Int, sourceIndex: Int, insertLength: Int, source: Int => T): this.type = {
    if (index >= 0 && sourceIndex >= 0) {
      if (insertLength > 0) {
        shiftRight(index, insertLength)
        var i = 0
        while (i < insertLength) {
          update(index + i, source(sourceIndex + i))
          i = i + 1
        }
      }
      topIndex = Math.max(topIndex, index + insertLength - 1)
    }
    this
  }

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * and copies iterated values into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertFromIterator(index: Int, insertLength: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      if (insertLength > 0) {
        shiftRight(index, insertLength)
        var i = 0
        while (i < insertLength && iterator.hasNext) {
          update(index + i, iterator.next())
          i = i + 1
        }
      }
      topIndex = Math.max(topIndex, index + insertLength - 1)
    }
    this
  }

  /** Moves values [index, length) right to [index+distance, length + distance).
    * Effectively creates a new range [index, index+distance).
    * Ignores negative distance.
    * Does not clear existing values inside [index, index+distance).
    * Moves topIndex if affected.
    * @group Shift */
  def shiftRight(index: Int, distance: Int): this.type

  /** Moves values [index, length) left to [index-distance, length - distance).
    * Effectively removes range (index-distance, index].
    * Ignores negative distance.
    * Moves topIndex if affected.
    * @group Shift */
  def shiftLeft(index: Int, distance: Int): this.type

  /** Replace value at the topIndex.
    * @group Stack Ops */
  final def store(value: T): this.type = {
    if (topIndex < 0) topIndex = 0
    update(topIndex, value)
    this
  }

  /** Appends value to the topIndex.
    * Same as [[append]]
    * @group Stack Ops */
  @`inline` final def push(value: T): this.type = {
    update(length, value)
    this
  }

  /** Returns value at the topIndex.
    * @group Stack Ops */
  final def peek: T = apply(topIndex)

  /** Returns value at the topIndex, and moves topIndex back.
    * @group Stack Ops */
  final def pop: T = {
    val value = peek
    topIndex = topIndex - 1
    value
  }

}

/** Buffer factory. */
object Buffer {

  def apply[T: ClassTag](elems: T*): ArrayBuffer[T] = new ArrayBuffer[T](elems.toArray)

  def apply[T: ClassTag](array: Array[T]): ArrayBuffer[T] = new ArrayBuffer[T](array)

  def ofSize[T: ClassTag](size: Int): ArrayBuffer[T] = new ArrayBuffer[T](new Array[T](size))

  def empty[T: ClassTag] = new ArrayBuffer[T](Array.empty[T])

}
