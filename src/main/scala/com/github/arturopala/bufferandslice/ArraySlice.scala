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
  @`inline` override def toBuffer[T1 >: T]: Buffer[T1] =
    new DeferredArrayBuffer[T1](0).appendSlice(this.asInstanceOf[Slice[T1]])

  /** Returns a buffer with a copy of this Slice. */
  @`inline` override def asBuffer: Buffer[T] =
    if (array.isEmpty) new DeferredArrayBuffer[T](0)
    else new ArrayBuffer[T](asArray)
}

object ArraySlice {

  /** Creates new detached ArraySlice out of given value sequence. */
  def apply[T](head: T, tail: T*): ArraySlice[T] = {
    val array = ArrayOps.newArray(head, tail.length + 1)
    array(0) = head
    var i = 0
    while (i < tail.length) {
      array(i + 1) = tail(i)
      i = i + 1
    }
    new ArraySlice(0, array.length, array, true)
  }

  def of[T](array: Array[T]): ArraySlice[T] = new ArraySlice(0, array.length, array, false)

  def of[T](array: Array[T], from: Int, to: Int): ArraySlice[T] =
    new ArraySlice(Math.max(0, Math.min(from, array.length)), Math.min(Math.max(from, to), array.length), array, false)

  def empty[T]: ArraySlice[T] = ArraySlice.of(Array.empty[AnyRef].asInstanceOf[Array[T]])

}
