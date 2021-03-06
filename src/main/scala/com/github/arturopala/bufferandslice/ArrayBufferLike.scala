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

/** Common buffer functions impl for array-backend buffers. */
trait ArrayBufferLike[T] extends Buffer[T] {

  protected def copyFrom(sourceArray: Array[T], sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit
  protected def copyFrom(slice: Slice[T], targetIndex: Int): Unit
  protected def copyFromSelf(sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit
  protected def emptyArray(length: Int): Array[T]

  /** Updates value at the provided index.
    * Alters underlying array if necessary.
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final override def update(index: Int, value: T): this.type =
    if (index < 0) throw new IndexOutOfBoundsException
    else {
      ensureIndex(index)
      write(index, value)
      set(Math.max(index, top))
      this
    }

  final override def toString: String =
    iterator.take(Math.min(20, length)).mkString("[", ",", if (length > 20) ", ... ]" else "]")

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * and copies array chunk into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final override def insertArray(index: Int, sourceIndex: Int, insertLength: Int, sourceArray: Array[T]): this.type = {
    if (index < 0 || sourceIndex < 0) throw new IndexOutOfBoundsException
    val copyLength = Math.min(insertLength, sourceArray.length)
    if (copyLength > 0) {
      shiftRight(index, copyLength)
      copyFrom(sourceArray, sourceIndex, index, copyLength)
    }
    set(Math.max(top, index + copyLength - 1))
    this
  }

  /** Shift current content to the right starting from `index` at the `slice.length` distance,
    * and copies slice content into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final override def insertSlice(index: Int, slice: Slice[T]): this.type = {
    if (index < 0) throw new IndexOutOfBoundsException
    shiftRight(index, slice.length)
    copyFrom(slice, index)
    set(Math.max(top, index + slice.length - 1))
    this
  }

  /** Replaces current values in the range [index, index + replaceLength)
    * with values of the array range [sourceIndex, sourceIndex + replaceLength).
    * @group Replace */
  final override def replaceFromArray(
    index: Int,
    sourceIndex: Int,
    replaceLength: Int,
    sourceArray: Array[T]
  ): this.type = {
    if (index < 0 || sourceIndex < 0) throw new IndexOutOfBoundsException
    val copyLength = Math.min(replaceLength, sourceArray.length)
    if (copyLength > 0) {
      ensureIndex(index + replaceLength)
      copyFrom(sourceArray, sourceIndex, index, copyLength)
    }
    set(Math.max(top, index + copyLength - 1))
    this
  }

  /** Replaces current values in the range [index, index + slice.length) with values of the slice.
    * @group Replace */
  final override def replaceFromSlice(index: Int, slice: Slice[T]): this.type = {
    if (index < 0) throw new IndexOutOfBoundsException
    ensureIndex(index + slice.length)
    copyFrom(slice, index)
    set(Math.max(top, index + slice.length - 1))
    this
  }

  /** Moves values [index, length) right to [index+distance, length + distance).
    * Effectively creates a new range [index, index+distance).
    * Ignores negative distance.
    * Does not clear existing values inside [index, index+distance).
    * Moves topIndex if affected.
    * @group Shift */
  final override def shiftRight(index: Int, distance: Int): this.type = {
    if (distance > 0 && index >= 0) {
      ensureIndex(Math.max(length, index) + distance)
      if (length - index > 0) {
        copyFromSelf(index, index + distance, length - index)
      }
      if (top == -1) set(distance - 1)
      else if (top >= index) {
        set(top + distance)
      }
    }
    this
  }

  /** Moves values [index, length) left to [index-distance, length - distance).
    * Effectively removes range (index-distance, index].
    * Ignores negative distance.
    * Moves topIndex if affected.
    * @group Shift */
  final override def shiftLeft(index: Int, distance: Int): this.type = {
    if (distance > 0 && index >= 0) {
      val distance2 = Math.min(index, distance)
      val offset = distance - distance2
      if (length - index - offset > 0) {
        copyFromSelf(index + offset, index - distance2, length - index - offset)
      }
      if (top >= index - distance2) {
        set(Math.max(-1, top - distance))
      }
    }
    this
  }

  /** Moves values in [fromIndex,toIndex) to the right at a distance,
    * to become [fromIndex + distance, toIndex + distance),
    * and moves left any existing values in [toIndex, toIndex + distance)
    * to become [fromIndex, fromIndex + distance).
    * Ignores negative distance and values outside of [0,length).
    * Moves topIndex if affected.
    * @group Move
    * */
  final override def moveRangeRight(fromIndex: Int, toIndex: Int, distance: Int): this.type = {
    if (distance > 0 && fromIndex >= 0 && toIndex > fromIndex && fromIndex < length) {
      val to = Math.min(toIndex, length)
      ensureIndex(to + distance - 1)
      val backup = emptyArray(distance)
      this.slice(to, Math.min(to + distance, length)).copyToArray(0, backup)
      copyFromSelf(fromIndex, fromIndex + distance, to - fromIndex)
      copyFrom(backup, 0, fromIndex, backup.length)
      set(Math.max(top, to + distance - 1))
    }
    this
  }

  /** Moves values in [fromIndex,toIndex) to the left at a distance,
    * to become [fromIndex - distance, toIndex - distance),
    * and moves right any existing values in [fromIndex - distance, fromIndex)
    * to become [toIndex - distance, toIndex).
    *  Shifts right first if distance > fromIndex.
    * Ignores negative distance and values outside of [0,length).
    * Moves topIndex if affected.
    * @group Move
    * */
  final override def moveRangeLeft(fromIndex: Int, toIndex: Int, distance: Int): this.type = {
    if (distance > 0 && fromIndex >= 0 && toIndex > fromIndex && fromIndex < length) {
      val from = Math.max(fromIndex, distance)
      val to = Math.min(toIndex, length)
      val backup = emptyArray(distance)
      val relocating = this.slice(from - distance, fromIndex)
      relocating.copyToArray(backup.length - relocating.length, backup)
      val gap = Math.max(0, distance - fromIndex)
      shiftRight(0, gap)
      copyFromSelf(fromIndex + gap, fromIndex - distance + gap, to - fromIndex)
      copyFrom(backup, 0, to - distance + gap, backup.length)
    }
    this
  }

  /** Swap values in range  [first, first + swapLength) with values in range [second, second + swapLength]
    * - Does nothing if any index falls outside [0,length), or if indexes are equal.
    * - if [first, first + swapLength) overlaps with [second, second + swapLength)
    *   then the later overwrites the former.
    * @group Swap
    */
  final override def swapRange(first: Int, second: Int, swapLength: Int): this.type = {
    if (swapLength > 0 && first >= 0 && second >= 0 && first != second && first < length && second < length
        && first + swapLength >= 0 && second + swapLength >= 0) {
      val backupLength = Math.min(swapLength, length - Math.max(first, second))
      val backup = emptyArray(backupLength)
      slice(second, second + backupLength).copyToArray(0, backup)
      copyFromSelf(first, second, backupLength)
      copyFrom(backup, 0, first, backupLength)
    }
    this
  }

  /** Attempts to optimize buffer storage, if needed. */
  final override def optimize(): this.type = copy

}
