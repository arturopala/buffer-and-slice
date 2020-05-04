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

/** Lazy, immutable slice of an underlying array.
  *
  * @tparam T type of the array's items */
final class ArraySlice[T] private (
  protected val fromIndex: Int,
  protected val toIndex: Int,
  protected val array: Array[T]
) extends ArraySliceLike[T] {

  override protected def create(fromIndex: Int, toIndex: Int, array: Array[T]): this.type =
    new ArraySlice[T](fromIndex, toIndex, array).asInstanceOf[this.type]
}

object ArraySlice {

  def apply[T: ClassTag](is: T*): ArraySlice[T] = ArraySlice.of(Array(is: _*))

  def of[T](array: Array[T]): ArraySlice[T] = new ArraySlice(0, array.length, array)

  def of[T](array: Array[T], from: Int, to: Int): ArraySlice[T] = {
    assert(from >= 0, "When creating an ArraySlice, parameter `from` must be greater or equal to 0.")
    assert(
      to <= array.length,
      "When creating an ArraySlice, parameter `to` must be lower or equal to the array length."
    )
    assert(from <= to, "When creating an ArraySlice, parameter `from` must be lower or equal to `to`.")
    new ArraySlice(from, to, array)
  }

  def empty[T: ClassTag]: ArraySlice[T] = ArraySlice()

}
