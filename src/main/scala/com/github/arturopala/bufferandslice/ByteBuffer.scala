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

/** Growable, mutable array of bytes. */
final class ByteBuffer(initialSize: Int = 8) extends ArrayBufferLike[Byte] {

  private var _array = new Array[Byte](initialSize)

  @`inline` override protected def uncheckedApply(index: Int): Byte =
    _array(index)

  @`inline` override protected def uncheckedUpdate(index: Int, value: Byte): Unit =
    _array.update(index, value)

  @`inline` override protected def copyFrom(
    sourceArray: Array[Byte],
    sourceIndex: Int,
    targetIndex: Int,
    copyLength: Int
  ): Unit =
    java.lang.System.arraycopy(sourceArray, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def copyFrom(slice: Slice[Byte], targetIndex: Int): Unit =
    slice.copyToArray(targetIndex, _array)

  @`inline` override protected def copyFromSelf(sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit =
    java.lang.System.arraycopy(_array, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def emptyArray(length: Int): Array[Byte] = new Array[Byte](length)

  /** Returns value at the given index or 0 if out of scope. */
  override def apply(index: Int): Byte =
    if (index < 0 || index >= _array.length) 0
    else _array(index)

  override protected def ensureIndex(index: Int): Unit =
    if (index >= _array.length) {
      val upswing = Math.max(1, Math.min(_array.length, 1024 * 1024))
      val newArray: Array[Byte] = new Array(Math.max(_array.length + upswing, index + 1))
      java.lang.System.arraycopy(_array, 0, newArray, 0, _array.length)
      _array = newArray
    }

  override def copy: this.type =
    new ByteBuffer(length).appendArray(asArray).asInstanceOf[this.type]

  override def emptyCopy: this.type =
    new ByteBuffer(0).asInstanceOf[this.type]

  /** Returns a trimmed copy of an underlying array. */
  override def toArray[T1 >: Byte: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(_array, 0, newArray, 0, length)
    newArray
  }

  /** Returns an Array with a copy of an accessible buffer range. */
  override def asArray: Array[Byte] = java.util.Arrays.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  override def asSlice: ByteSlice =
    new ByteSlice(0, length, _array, detached = false)

  /** Takes range and returns an IntSlice. */
  override def slice(from: Int, to: Int): ByteSlice = {
    val t = Math.min(length, to)
    val f = Math.min(from, t)
    new ByteSlice(f, t, _array, detached = false)
  }
}

/** ByteBuffer factory. */
object ByteBuffer {

  /** Create buffer with initial values. */
  def apply(elems: Int*): ByteBuffer =
    new ByteBuffer(elems.size).appendArray(elems.toArray.map(_.toByte))

  /** Create buffer from an array copy. */
  def apply(array: Array[Byte]): ByteBuffer = new ByteBuffer(array.length).appendArray(array)

  /** An empty buffer. */
  def empty = new ByteBuffer(8)
}
