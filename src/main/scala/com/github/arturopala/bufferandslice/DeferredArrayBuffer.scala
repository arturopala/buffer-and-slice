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

/** Growable, mutable array of values with deferred initialization.
  *
  * @tparam T type of the underlying array items */
final class DeferredArrayBuffer[T](initialSize: Int) extends ArrayBufferLike[T] {

  private var pristine: Boolean = true
  private var initializeWithSize: Int = initialSize

  private var _array: Array[T] = _

  private def initializeWith(value: T): Unit =
    if (pristine) {
      _array = ArrayOps.newArray(value, initializeWithSize)
      pristine = false
    }

  @`inline` override protected def uncheckedApply(index: Int): T =
    if (pristine) throw new RuntimeException("Deferred buffer not yet initialized.")
    else _array(index)

  @`inline` override protected def uncheckedUpdate(index: Int, value: T): Unit = {
    initializeWith(value)
    _array.update(index, value)
  }

  @`inline` override protected def copyFrom(
    sourceArray: Array[T],
    sourceIndex: Int,
    targetIndex: Int,
    copyLength: Int
  ): Unit = {
    if (pristine) {
      if (sourceArray != null) {
        initializeWith(sourceArray(sourceIndex))
      }
    }
    if (sourceArray != null) java.lang.System.arraycopy(sourceArray, sourceIndex, _array, targetIndex, copyLength)
    else ()
  }

  @`inline` override protected def copyFrom(slice: Slice[T], targetIndex: Int): Unit =
    if (slice.nonEmpty) {
      initializeWith(slice.head)
      slice.copyToArray(targetIndex, _array)
    } else ()

  @`inline` override protected def copyFromSelf(sourceIndex: Int, targetIndex: Int, copyLength: Int): Unit =
    if (pristine) ()
    else java.lang.System.arraycopy(_array, sourceIndex, _array, targetIndex, copyLength)

  @`inline` override protected def emptyArray(length: Int): Array[T] =
    if (pristine) null
    else ArrayOps.copyOf(ArrayOps.copyOf(_array, 0), length)

  /** Returns value at the given index.
    * @throws IndexOutOfBoundsException if index out of range [0, length). */
  override def apply(index: Int): T =
    if (index < 0 || index >= length) throw new IndexOutOfBoundsException
    else _array(index)

  /** Ensures index is within buffer range. */
  override protected def ensureIndex(index: Int): Unit =
    if (pristine) {
      initializeWithSize = Math.max(index + 1, initializeWithSize)
    } else if (index >= _array.length) {
      val upswing = Math.min(_array.length, 1024 * 1024)
      _array = ArrayOps.copyOf(_array, Math.max(_array.length + upswing, index + 1))
    }

  /** Returns copy of this buffer. */
  override def copy: this.type =
    if (pristine) this
    else new DeferredArrayBuffer(length).appendArray(_array).asInstanceOf[this.type]

  /** Returns an empty copy of this buffer type. */
  override def emptyCopy: this.type =
    if (pristine) new DeferredArrayBuffer(0).asInstanceOf[this.type]
    else new DeferredArrayBuffer(length).asInstanceOf[this.type]

  /** Returns a trimmed copy of an underlying array. */
  override def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    if (!pristine) {
      java.lang.System.arraycopy(_array, 0, newArray, 0, length)
    }
    newArray
  }

  /** Returns an array with a copy of an accessible buffer range. */
  override def asArray: Array[T] =
    if (pristine) Array.empty[AnyRef].asInstanceOf[Array[T]]
    else ArrayOps.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy. */
  override def asSlice: Slice[T] =
    if (pristine) Slice.empty[AnyRef].asInstanceOf[Slice[T]]
    else new ArraySlice(0, length, _array, detached = false)

  /** Takes range and returns a Slice. */
  override def slice(from: Int, to: Int): Slice[T] =
    if (pristine) Slice.empty[Object].asInstanceOf[Slice[T]]
    else {
      val t = Math.min(length, to)
      val f = Math.min(from, t)
      new ArraySlice(f, t, _array, detached = false)
    }
}

object DeferredArrayBuffer {

  def apply[T](initialSize: Int = 0): DeferredArrayBuffer[T] = new DeferredArrayBuffer(initialSize)

}
