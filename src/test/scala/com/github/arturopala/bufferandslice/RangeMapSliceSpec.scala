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

class RangeMapSliceSpec extends AnyWordSpecCompat {

  "RangeMapSlice" should {

    "have a length" in {
      RangeMapSlice(x => s"$x").length shouldBe Int.MaxValue
      RangeMapSlice(x => s"$x", 0, 10).length shouldBe 10
      RangeMapSlice(x => s"$x", 3, 9).length shouldBe 6
      RangeMapSlice(x => s"$x", 100, 1000).length shouldBe 900
    }

    "have toArray" in {
      RangeMapSlice(x => s"$x").take(3).toArray shouldBe Array("0", "1", "2")
      RangeMapSlice(x => s"$x", 0, 2).toArray shouldBe Array("0", "1")
      RangeMapSlice(x => s"$x", 3, 9).toArray shouldBe Array("3", "4", "5", "6", "7", "8")
      RangeMapSlice(x => s"$x", 4, 7).toArray shouldBe Array("4", "5", "6")
    }

    "have slice" in {
      RangeMapSlice(x => s"$x").slice(3, 7).toArray shouldBe Array("3", "4", "5", "6")
      RangeMapSlice(x => s"$x").slice(0, 2).toArray shouldBe Array("0", "1")
      RangeMapSlice(x => s"$x").slice(3, 9).toArray shouldBe Array("3", "4", "5", "6", "7", "8")
      RangeMapSlice(x => s"$x").slice(3, 9).slice(2, 4).toArray shouldBe Array("5", "6")
      RangeMapSlice(x => s"$x").slice(4, 7).toArray shouldBe Array("4", "5", "6")
      RangeMapSlice(x => s"$x").slice(4, 7).slice(1, 6).toArray shouldBe Array("5", "6")
    }

    "have map" in {
      RangeMapSlice(x => s"$x").take(3).map(s => s"$s$s").toArray shouldBe Array("00", "11", "22")
      RangeMapSlice(x => s"$x", 0, 2).map(s => s"$s$s").toArray shouldBe Array("00", "11")
      RangeMapSlice(x => s"$x", 3, 9).map(s => s"$s$s").toArray shouldBe Array("33", "44", "55", "66", "77", "88")
      RangeMapSlice(x => s"$x", 4, 7).map(s => s"$s$s").toArray shouldBe Array("44", "55", "66")
      RangeMapSlice(x => s"$x").slice(3, 9).map(s => s"$s$s").slice(2, 4).toArray shouldBe Array("55", "66")
      RangeMapSlice(x => s"$x").slice(3, 9).slice(2, 4).map(s => s"$s$s").toArray shouldBe Array("55", "66")
      RangeMapSlice(x => s"$x").map(s => s"$s$s").slice(3, 9).slice(2, 4).toArray shouldBe Array("55", "66")
    }

    "have toBuffer" in {
      RangeMapSlice(x => s"$x").slice(3, 7).toBuffer.toArray shouldBe Array("3", "4", "5", "6")
      RangeMapSlice(x => s"$x").slice(0, 2).toBuffer.toArray shouldBe Array("0", "1")
      RangeMapSlice(x => s"$x").slice(3, 9).toBuffer.toArray shouldBe Array("3", "4", "5", "6", "7", "8")
      RangeMapSlice(x => s"$x").slice(3, 9).slice(2, 4).toBuffer.toArray shouldBe Array("5", "6")
      RangeMapSlice(x => s"$x").slice(4, 7).toBuffer.toArray shouldBe Array("4", "5", "6")
      RangeMapSlice(x => s"$x").slice(4, 7).slice(1, 6).toBuffer.toArray shouldBe Array("5", "6")
    }

  }

}
