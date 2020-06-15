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

/** Growable, mutable array of integers. */
final class IntBuffer(initialSize: Int = 8) extends ArrayBufferLike[Int] {

  private var _array = new Array[Int](initialSize)

  @`inline` override protected def uncheckedApply(index: Int): Int =
    _array(index)

  @`inline` override protected def uncheckedUpdate(index: Int, value: Int): Unit =
    _array.update(index, value)

  @`inline` override protected def copyFrom(
    sourceArray: Array[Int],
    sourceIndex: Int,
    targetIndex: Int,
    copyLength: Int
  ): Unit =
    java.lang.System.arraycopy(sourceArray, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def copyFrom(slice: Slice[Int], targetIndex: Int): Unit =
    slice.copyToArray(targetIndex, _array)

  @`inline` override protected def copyFromSelf(sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit =
    java.lang.System.arraycopy(_array, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def emptyArray(length: Int): Array[Int] = new Array[Int](length)

  /** Returns value at the given index or 0 if out of scope. */
  override def apply(index: Int): Int =
    if (index < 0 || index >= _array.length) 0
    else _array(index)

  override protected def ensureIndex(index: Int): Unit =
    if (index >= _array.length) {
      val upswing = Math.max(1, Math.min(_array.length, 1024 * 1024))
      val newArray: Array[Int] = new Array(Math.max(_array.length + upswing, index + 1))
      java.lang.System.arraycopy(_array, 0, newArray, 0, _array.length)
      _array = newArray
    }

  /** Increments the value at an index */
  def increment(index: Int): this.type = {
    update(index, apply(index) + 1)
    this
  }

  /** Decrements the value at an index */
  def decrement(index: Int): this.type = {
    update(index, apply(index) - 1)
    this
  }

  override def copy: this.type =
    new IntBuffer(length).appendArray(asArray).asInstanceOf[this.type]

  override def emptyCopy: this.type =
    new IntBuffer(0).asInstanceOf[this.type]

  /** Returns a trimmed copy of an underlying array. */
  override def toArray[T1 >: Int: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(_array, 0, newArray, 0, length)
    newArray
  }

  /** Returns an Array with a copy of an accessible buffer range. */
  override def asArray: Array[Int] = java.util.Arrays.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  override def asSlice: IntSlice =
    new IntSlice(0, length, _array, detached = false)

  /** Takes range and returns an IntSlice. */
  override def slice(from: Int, to: Int): IntSlice = {
    val t = Math.min(length, to)
    val f = Math.min(from, t)
    new IntSlice(f, t, _array, detached = false)
  }

}

/** IntBuffer factory. */
object IntBuffer {

  /** Create buffer with initial values. */
  def apply(elems: Int*): IntBuffer = new IntBuffer(elems.size).appendArray(elems.toArray)

  /** Create buffer from an array copy. */
  def apply(array: Array[Int]): IntBuffer = new IntBuffer(array.length).appendArray(array)

  /** An empty buffer. */
  def empty = new IntBuffer(8)
}
