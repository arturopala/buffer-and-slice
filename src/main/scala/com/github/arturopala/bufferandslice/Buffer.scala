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
  * All modifications happens in-place.
  *
  * In addition, the Buffer API offers both Stack- and List-like interfaces.
  * For the purpose of the List-like processing,
  * the head is a top element in the buffer.
  *
  * @groupprio Abstract 0
  * @groupprio Properties 1
  * @groupprio List 2
  * @groupname List List-like
  * @groupdesc List List like operations, head is the top element
  * @groupprio Stack 3
  * @groupname Stack Stack-like
  * @groupdesc Stack Stack like operations, peek takes the top element
  * @groupprio Slice 4
  * @groupprio Append 5
  * @groupprio Insert 6
  * @groupprio Modify 7
  * @groupprio Replace 8
  * @groupprio Remove 9
  * @groupprio Shift 10
  * @groupprio Move 11
  * @groupprio Swap 12
  * @groupprio Limit 13
  * @groupdesc Limit Manipulations of the topIndex
  * @groupprio Read 14
  */
trait Buffer[T] extends (Int => T) {

  /** Index of the last accessible value in the buffer, or -1 if empty. */
  private var topIndex: Int = -1

  protected def uncheckedApply(index: Int): T
  protected def uncheckedUpdate(index: Int, value: T): Unit

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

  /** Creates a copy of this buffer.
    * @group Abstract */
  def copy: this.type

  /** Returns an Array with a copy of an accessible buffer range.
    * @group Read */
  def toArray: Array[T]

  /** Wraps accessible internal state as a Slice without making any copy.
    * @group Slice */
  def asSlice: Slice[T]

  /** Takes range and returns a Slice.
    * @group Slice */
  def slice(from: Int, to: Int): Slice[T]

  /** Length of the accessible part of the buffer.
    * @group Properties */
  @`inline` final def length: Int = topIndex + 1

  /** Is the accessible part of the buffer empty?
    * @group Properties */
  @`inline` final def isEmpty: Boolean = length == 0

  /** Is the accessible part of the buffer non empty?
    * @group Properties */
  @`inline` final def nonEmpty: Boolean = length > 0

  /** Returns topIndex value.
    * @group Limit */
  @`inline` final def top: Int = topIndex

  /** Returns value at the topIndex.
    * @group List */
  @`inline` final def head: T = apply(topIndex)

  /** Returns Some value at the topIndex
    * or None if empty buffer.
    * @group List */
  @`inline` final def headOption: Option[T] =
    if (topIndex < 0) None else Some(uncheckedApply(topIndex))

  /** Returns value at the zero index.
    * @group List */
  @`inline` final def last: T = apply(0)

  /** Returns Some value at the zero index,
    * or None if empty buffer.
    * @group List */
  @`inline` final def lastOption: Option[T] =
    if (topIndex < 0) None else Some(uncheckedApply(0))

  /** Returns this buffer after decrementing topIndex .
    * @group List */
  @`inline` final def tail: this.type = rewind(1)

  /** Returns this buffer without a first element.
    * @group List */
  @`inline` final def init: this.type = shiftLeft(1, 1)

  /** Returns Some value at the index, or None if index outside of range.
    * @group List */
  @`inline` final def get(index: Int): Option[T] =
    if (index >= 0 && index <= topIndex) Some(uncheckedApply(index))
    else None

  /** Updates value at the provided index using the function.
    * Index must fall within range [0,length).
    * @param index value's index
    * @param map map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Modify */
  final def modify(index: Int, map: T => T): this.type = {
    if (index >= 0 && index < length) {
      uncheckedUpdate(index, map(uncheckedApply(index)))
      topIndex = Math.max(index, topIndex)
    }
    this
  }

  /** Updates all values in the range [0,length) using the function.
    * @param f map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Modify */
  @`inline` final def map(f: T => T): this.type = modifyAll(f)

  /** Updates all values in the range [0,length) using the function.
    * @param map map function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Modify */
  final def modifyAll(map: T => T): this.type = {
    var i = 0
    while (i < length) {
      uncheckedUpdate(i, map(uncheckedApply(i)))
      i = i + 1
    }
    this
  }

  /** Updates all accepted values in the range [0,length) using the function.
    * @param map map function
    * @param pred filter function
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Modify */
  final def modifyAllWhen(map: T => T, pred: T => Boolean): this.type = {
    var i = 0
    while (i < length) {
      val v = uncheckedApply(i)
      if (pred(v)) uncheckedUpdate(i, map(v))
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
    * @group Modify */
  final def modifyRange(fromIndex: Int, toIndex: Int, map: T => T): this.type = {
    if (toIndex > 0 && fromIndex < length) {
      var i = Math.max(0, fromIndex)
      val limit = Math.min(length, toIndex)
      while (i < limit) {
        uncheckedUpdate(i, map(uncheckedApply(i)))
        i = i + 1
      }
      touch(i - 1)

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
    * @group Modify */
  final def modifyRangeWhen(fromIndex: Int, toIndex: Int, map: T => T, pred: T => Boolean): this.type = {
    if (toIndex > 0 && fromIndex < length) {
      var i = Math.max(0, fromIndex)
      val limit = Math.min(length, toIndex)
      while (i < limit) {
        val v = uncheckedApply(i)
        if (pred(v)) uncheckedUpdate(i, map(v))
        i = i + 1
      }
      touch(i - 1)

    }
    this
  }

  /** Sets topIndex value.
    * @group Limit */
  final def set(index: Int): this.type = {
    ensureIndex(index)
    topIndex = Math.max(-1, index)
    this
  }

  /** Sets topIndex value if lower than index,
    * otherwise keeps existing.
    * @group Limit */
  final def touch(index: Int): this.type = {
    ensureIndex(index)
    topIndex = Math.max(-1, Math.max(index, topIndex))
    this
  }

  /** Trims the buffer, if needed, to be have at most the `size`.
    * @group Limit */
  final def trim(size: Int): this.type = {
    if (size >= 0 && size < length) {
      topIndex = size - 1
    }
    this
  }

  /** Moves topIndex value left by the distance.
    * @group Limit */
  final def rewind(distance: Int): this.type = {
    ensureIndex(Math.max(-1, topIndex - distance))
    topIndex = Math.max(-1, topIndex - distance)
    this
  }

  /** Moves topIndex value right by the distance.
    * @group Limit */
  final def forward(distance: Int): this.type = {
    ensureIndex(topIndex + distance)
    topIndex =
      if (topIndex < 0) distance
      else topIndex + distance
    this
  }

  /** Resets buffer, sets topIndex to -1.
    * - Does not clear existing values.
    * @return previous topIndex
    * @group Limit */
  final def reset: Int = {
    val i = topIndex
    topIndex = -1
    i
  }

  /** Appends value at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def append(value: T): this.type = {
    update(length, value)
    this
  }

  /** Appends values from the given array at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendArray(values: Array[T]): this.type =
    insertArray(length, 0, values.length, values)

  /** Appends values from the given slice at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendSlice(slice: Slice[T]): this.type =
    insertSlice(length, slice)

  /** Appends values from the given sequence at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendSequence(values: IndexedSeq[T]): this.type =
    insertValues(length, 0, values.length, values)

  /** Appends values from the given iterator at the end of the buffer and advances topIndex.
    * @group Append */
  final def appendFromIterator(iterator: Iterator[T]): this.type = {
    var d = 1
    while (iterator.hasNext) {
      insertFromIterator(length, d, iterator)
      d = Math.min(d * 2, 256)
    }
    this
  }

  /** Appends number of values from the given iterator at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendFromIterator(numberOfValues: Int, iterator: Iterator[T]): this.type =
    insertFromIterator(length, numberOfValues, iterator)

  /** Appends values from the given iterable at the end of the buffer and advances topIndex.
    * @group Append */
  @`inline` final def appendIterable(iterable: Iterable[T]): this.type =
    appendFromIterator(iterable.size, iterable.iterator)

  /** Shifts content in [index, length) one step to the right and updates value at index.
    * @group Insert
    */
  final def insert(index: Int, value: T): this.type = {
    if (index >= 0) {
      shiftRight(index, 1)
      uncheckedUpdate(index, value)
      touch(index)
    }
    this
  }

  /** Shift current content to the right starting from `index` at the `insertLength` distance,
    * and copies array chunk into the gap.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  def insertArray(index: Int, sourceIndex: Int, insertLength: Int, sourceArray: Array[T]): this.type

  /** Shift current content to the right starting from `index` at the `slice.length` distance,
    * and copies slice into the gap.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  def insertSlice(index: Int, slice: Slice[T]): this.type

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * iterates over the source indexes and copies values into the gap.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertValues(index: Int, sourceIndex: Int, numberOfValues: Int, source: Int => T): this.type = {
    if (index >= 0 && sourceIndex >= 0) {
      if (numberOfValues > 0) {
        shiftRight(index, numberOfValues)
        var i = 0
        while (i < numberOfValues) {
          uncheckedUpdate(index + i, source(sourceIndex + i))
          i = i + 1
        }
        touch(index + numberOfValues - 1)
      }
    }
    this
  }

  /** Inserts iterated values into the gap made by shiftjng buffer right, starting from the index.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertFromIterator(index: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      var i = index
      var d = 1
      var l = Math.max(length, index)
      while (iterator.hasNext) {
        insertFromIterator(i, d, iterator)
        i = i + (length - l)
        l = length
        d = Math.min(d * 2, 256)
      }
    }
    this
  }

  /** Inserts iterated values, in the reverse order, into the gap made by shiftjng buffer right, starting from the index.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertFromIteratorReverse(index: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      var d = 1
      while (iterator.hasNext) {
        insertFromIteratorReverse(index, d, iterator)
        d = Math.min(d * 2, 64)
      }
    }
    this
  }

  /** Shift current content to the right starting from `index`at the `min(iterator.length, insertLength)` distance,
    * and inserts iterated values into the gap.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertFromIterator(index: Int, numberOfValues: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      if (numberOfValues > 0) {
        shiftRight(index, numberOfValues)
        var i = 0
        while (i < numberOfValues && iterator.hasNext) {
          uncheckedUpdate(index + i, iterator.next())
          i = i + 1
        }
        touch(index + numberOfValues - 1)
        if (i <= numberOfValues) {
          shiftLeft(index + numberOfValues, numberOfValues - i)
        }
      }
    }
    this
  }

  /** Shift current content to the right starting from `index`at the `min(iterator.length, insertLength)` distance,
    * and inserts iterated values into the gap in the reverse order.
    * - Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final def insertFromIteratorReverse(index: Int, numberOfValues: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      if (numberOfValues > 0) {
        shiftRight(index, numberOfValues)
        var i = index + numberOfValues - 1
        while (i >= index && iterator.hasNext) {
          uncheckedUpdate(i, iterator.next())
          i = i - 1
        }
        touch(index + numberOfValues - 1)
        if (i >= index) {
          shiftLeft(i + 1, i - index + 1)
        }
      }
    }
    this
  }

  /** Replaces current values in the range [index, index + replaceLength)
    * with values of the array range [sourceIndex, sourceIndex + replaceLength).
    * @group Replace */
  def replaceFromArray(index: Int, sourceIndex: Int, replaceLength: Int, sourceArray: Array[T]): this.type

  /** Replaces current values in the range [index, index + slice.length) with values of the slice.
    * @group Replace */
  def replaceFromSlice(index: Int, slice: Slice[T]): this.type

  /** Replaces current values in the range [index, index + replaceLength) with values returned by the function
    * when iterating argument in the range [sourceIndex, sourceIndex + replaceLength).
    * @group Replace */
  final def replaceValues(index: Int, sourceIndex: Int, numberOfValues: Int, source: Int => T): this.type = {
    if (index >= 0 && sourceIndex >= 0) {
      if (numberOfValues > 0) {
        ensureIndex(index + numberOfValues - 1)
        var i = 0
        while (i < numberOfValues) {
          uncheckedUpdate(index + i, source(sourceIndex + i))
          i = i + 1
        }
      }
      touch(index + numberOfValues - 1)
    }
    this
  }

  /** Replaces current values in the range [index, index + min(iterator.length, replaceLength) )
    * with values returned from the iterator.
    * @group Replace */
  final def replaceFromIterator(index: Int, numberOfValues: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      if (numberOfValues > 0) {
        ensureIndex(index + numberOfValues - 1)
        var i = 0
        while (i < numberOfValues && iterator.hasNext) {
          uncheckedUpdate(index + i, iterator.next())
          i = i + 1
        }
        touch(index + Math.min(i, numberOfValues) - 1)
      }
    }
    this
  }

  /** Replaces current values in the range [index, index + min(iterator.length, replaceLength))
    * with values returned from the iterator in the reverse order.
    * @group Replace */
  final def replaceFromIteratorReverse(index: Int, numberOfValues: Int, iterator: Iterator[T]): this.type = {
    if (index >= 0) {
      if (numberOfValues > 0) {
        ensureIndex(index + numberOfValues - 1)
        var i = index + numberOfValues - 1
        while (i >= index && iterator.hasNext) {
          uncheckedUpdate(i, iterator.next())
          i = i - 1
        }
        touch(index + numberOfValues - 1)
      }
    }
    this
  }

  /** Removes value at index and shifts content in [index+1, length) to the left.
    * @group Remove
    */
  @`inline` final def remove(index: Int): this.type =
    shiftLeft(index + 1, 1)

  /** Removes values in the range [fromIndex, toIndex) and shifts content in [toIndex, length) to the left.
    * @group Remove */
  @`inline` final def removeRange(fromIndex: Int, toIndex: Int): this.type =
    shiftLeft(toIndex, toIndex - fromIndex)

  /** Removes values matching the predicate.
    * @group Remove */
  @`inline` final def removeWhen(pred: T => Boolean): this.type = {
    var i = 0
    while (i < length) {
      if (pred(uncheckedApply(i))) {
        remove(i)
      } else {
        i = i + 1
      }
    }
    this
  }

  /** Moves values in [index, length) right to [index+distance, length + distance).
    * Effectively creates a new range [index, index+distance).
    * - Ignores negative distance.
    * - Does not clear existing values inside [index, index+distance).
    * - Moves topIndex if affected.
    * @group Shift */
  def shiftRight(index: Int, distance: Int): this.type

  /** Moves values in [index, length) left to [index-distance, length - distance).
    * Effectively removes range (index-distance, index].
    * - Ignores negative distance.
    * - Moves topIndex if affected.
    * @group Shift */
  def shiftLeft(index: Int, distance: Int): this.type

  /** Moves values in [fromIndex, toIndex) to the right at a distance,
    * to become [fromIndex + distance, toIndex + distance),
    * and moves left any existing values in [toIndex, toIndex + distance)
    * to become [fromIndex, fromIndex + distance).
    * - Ignores negative distance.
    * - Does nothing if fromIndex > topIndex.
    * - Moves topIndex if affected.
    * @group Move
    * */
  def moveRangeRight(fromIndex: Int, toIndex: Int, distance: Int): this.type

  /** Moves values in [fromIndex, toIndex) to the left at a distance,
    * to become [fromIndex - distance, toIndex - distance),
    * and moves right any existing values in [fromIndex - distance, fromIndex)
    * to become [toIndex, toIndex + distance).
    * - Ignores negative distance.
    * - Does nothing if fromIndex > topIndex.
    * - Moves topIndex if affected.
    * @group Move
    * */
  def moveRangeLeft(fromIndex: Int, toIndex: Int, distance: Int): this.type

  /** Swap two values at the provided indexes.
    * Value at `first` becomes value at `second`, and vice versa.
    * - Does nothing if any index falls outside [0,length) or if indexes are equal.
    * @group Swap
    */
  final def swap(first: Int, second: Int): this.type = {
    if (first >= 0 && second >= 0 && first != second && first < length && second < length) {
      val v = uncheckedApply(first)
      uncheckedUpdate(first, uncheckedApply(second))
      uncheckedUpdate(second, v)
    }
    this
  }

  /** Swap values in range  [first, first + swapLength) with values in range [second, second + swapLength)
    * - Does nothing if any index falls outside [0,length) or if indexes are equal.
    *  - if [first, first + swapLength) overlaps with [second, second + swapLength)
    *    then the later overwrites the former.
    * @group Swap
    */
  def swapRange(first: Int, second: Int, swapLength: Int): this.type

  /** Replace value at the topIndex.
    * @group Stack */
  final def store(value: T): this.type = {
    if (topIndex < 0) topIndex = 0
    uncheckedUpdate(topIndex, value)
    this
  }

  /** Appends value to the topIndex.
    * Same as [[append]]
    * @group Stack */
  @`inline` final def push(value: T): this.type = {
    update(length, value)
    this
  }

  /** Returns value at the topIndex.
    * @group Stack */
  @`inline` final def peek: T = apply(topIndex)

  /** Returns value at the topIndex - offset.
    * @group Stack */
  @`inline` final def peek(offset: Int): T = apply(topIndex - offset)

  /** Returns value at the topIndex - offset.
    * @group Stack */
  @`inline` final def peekOption(offset: Int): Option[T] = get(topIndex - offset)

  /** Returns value at the topIndex, and moves topIndex back.
    * @group Stack */
  final def pop: T = {
    val value = peek
    topIndex = topIndex - 1
    value
  }

  /** Returns an iterator over actual buffer values,
    * starting from the zero index up.
    * @note does not copy buffer values,
    * @group Read */
  final def iterator: Iterator[T] = new Iterator[T] {
    var i: Int = 0
    def hasNext: Boolean = i <= topIndex
    def next(): T = {
      val value = uncheckedApply(i)
      i = i + 1
      value
    }
  }

  /** Returns a reverse iterator over actual buffer values,
    * starting from the topIndex down.
    * @group Read */
  final def reverseIterator: Iterator[T] = new Iterator[T] {
    var i: Int = topIndex
    def hasNext: Boolean = i >= 0
    def next(): T = {
      val value = uncheckedApply(i)
      i = i - 1
      value
    }
  }

}

/** Buffer factory. */
object Buffer {

  @`inline` def apply[T: ClassTag](elems: T*): Buffer[T] = apply(elems.toArray)

  def apply[T: ClassTag](array: Array[T]): Buffer[T] =
    if (array.isInstanceOf[Array[Int]])
      new IntBuffer(array.length)
        .appendArray(array.asInstanceOf[Array[Int]])
        .asInstanceOf[Buffer[T]]
    else if (array.isInstanceOf[Array[Byte]])
      new ByteBuffer(array.length)
        .appendArray(array.asInstanceOf[Array[Byte]])
        .asInstanceOf[Buffer[T]]
    else
      new ArrayBuffer[T](array)

  @`inline` def apply[T: ClassTag](array: Array[T], length: Int): Buffer[T] =
    apply(array).set(length)

  def ofSize[T: ClassTag](size: Int): Buffer[T] =
    new ArrayBuffer[T](new Array[T](size))

  def empty[T: ClassTag]: Buffer[T] = {
    val buffer = new ArrayBuffer[T](new Array[T](8))
    buffer.reset
    buffer
  }

}
