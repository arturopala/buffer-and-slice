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
  * @tparam T type of the underlying array items
  * @param initialLength initial length of the buffer
  *
  * @note This is to avoid ClassTag parameters as much as possible, but
  *       comes with a price of a few rough corners. Especially, if your type
  *       parameter is non-primitive do not try to call [[asArray]] and assign result to variable,
  *       as it will raise ClassCastException,
  *       instead pass the array as a parameter or call methods on it.
  */
final class DeferredArrayBuffer[T](initialLength: Int) extends ArrayBufferLike[T] {

  private var pristine: Boolean = true
  private var initialSize: Int = initialLength

  private var _array: Array[T] = _

  set(initialLength - 1)

  private def initializeWith(value: T): Unit =
    if (pristine) {
      _array = ArrayOps.newArray(value, initialSize)
      pristine = false
    }

  /** @throws RuntimeException if not yet initialized */
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
    * @throws IndexOutOfBoundsException if index out of range [0, length)
    * @throws RuntimeException if not yet initialized
    */
  override def apply(index: Int): T =
    if (index < 0 || index >= length) throw new IndexOutOfBoundsException
    else if (pristine) throw new RuntimeException("Deferred buffer not yet initialized.")
    else _array(index)

  /** Ensures index is within buffer range. */
  override protected def ensureIndex(index: Int): Unit =
    if (pristine) {
      initialSize = Math.max(index + 1, initialSize)
    } else if (index >= _array.length) {
      val upswing = Math.max(1, Math.min(_array.length, 1024 * 1024))
      _array = ArrayOps.copyOf(_array, Math.max(_array.length + upswing, index + 1))
    }

  /** Returns copy of this buffer. */
  override def copy: this.type =
    if (pristine) new DeferredArrayBuffer(initialLength).asInstanceOf[this.type]
    else new DeferredArrayBuffer(0).appendArray(_array).asInstanceOf[this.type]

  /** Returns an empty copy of this buffer type. */
  override def emptyCopy: this.type =
    new DeferredArrayBuffer(0).asInstanceOf[this.type]

  /** Returns a trimmed copy of an underlying array. */
  override def toArray[T1 >: T: ClassTag]: Array[T1] =
    if (pristine) {
      new Array[T1](initialSize)
    } else {
      val newArray = new Array[T1](length)
      java.lang.System.arraycopy(_array, 0, newArray, 0, length)
      newArray
    }

  /** Returns an array with a copy of an accessible buffer range.
    *
    * @note this method avoids ClassTag but at the price of:
    *       - RuntimeException if non-empty buffer not yet initialized
    *       - ClassCastExceptions when assigning returned non-primitive array to a variable.
    *       Try using [[toArray]] when possible.
    *
    * @throws RuntimeException if not yet initialized
    */
  override def asArray: Array[T] =
    if (pristine) {
      if (initialSize == 0) Array.empty[Any].asInstanceOf[Array[T]]
      else throw new RuntimeException("Deferred buffer not yet initialized. Maybe try using .toArray instead.")
    } else ArrayOps.copyOf(_array, length)

  /** Wraps accessible internal state as a Slice without making any copy.
    * @throws RuntimeException if not yet initialized
    */
  override def asSlice: Slice[T] =
    if (pristine) {
      if (initialSize == 0) Slice.empty[AnyRef].asInstanceOf[Slice[T]]
      else throw new RuntimeException("Deferred buffer not yet initialized. Maybe try using ArrayBuffer instead.")
    } else new ArraySlice(0, length, _array, detached = false)

  /** Takes range and returns a Slice.
    * @throws RuntimeException if not yet initialized
    */
  override def slice(from: Int, to: Int): Slice[T] =
    if (pristine) {
      if (initialSize == 0) Slice.empty[Any].asInstanceOf[Slice[T]]
      else throw new RuntimeException("Deferred buffer not yet initialized. Maybe try ArrayBuffer instead.")
    } else {
      val t = Math.min(length, to)
      val f = Math.min(from, t)
      new ArraySlice(f, t, _array, detached = false)
    }
}

object DeferredArrayBuffer {

  def apply[T](initialLength: Int = 0): DeferredArrayBuffer[T] = new DeferredArrayBuffer(initialLength)

}
