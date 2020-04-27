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

import scala.runtime.BoxedUnit

/** Array modifications helper. */
object ArrayOps {

  /** Makes a copy of an array with new length. */
  final def copyOf[K](array: Array[K], newLength: Int): Array[K] =
    (array match {
      case x if x.isInstanceOf[Array[BoxedUnit]] =>
        val result = new Array[Unit](newLength)
        java.util.Arrays.fill(result.asInstanceOf[Array[AnyRef]], ())
        result

      case x if x.isInstanceOf[Array[AnyRef]]  => java.util.Arrays.copyOf(x.asInstanceOf[Array[AnyRef]], newLength)
      case x if x.isInstanceOf[Array[Int]]     => java.util.Arrays.copyOf(x.asInstanceOf[Array[Int]], newLength)
      case x if x.isInstanceOf[Array[Double]]  => java.util.Arrays.copyOf(x.asInstanceOf[Array[Double]], newLength)
      case x if x.isInstanceOf[Array[Long]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Long]], newLength)
      case x if x.isInstanceOf[Array[Float]]   => java.util.Arrays.copyOf(x.asInstanceOf[Array[Float]], newLength)
      case x if x.isInstanceOf[Array[Char]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Char]], newLength)
      case x if x.isInstanceOf[Array[Byte]]    => java.util.Arrays.copyOf(x.asInstanceOf[Array[Byte]], newLength)
      case x if x.isInstanceOf[Array[Short]]   => java.util.Arrays.copyOf(x.asInstanceOf[Array[Short]], newLength)
      case x if x.isInstanceOf[Array[Boolean]] => java.util.Arrays.copyOf(x.asInstanceOf[Array[Boolean]], newLength)
    }).asInstanceOf[Array[K]]

}
