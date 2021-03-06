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

/** Lazy, specialized slice of the array of integers.
  * @note Truly immutable only if an underlying array kept private, or if detached.
  */
final class IntSlice private[bufferandslice] (
  protected val fromIndex: Int,
  protected val toIndex: Int,
  protected val array: Array[Int],
  protected val detached: Boolean
) extends ArraySliceLike[Int] {

  override protected def wrap(fromIndex: Int, toIndex: Int, array: Array[Int], detached: Boolean): this.type =
    new IntSlice(fromIndex, toIndex, array, detached).asInstanceOf[this.type]

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  @`inline` override def toBuffer[T1 >: Int]: Buffer[T1] =
    Buffer(asArray.asInstanceOf[Array[T1]])

  /** Returns a buffer with a copy of this Slice. */
  @`inline` override def asBuffer: IntBuffer = IntBuffer(asArray)

  /** Sums all integers.
    * @group Aggregate */
  def sum: Int =
    if (isEmpty) 0
    else {
      var acc: Int = head
      var i = fromIndex + 1
      while (i < toIndex) {
        acc = acc + array(i)
        i = i + 1
      }
      acc
    }

  /** Max of all integers.
    * @group Aggregate */
  def max: Int =
    if (isEmpty) throw new UnsupportedOperationException
    else {
      var acc: Int = head
      var i = fromIndex + 1
      while (i < toIndex) {
        acc = Math.max(acc, array(i))
        i = i + 1
      }
      acc
    }

  /** Min of all integers.
    * @group Aggregate */
  def min: Int =
    if (isEmpty) throw new UnsupportedOperationException
    else {
      var acc: Int = head
      var i = fromIndex + 1
      while (i < toIndex) {
        acc = Math.min(acc, array(i))
        i = i + 1
      }
      acc
    }
}

object IntSlice {

  /** Creates new detached IntSlice out of given integers. */
  def apply(head: Int, tail: Int*): IntSlice = {
    val array = Array(head, tail: _*)
    new IntSlice(0, array.length, array, detached = true)
  }

  def of(array: Array[Int]): IntSlice = new IntSlice(0, array.length, array, detached = false)

  def of(array: Array[Int], from: Int, to: Int): IntSlice =
    new IntSlice(Math.max(0, Math.min(from, array.length)), Math.min(Math.max(from, to), array.length), array, false)

  def empty: IntSlice = IntSlice.of(Array.empty[Int])

}
