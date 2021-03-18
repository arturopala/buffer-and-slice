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

/** Lazily mapped slice of an underlying array.
  * @note Truly immutable only if an underlying array kept private, or if detached.
  * @tparam T type of the array's items
  */
abstract class LazyMapArraySlice[T] private (fromIndex: Int, toIndex: Int, detached: Boolean) extends Slice[T] {

  /** Type of the underlying array items. */
  type A

  /** Underlying array. */
  val array: Array[A]

  /** Value mapping function. */
  val mapF: A => T

  /** Sliced range length. */
  final override val length: Int = toIndex - fromIndex

  /** Returns value at the given index */
  @`inline` final override protected def read(index: Int): T =
    mapF(array(fromIndex + index))

  /** Creates a copy of the slice with modified value. */
  final override def update[T1 >: T](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    Slice.of(toBuffer[T1].update(index, value).asArray)
  }

  /** Lazily composes mapping function and returns new Slice.
    * Does not modify nor copy underlying array. */
  final override def map[K](f: T => K): LazyMapArraySlice[K] =
    LazyMapArraySlice.lazilyMapped[K, A](fromIndex, toIndex, array, mapF.andThen(f), detached)

  /** Lazily narrows Slice to provided range. */
  final override def slice(from: Int, to: Int): this.type = {

    def fit(value: Int, upper: Int): Int = Math.min(Math.max(0, value), upper)

    val t = fit(to, length)
    val f = fit(from, t)
    if (f == 0 && t == length) this
    else
      LazyMapArraySlice
        .lazilyMapped[T, A](fromIndex + f, fromIndex + t, array, mapF, detached)
        .asInstanceOf[this.type]
  }

  /** Returns an array of mapped values. */
  final override def asArray: Array[T] =
    ArrayOps.copyMapOf(fromIndex, toIndex, array, mapF)

  /** Detaches a slice creating a trimmed copy of an underlying data. */
  final override def detach: this.type =
    if (detached) this
    else {
      val newArray = ArrayOps.copyOf(array, length)
      java.lang.System.arraycopy(array, fromIndex, newArray, 0, length)
      LazyMapArraySlice
        .lazilyMapped[T, A](0, length, newArray, mapF, detached = true)
        .asInstanceOf[this.type]
    }

  /** Returns buffer with a copy of this Slice. */
  final override def toBuffer[T1 >: T]: Buffer[T1] =
    new DeferredArrayBuffer[T1](0).appendSlice(this.asInstanceOf[Slice[T1]])

  /** Returns a buffer with a copy of this Slice. */
  final override def asBuffer: Buffer[T] = Buffer(asArray)

}

object LazyMapArraySlice {

  private[bufferandslice] def lazilyMapped[T, K](
    fromIndex: Int,
    toIndex: Int,
    _array: Array[K],
    _mapF: K => T,
    detached: Boolean
  ): LazyMapArraySlice[T] =
    new LazyMapArraySlice[T](fromIndex, toIndex, detached) {
      type A = K
      val array: Array[A] = _array
      val mapF: A => T = _mapF
    }

}
