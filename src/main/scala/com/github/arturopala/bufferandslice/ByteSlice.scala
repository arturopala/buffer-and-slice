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

/** Lazy, specialized slice of the array of bytes. */
final class ByteSlice private (
  protected val fromIndex: Int,
  protected val toIndex: Int,
  protected val array: Array[Byte]
) extends ArraySliceLike[Byte] {

  override protected def create(fromIndex: Int, toIndex: Int, array: Array[Byte]): this.type =
    new ByteSlice(fromIndex, toIndex, array).asInstanceOf[this.type]
}

object ByteSlice {

  def apply(is: Byte*): ByteSlice = ByteSlice.of(Array(is: _*))

  def of(array: Array[Byte]): ByteSlice = new ByteSlice(0, array.length, array)

  def of(array: Array[Byte], from: Int, to: Int): ByteSlice = {
    assert(from >= 0, "When creating a ByteSlice, parameter `from` must be greater or equal to 0.")
    assert(to <= array.length, "When creating a ByteSlice, parameter `to` must be lower or equal to the array length.")
    assert(from <= to, "When creating a ByteSlice, parameter `from` must be lower or equal to `to`.")
    new ByteSlice(from, to, array)
  }

  def empty: ByteSlice = ByteSlice()

}
