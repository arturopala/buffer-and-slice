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

/** Common functions of an array-backed Slice.
  * @note Truly immutable only if an underlying array kept private, or if detached.
  * @tparam T type of the array's items
  */
trait ArraySliceLike[T] extends Slice[T] {

  /** @group Internal */
  protected def fromIndex: Int

  /** @group Internal */
  protected def toIndex: Int

  /** @group Internal */
  protected def array: Array[T]

  /** @group Internal */
  protected def detached: Boolean

  /** Wraps an array preserving current Slice type.
    * @group Internal */
  protected def wrap(fromIndex: Int, toIndex: Int, array: Array[T], detached: Boolean): this.type

  /** Sliced range length. */
  @`inline` final override val length: Int =
    toIndex - fromIndex

  /** Returns value at the given index */
  @`inline` final override protected def read(index: Int): T =
    array(fromIndex + index)

  /** Creates a copy of the slice with modified value. */
  final override def update[T1 >: T](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    Slice.of(toBuffer[T1].update(index, value).asArray)
  }

  /** Lazily composes mapping function and returns new [[LazyMapArraySlice]].
    * Does not modify nor copy underlying array. */
  @`inline` final override def map[K](f: T => K): Slice[K] =
    LazyMapArraySlice.lazilyMapped[K, T](fromIndex, toIndex, array, f, detached)

  /** Lazily narrows Slice to provided range. */
  final override def slice(from: Int, to: Int): this.type = {
    val t = fit(to, length)
    val f = fit(from, t)
    if (f == 0 && t == length) this
    else wrap(fromIndex + f, fromIndex + t, array, detached)
  }

  @`inline` private def fit(value: Int, upper: Int): Int =
    Math.min(Math.max(0, value), upper)

  /** Returns a minimal copy of an underlying array, trimmed to the actual range. */
  final override def toArray[T1 >: T: ClassTag]: Array[T1] = {
    val newArray = new Array[T1](length)
    java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
    newArray
  }

  /** Returns a trimmed copy of an underlying array. */
  final override def asArray: Array[T] = {
    val newArray = ArrayOps.copyOf(array, length)
    if (fromIndex > 0) {
      java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
    }
    newArray
  }

  /** Dumps content to the array, starting from an index. */
  @`inline` final override def copyToArray[T1 >: T](targetIndex: Int, targetArray: Array[T1]): Array[T1] = {
    java.lang.System.arraycopy(array, fromIndex, targetArray, targetIndex, length)
    targetArray
  }

  /** Detaches a slice creating a trimmed copy of an underlying data, if needed.
    * Subsequent detach operations will return the same instance without making new copies. */
  @`inline` final override def detach: this.type =
    if (detached) this else wrap(0, length, asArray, detached = true)

}
