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

/** Growable, mutable array of bytes. */
final class ByteBuffer(initialSize: Int = 8) extends ArrayBufferLike[Byte] {

  private var _array = new Array[Byte](initialSize)

  /** Very unsafe access to the underlying array, if you really need it.
    * @group Unsafe */
  @`inline` override def underlyingUnsafe: Array[Byte] = _array

  /** Returns value at the given index or 0 if out of scope. */
  @`inline` def apply(index: Int): Byte =
    if (index < 0 || index >= _array.length) 0
    else _array(index)

  override protected def ensureIndex(index: Int): Unit =
    if (index >= _array.length) {
      val upswing = Math.min(_array.length, 1024 * 1024)
      val newArray: Array[Byte] = new Array(Math.max(_array.length + upswing, index + 1))
      java.lang.System.arraycopy(_array, 0, newArray, 0, _array.length)
      _array = newArray
    }

  /** Returns an Array with a copy of an accessible buffer range. */
  def toArray: Array[Byte] = java.util.Arrays.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  def asSlice: ByteSlice = ByteSlice.of(_array, 0, length)

}

/** ByteBuffer factory. */
object ByteBuffer {

  /** Create buffer with initial values. */
  def apply(elems: Int*): ByteBuffer =
    new ByteBuffer(elems.size).appendArray(elems.toArray.map(_.toByte))

  /** Create buffer from an array copy. */
  def apply(array: Array[Byte]): ByteBuffer = new ByteBuffer(array.length).appendArray(array)

  /** An empty buffer. */
  def empty = new ByteBuffer(0)
}
