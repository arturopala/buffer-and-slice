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

/** Growable, mutable array of values.
  *
  * @tparam T type of the underlying array items */
final class ArrayBuffer[T](initialArray: Array[T]) extends ArrayBufferLike[T] {

  private var _array: Array[T] = initialArray

  set(initialArray.length - 1)

  @`inline` override protected def uncheckedApply(index: Int): T =
    _array(index)

  @`inline` override protected def uncheckedUpdate(index: Int, value: T): Unit =
    _array.update(index, value)

  @`inline` override protected def copyFrom(
    sourceArray: Array[T],
    sourceIndex: Int,
    targetIndex: Int,
    copyLength: Int
  ): Unit =
    java.lang.System.arraycopy(sourceArray, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def copyFrom(slice: Slice[T], targetIndex: Int): Unit =
    slice.copyToArray(targetIndex, _array)

  @`inline` override protected def copyFromSelf(sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit =
    java.lang.System.arraycopy(_array, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def emptyArray(length: Int): Array[T] =
    ArrayOps.copyOf(ArrayOps.copyOf(_array, 0), length)

  /** Returns value at the given index.
    * @throws IndexOutOfBoundsException if index out of range [0, length). */
  override def apply(index: Int): T =
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException
    else
      _array(index)

  override protected def ensureIndex(index: Int): Unit =
    if (index >= _array.length) {
      val upswing = Math.min(_array.length, 1024 * 1024)
      _array = ArrayOps.copyOf(_array, Math.max(_array.length + upswing, index + 1))
    }

  override def copy: this.type =
    new ArrayBuffer(asArray).asInstanceOf[this.type]

  override def emptyCopy: this.type =
    new ArrayBuffer(ArrayOps.copyOf(_array, 0)).asInstanceOf[this.type]

  /** Returns a trimmed copy of an underlying array. */
  override def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(_array, 0, newArray, 0, length)
    newArray
  }

  /** Returns a trimmed copy of an underlying array. */
  override def asArray: Array[T] = ArrayOps.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  override def asSlice: Slice[T] =
    new ArraySlice(0, length, _array, detached = false)

  /** Takes range and returns an ArraySlice. */
  override def slice(from: Int, to: Int): Slice[T] = {
    val t = Math.min(length, to)
    val f = Math.min(from, t)
    new ArraySlice(f, t, _array, detached = false)
  }
}
