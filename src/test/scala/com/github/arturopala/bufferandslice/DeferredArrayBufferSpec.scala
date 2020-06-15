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
      DeferredArrayBuffer(0).asArray should not be null
      an[RuntimeException] shouldBe thrownBy {
        DeferredArrayBuffer(4).asArray
      }
      DeferredArrayBuffer(0).asSlice should not be null
      DeferredArrayBuffer(0).slice(1, 3) should not be null
      an[RuntimeException] shouldBe thrownBy {
        DeferredArrayBuffer(4).apply(3)
      }
      an[IndexOutOfBoundsException] shouldBe thrownBy {
        DeferredArrayBuffer(4).apply(4)
      }
    }

    "initialize an array on first write" in {
      DeferredArrayBuffer(1).update(0, "a").asArray shouldBe Array("a")
      DeferredArrayBuffer().push(1).push(2).asArray shouldBe Array(1, 2)
      DeferredArrayBuffer().push(true).push(false).asArray shouldBe Array(true, false)
      DeferredArrayBuffer().appendArray(Array(Item("a"), Item("b"))).asArray shouldBe Array(Item("a"), Item("b"))
      DeferredArrayBuffer().appendSlice(Slice(1.1, 2.2, 3.3)).asArray shouldBe Array(1.1, 2.2, 3.3)
      DeferredArrayBuffer().appendFromIterator(Iterator.from(0).map(_.toByte).take(4)).asArray shouldBe
        Array(0.toByte, 1.toByte, 2.toByte, 3.toByte)

      val a = new A
      val b = new B
      DeferredArrayBuffer().append(b).apply(0) shouldBe b
      DeferredArrayBuffer().append(b).append(b).append(b).asArray shouldBe Array(b, b, b)
      DeferredArrayBuffer().append(b).toArray[B] shouldBe Array(b)
      DeferredArrayBuffer().append(b).toArray[A] shouldBe Array(b)
      DeferredArrayBuffer().append(a).append(b).toArray[A] shouldBe Array(a, b)
      DeferredArrayBuffer().append(a).append(b).asArray shouldBe Array(a, b)
      DeferredArrayBuffer().append(a).append(b).asSlice.asArray.take(3) shouldBe Array(a, b)
      val arr2 = DeferredArrayBuffer().append(a).append(b).asSlice.toArray[A]
      arr2 shouldBe Array(a, b)
      DeferredArrayBuffer().append(b).append(b).asSlice.toBuffer[A].push(a).toArray shouldBe Array(b, b, a)
      DeferredArrayBuffer().append(b).append(b).asSlice.toBuffer[A].push(a).asArray shouldBe Array(b, b, a)
      DeferredArrayBuffer().append(b).append(b).emptyCopy.toArray shouldBe Array.empty[B]
      DeferredArrayBuffer().append(a).append(b).copy.toArray shouldBe Array(a, b)
      DeferredArrayBuffer().append(a).append(b).append(a).slice(1, 3).toArray shouldBe Array(b, a)
    }

    "have same behaviour as ArrayBuffer" in {
      DeferredArrayBuffer(10).length shouldBe ArrayBuffer[Int](10).length
      DeferredArrayBuffer().push(1).toArray shouldBe ArrayBuffer[Int]().push(1).toArray
      DeferredArrayBuffer().push(1).peek shouldBe ArrayBuffer[Int]().push(1).peek
      DeferredArrayBuffer().push(1).push(2).copy.toArray shouldBe
        ArrayBuffer[Int]().push(1).push(2).copy.toArray
      DeferredArrayBuffer().push(1).emptyCopy.push(2).toArray shouldBe
        ArrayBuffer[Int]().push(1).emptyCopy.push(2).toArray
      DeferredArrayBuffer().push(1).push(2).apply(0) shouldBe
        ArrayBuffer[Int]().push(1).push(2).apply(0)
      DeferredArrayBuffer().push(1).update(0, 2).apply(0) shouldBe
        ArrayBuffer[Int]().push(1).update(0, 2).apply(0)

      val a = new A
      val b = new B
      DeferredArrayBuffer(10).length shouldBe ArrayBuffer[A](10).length
      DeferredArrayBuffer().push(a).toArray shouldBe ArrayBuffer[A]().push(a).toArray
      DeferredArrayBuffer().push(a).peek shouldBe ArrayBuffer[A]().push(a).peek
      DeferredArrayBuffer().push(a).push(b).copy.toArray shouldBe
        ArrayBuffer[A]().push(a).push(b).copy.toArray
      DeferredArrayBuffer().push(a).emptyCopy.push(b).toArray shouldBe
        ArrayBuffer[A]().push(a).emptyCopy.push(b).toArray
      DeferredArrayBuffer().push(a).push(b).apply(0) shouldBe
        ArrayBuffer[A]().push(a).push(b).apply(0)
      DeferredArrayBuffer().push(a).update(0, b).apply(0) shouldBe
        ArrayBuffer[A]().push(a).update(0, b).apply(0)

    }
  }

}
