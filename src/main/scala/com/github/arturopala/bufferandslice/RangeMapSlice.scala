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

/** Lazily mapped slice of an index range. */
class RangeMapSlice[T] private (fromIndex: Int, toIndex: Int, mapF: Int => T) extends Slice[T] {

  /** Sliced range length. */
  final override val length: Int = toIndex - fromIndex

  /** Returns value at the given index */
  @`inline` final override protected def read(index: Int): T =
    mapF(fromIndex + index)

  /** Creates a copy of the slice with modified value. */
  final override def update[T1 >: T](index: Int, value: T1): Slice[T1] = {
    if (index < 0 || index >= length)
      throw new IndexOutOfBoundsException(s"Expected an `update` index in the interval [0,$length), but was $index.")
    Slice.of(toBuffer[T1].update(index, value).asArray)
  }

  /** Lazily composes mapping function and returns new IndexMapSlice.
    * Does not modify nor copy underlying array. */
  final override def map[K](f: T => K): RangeMapSlice[K] =
    new RangeMapSlice[K](fromIndex, toIndex, mapF.andThen(f))

  /** Lazily narrows Slice to provided range. */
  final override def slice(from: Int, to: Int): this.type = {

    def fit(value: Int, upper: Int): Int = Math.min(Math.max(0, value), upper)

    val t = fit(to, length)
    val f = fit(from, t)

    if (f == 0 && t == length) this
    else
      new RangeMapSlice[T](fromIndex + f, fromIndex + t, mapF)
        .asInstanceOf[this.type]
  }

  /** Returns an array of mapped values. */
  final override def asArray: Array[T] = toBuffer[T].asArray

  /** Does nothing as this type of Slice does not have underlying mutable data. */
  final override def detach: this.type = this

  /** Returns buffer with a copy of this Slice. */
  final override def toBuffer[T1 >: T]: Buffer[T1] =
    new DeferredArrayBuffer[T1](0).appendSlice(this.asInstanceOf[Slice[T1]])

  /** Returns a buffer with a copy of this Slice. */
  final override def asBuffer: Buffer[T] = toBuffer[T]

}

object RangeMapSlice {

  def apply[T](mapF: Int => T): RangeMapSlice[T] = new RangeMapSlice[T](0, Int.MaxValue, mapF)

  def apply[T](mapF: Int => T, from: Int, to: Int): RangeMapSlice[T] =
    new RangeMapSlice[T](Math.max(0, from), Math.max(from, to), mapF)

}
