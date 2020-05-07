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

/** Growable, mutable array of values.
  * @tparam T type of the underlying array items */
final class ArrayBuffer[T](initialArray: Array[T]) extends ArrayBufferLike[T] {

  private var _array: Array[T] = initialArray

  /** Very unsafe access to the underlying array, if you really need it.
    * @group Unsafe */
  @`inline` override def underlyingUnsafe: Array[T] = _array

  set(initialArray.length - 1)

  /** Returns value at the given index.
    * @throws IndexOutOfBoundsException if index out of range [0, length). */
  def apply(index: Int): T =
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException
    else
      _array(index)

  override protected def ensureIndex(index: Int): Unit =
    if (index >= _array.length) {
      val upswing = Math.min(_array.length, 1024 * 1024)
      _array = ArrayOps.copyOf(_array, Math.max(_array.length + upswing, index + 1))
    }

  /** Returns an Array with a copy of an accessible buffer range. */
  def toArray: Array[T] = ArrayOps.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  def asSlice: Slice[T] = ArraySlice.of(_array, 0, length)
}
