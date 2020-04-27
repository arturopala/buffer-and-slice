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

class FunSuite extends munit.FunSuite {

  def all[T]: T => Boolean = _ => true
  def none[T]: T => Boolean = _ => false

  val even: String => Boolean = s => s.head.toInt % 2 == 0
  val odd: String => Boolean = s => s.head.toInt  % 2 != 0

  def test(suiteName: String, suite: munit.FunSuite): Unit =
    this.munitTestsBuffer.appendAll(
      suite
        .munitTests()
        .map(test => test.withName(suiteName + " should " + test.name))
    )

}
