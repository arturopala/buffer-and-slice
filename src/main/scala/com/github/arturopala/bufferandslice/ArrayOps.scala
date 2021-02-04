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

/** Array modifications helper. */
object ArrayOps {

  /** Makes a copy of an array with new length. */
  final def copyOf[K](array: Array[K], newLength: Int): Array[K] =
    (array match {
      case x if x.isInstanceOf[Array[AnyRef]]  => java.util.Arrays.copyOf(x.asInstanceOf[Array[AnyRef]], newLength)
      case x if x.isInstanceOf[Array[Int]]     => java.util.Arrays.copyOf(x.asInstanceOf[Array[Int]], newLength)
      case x if x.isInstanceOf[Array[Long]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Long]], newLength)
      case x if x.isInstanceOf[Array[Double]]  => java.util.Arrays.copyOf(x.asInstanceOf[Array[Double]], newLength)
      case x if x.isInstanceOf[Array[Long]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Long]], newLength)
      case x if x.isInstanceOf[Array[Float]]   => java.util.Arrays.copyOf(x.asInstanceOf[Array[Float]], newLength)
      case x if x.isInstanceOf[Array[Char]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Char]], newLength)
      case x if x.isInstanceOf[Array[Byte]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Byte]], newLength)
      case x if x.isInstanceOf[Array[Short]]   => java.util.Arrays.copyOf(x.asInstanceOf[Array[Short]], newLength)
      case x if x.isInstanceOf[Array[Boolean]] => java.util.Arrays.copyOf(x.asInstanceOf[Array[Boolean]], newLength)
    }).asInstanceOf[Array[K]]

  /** Makes a copy of a portions of an array with elements mapped.
    * Does not require ClassTag instance. */
  final def copyMapOf[K, T](from: Int, to: Int, array: Array[K], map: K => T): Array[T] = {
    val fromIndex = Math.max(0, from)
    val toIndex = Math.min(array.length, Math.max(from, to))
    val length = Math.max(0, toIndex - fromIndex)
    val array2: Array[T] =
      if (array.length > 0) newArray(map(array(fromIndex)), length)
      else Array.empty[Any].asInstanceOf[Array[T]]

    var i = 0
    while (i < length) {
      array2(i) = map(array(fromIndex + i))
      i = i + 1
    }
    array2
  }

  /** Creates a new array based on the type of the example item provided.
    * Does not require ClassTag instance. */
  final def newArray[T](exampleItem: T, length: Int): Array[T] = {
    exampleItem match {
      case _: String     => new Array[String](length)
      case _: Int        => new Array[Int](length)
      case _: Byte       => new Array[Byte](length)
      case _: Double     => new Array[Double](length)
      case _: Float      => new Array[Float](length)
      case _: Char       => new Array[Char](length)
      case _: Boolean    => new Array[Boolean](length)
      case _: Long       => new Array[Long](length)
      case _: Short      => new Array[Short](length)
      case _: BigDecimal => new Array[BigDecimal](length)
      case _             => new Array[AnyRef](length)
    }
  }.asInstanceOf[Array[T]]
}
