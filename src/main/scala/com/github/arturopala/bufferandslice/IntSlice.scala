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

/** Lazy, specialized slice of the array of integers. */
final class IntSlice private (protected val fromIndex: Int, protected val toIndex: Int, protected val array: Array[Int])
    extends ArraySliceLike[Int] {

  override protected def create(fromIndex: Int, toIndex: Int, array: Array[Int]): this.type =
    new IntSlice(fromIndex, toIndex, array).asInstanceOf[this.type]

  /** Returns buffer with a copy of this Slice.
    * @group Read */
  @`inline` override def toBuffer(implicit tag: ClassTag[Int]): IntBuffer =
    new IntBuffer(length).appendArray(toArray)
}

object IntSlice {

  def apply(is: Int*): IntSlice = IntSlice.of(Array(is: _*))

  def of(array: Array[Int]): IntSlice = new IntSlice(0, array.length, array)

  def of(array: Array[Int], from: Int, to: Int): IntSlice = {
    assert(from >= 0, "When creating a IntSlice, parameter `from` must be greater or equal to 0.")
    assert(to <= array.length, "When creating a IntSlice, parameter `to` must be lower or equal to the array length.")
    assert(from <= to, "When creating a IntSlice, parameter `from` must be lower or equal to `to`.")
    new IntSlice(from, to, array)
  }

  def empty: IntSlice = IntSlice()

}
