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

  /** Very unsafe access to the underlying array, if you really need it.
    * @group Unsafe */
  def underlyingUnsafe: Array[T]

  /** Updates value at the provided index.
    * Alters underlying array if necessary.
    * @throws IndexOutOfBoundsException if index lower than zero.
    * @group Update */
  final def update(index: Int, value: T): this.type =
    if (index < 0) throw new IndexOutOfBoundsException
    else {
      ensureIndex(index)
      underlyingUnsafe.update(index, value)
      set(Math.max(index, top))
      this
    }

  final override def toString: String =
    underlyingUnsafe.take(Math.min(20, length)).mkString("[", ",", if (length > 20) ", ... ]" else "]")

  /** Shift current content to the right starting from `index`at the `insertLength` distance,
    * and copies array chunk into the gap.
    * Sets topIndex to be at least at the end of the new chunk of values.
    * @group Insert */
  final override def insertArray(index: Int, sourceIndex: Int, insertLength: Int, sourceArray: Array[T]): this.type = {
    if (index < 0 || sourceIndex < 0) throw new IndexOutOfBoundsException
    val copyLength = Math.min(insertLength, sourceArray.length)
    if (copyLength > 0) {
      shiftRight(index, copyLength)
      java.lang.System.arraycopy(sourceArray, sourceIndex, underlyingUnsafe, index, copyLength)
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
    slice.copyToArray(index, underlyingUnsafe)
    set(Math.max(top, index + slice.length - 1))

    this
  }

  /** Replaces current values in the range [index, index + replaceLength)
    * with values of the array range [sourceIndex, sourceIndex + replaceLength).
    * @group Replace */
  final def replaceFromArray(index: Int, sourceIndex: Int, replaceLength: Int, sourceArray: Array[T]): this.type = {
    if (index < 0 || sourceIndex < 0) throw new IndexOutOfBoundsException
    val copyLength = Math.min(replaceLength, sourceArray.length)
    if (copyLength > 0) {
      ensureIndex(index + replaceLength)
      java.lang.System.arraycopy(sourceArray, sourceIndex, underlyingUnsafe, index, copyLength)
    }
    set(Math.max(top, index + copyLength - 1))

    this
  }

  /** Replaces current values in the range [index, index + slice.length) with values of the slice.
    * @group Replace */
  final def replaceFromSlice(index: Int, slice: Slice[T]): this.type = {
    if (index < 0) throw new IndexOutOfBoundsException
    ensureIndex(index + slice.length)
    slice.copyToArray(index, underlyingUnsafe)
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
        java.lang.System.arraycopy(underlyingUnsafe, index, underlyingUnsafe, index + distance, length - index)
      }
      if (top >= index) {
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
  final def shiftLeft(index: Int, distance: Int): this.type = {
    if (distance > 0 && index >= 0) {
      val distance2 = Math.min(index, distance)
      val offset = distance - distance2
      if (length - index - offset > 0) {
        java.lang.System
          .arraycopy(underlyingUnsafe, index + offset, underlyingUnsafe, index - distance2, length - index - offset)
      }
      if (top >= index - distance2) {
        set(Math.max(-1, top - distance))
      }
    }
    this
  }

}
