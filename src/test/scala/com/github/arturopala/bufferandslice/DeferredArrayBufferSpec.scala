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

class DeferredArrayBufferSpec extends AnyWordSpecCompat {

  class A
  class B extends A
  case class Item(s: String) extends B

  "DeferredArrayBuffer" should {

    "behave as empty when uninitialized" in {
      DeferredArrayBuffer(4).asArray should not be null
      DeferredArrayBuffer(0).asSlice should not be null
      DeferredArrayBuffer(0).slice(1, 3) should not be null
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        DeferredArrayBuffer(4).apply(0)
      }
    }

    "initialize an array on first write" in {
      DeferredArrayBuffer(4).update(0, "a").asArray shouldBe Array("a")
      DeferredArrayBuffer(0).push(1).push(2).asArray shouldBe Array(1, 2)
      DeferredArrayBuffer(3).push(true).push(false).asArray shouldBe Array(true, false)
      DeferredArrayBuffer(3).appendArray(Array(Item("a"), Item("b"))).asArray shouldBe Array(Item("a"), Item("b"))
      DeferredArrayBuffer(3).appendSlice(Slice(1.1, 2.2, 3.3)).asArray shouldBe Array(1.1, 2.2, 3.3)
      DeferredArrayBuffer(3).appendFromIterator(Iterator.from(0).map(_.toByte).take(4)).asArray shouldBe
        Array(0.toByte, 1.toByte, 2.toByte, 3.toByte)

      val a = new A
      val b = new B
      DeferredArrayBuffer(3).append(b).apply(0) shouldBe b
      DeferredArrayBuffer(3).append(b).append(b).append(b).asArray shouldBe Array(b, b, b)
      DeferredArrayBuffer(3).append(b).toArray[B] shouldBe Array(b)
      DeferredArrayBuffer(3).append(b).toArray[A] shouldBe Array(b)
      DeferredArrayBuffer(3).append(a).append(b).toArray[A] shouldBe Array(a, b)
      DeferredArrayBuffer(3).append(a).append(b).asArray shouldBe Array(a, b)
      DeferredArrayBuffer(3).append(a).append(b).asSlice.asArray.take(3) shouldBe Array(a, b)
      val arr2 = DeferredArrayBuffer(3).append(a).append(b).asSlice.toArray[A]
      arr2 shouldBe Array(a, b)
      DeferredArrayBuffer(3).append(b).append(b).asSlice.toBuffer[A].push(a).toArray shouldBe Array(b, b, a)
      DeferredArrayBuffer(3).append(b).append(b).asSlice.toBuffer[A].push(a).asArray shouldBe Array(b, b, a)
    }
  }

}
