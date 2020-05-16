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
  * @note Truly immutable only if an underlying array kept private or if detached.
  * @tparam T type of the array's items
  */
final class ArraySlice[T] private[bufferandslice] (
  protected val fromIndex: Int,
  protected val toIndex: Int,
  protected val array: Array[T],
  protected val detached: Boolean
) extends ArraySliceLike[T] {

  override protected def wrap(fromIndex: Int, toIndex: Int, array: Array[T], detached: Boolean): this.type =
    new ArraySlice[T](fromIndex, toIndex, array, detached).asInstanceOf[this.type]

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  @`inline` override def toBuffer(implicit tag: ClassTag[T]): ArrayBuffer[T] =
    new ArrayBuffer(toArray)
}

object ArraySlice {

  /** Creates new detached ArraySlice out of given value sequence. */
  def apply[T: ClassTag](is: T*): ArraySlice[T] = {
    val array = Array(is: _*)
    new ArraySlice(0, array.length, array, true)
  }

  def of[T](array: Array[T]): ArraySlice[T] = new ArraySlice(0, array.length, array, false)

  def of[T](array: Array[T], from: Int, to: Int): ArraySlice[T] = {
    assert(from >= 0, "When creating an ArraySlice, parameter `from` must be greater or equal to 0.")
    assert(
      to <= array.length,
      "When creating an ArraySlice, parameter `to` must be lower or equal to the array length."
    )
    assert(from <= to, "When creating an ArraySlice, parameter `from` must be lower or equal to `to`.")
    new ArraySlice(from, to, array, false)
  }

  def empty[T: ClassTag]: ArraySlice[T] = ArraySlice()

}
