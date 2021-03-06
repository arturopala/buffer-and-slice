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

/** Lazy, specialized slice of the array of bytes.
  * @note Truly immutable only if an underlying array kept private, or if detached.
  */
final class ByteSlice private[bufferandslice] (
  protected val fromIndex: Int,
  protected val toIndex: Int,
  protected val array: Array[Byte],
  protected val detached: Boolean
) extends ArraySliceLike[Byte] {

  override protected def wrap(fromIndex: Int, toIndex: Int, array: Array[Byte], detached: Boolean): this.type =
    new ByteSlice(fromIndex, toIndex, array, detached).asInstanceOf[this.type]

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  @`inline` override def toBuffer[T1 >: Byte]: Buffer[T1] =
    Buffer(asArray.asInstanceOf[Array[T1]])

  /** Returns a buffer with a copy of this Slice. */
  @`inline` override def asBuffer: ByteBuffer = ByteBuffer(asArray)
}

object ByteSlice {

  /** Creates new detached ByteSlice out of given bytes. */
  def apply(head: Byte, tail: Byte*): ByteSlice = {
    val array = Array(head, tail: _*)
    new ByteSlice(0, array.length, array, detached = true)
  }

  def of(array: Array[Byte]): ByteSlice = new ByteSlice(0, array.length, array, detached = false)

  def of(array: Array[Byte], from: Int, to: Int): ByteSlice =
    new ByteSlice(Math.max(0, Math.min(from, array.length)), Math.min(Math.max(from, to), array.length), array, false)

  def empty: ByteSlice = ByteSlice.of(Array.empty[Byte])

}
