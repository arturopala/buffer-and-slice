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

/**
  * Set of functions to update buffers or sequences of indexes values
  * in order to match corresponding buffer operations.
  * Useful to track values and structures movement during buffer modifications.
  */
object IndexTracker {

  /** Modify values in the index buffer tracking [[Buffer.shiftRight]] operation effect. */
  final def trackShiftRight(index: Int, distance: Int, indexes: IntBuffer): IntBuffer = {
    if (indexes.nonEmpty && distance > 0 && index >= 0) {
      indexes.modifyAllWhen(_ + distance, _ >= index)
    }
    indexes
  }

  /** Modify the index sequence tracking [[Buffer.shiftRight]] operation effect. */
  final def trackShiftRight[S <: Seq[Int]](index: Int, distance: Int, indexes: S): S =
    if (indexes.nonEmpty && distance > 0 && index >= 0)
      indexes
        .map(i => if (i >= index) i + distance else i)
        .asInstanceOf[S]
    else indexes

  /** Modify values in the index buffer tracking [[Buffer.shiftLeft]] operation effect. */
  final def trackShiftLeft(index: Int, distance: Int, indexes: IntBuffer): IntBuffer = {
    if (indexes.nonEmpty && distance > 0 && index >= 0) {
      indexes.removeWhen(i => i >= index - distance && i < index)
      indexes.modifyAllWhen(_ - distance, _ >= index)
      indexes.removeWhen(_ < 0)
    }
    indexes
  }

  /** Modify the index sequence tracking [[Buffer.shiftLeft]] operation effect. */
  final def trackShiftLeft[S <: Seq[Int]](index: Int, distance: Int, indexes: S): S =
    if (indexes.nonEmpty && distance > 0 && index >= 0)
      indexes
        .filterNot(i => i >= index - distance && i < index)
        .map(i => if (i >= index) i - distance else i)
        .filterNot(_ < 0)
        .asInstanceOf[S]
    else indexes

  /** Modify values in the index buffer tracking [[Buffer.moveRangeRight]] operation effect. */
  final def trackMoveRangeRight(fromIndex: Int, toIndex: Int, distance: Int, indexes: IntBuffer): IntBuffer = {
    if (indexes.nonEmpty && distance > 0 && fromIndex >= 0 && toIndex > fromIndex) {
      val offset = fromIndex - toIndex
      indexes.modifyAll { i =>
        if (i >= fromIndex && i < toIndex) i + distance
        else if (i >= toIndex && i < toIndex + distance) i + offset
        else i
      }
    }
    indexes
  }

  /** Modify values in the index buffer tracking [[Buffer.moveRangeRight]] operation effect. */
  final def trackMoveRangeRight[S <: Seq[Int]](fromIndex: Int, toIndex: Int, distance: Int, indexes: S): S =
    if (indexes.nonEmpty && distance > 0 && fromIndex >= 0 && toIndex > fromIndex) {
      val offset = fromIndex - toIndex
      indexes
        .map { i =>
          if (i >= fromIndex && i < toIndex) i + distance
          else if (i >= toIndex && i < toIndex + distance) i + offset
          else i
        }
        .asInstanceOf[S]
    } else indexes

  /** Modify values in the index buffer tracking [[Buffer.moveRangeLeft]] operation effect. */
  final def trackMoveRangeLeft(fromIndex: Int, toIndex: Int, distance: Int, indexes: IntBuffer): IntBuffer = {
    if (indexes.nonEmpty && distance > 0 && fromIndex >= 0 && toIndex > fromIndex) {
      val offset = toIndex - fromIndex
      val shift = Math.max(0, distance - fromIndex)
      if (shift > 0) {
        indexes.modifyAll(_ + shift)
      }
      indexes.modifyAll { i =>
        if (i >= fromIndex + shift && i < toIndex + shift) i - distance
        else if (i >= fromIndex - distance + shift && i < fromIndex + shift) i + offset
        else i
      }
    }
    indexes
  }

  /** Modify values in the index buffer tracking [[Buffer.moveRangeLeft]] operation effect. */
  final def trackMoveRangeLeft[S <: Seq[Int]](fromIndex: Int, toIndex: Int, distance: Int, indexes: S): S =
    if (indexes.nonEmpty && distance > 0 && fromIndex >= 0 && toIndex > fromIndex) {
      val offset = toIndex - fromIndex
      val shift = Math.max(0, distance - fromIndex)
      val indexes2 = if (shift > 0) indexes.map(_ + shift) else indexes
      indexes2
        .map { i =>
          if (i >= fromIndex + shift && i < toIndex + shift) i - distance
          else if (i >= fromIndex - distance + shift && i < fromIndex + shift) i + offset
          else i
        }
        .asInstanceOf[S]
    } else indexes

  /** Modify values in the index buffer tracking [[Buffer.swapRange]] operation effect.
    * @note removed values are marked with -1 */
  final def trackSwapRange(first: Int, second: Int, swapLength: Int, indexes: IntBuffer): IntBuffer = {
    if (indexes.nonEmpty && swapLength > 0 && first >= 0 && second >= 0 && first != second && first + swapLength >= 0 && second + swapLength >= 0) {
      val offset = first - second
      val hasLeftOverlap = Math.abs(offset) < swapLength && first < second
      val hasRightOverlap = !hasLeftOverlap && Math.abs(offset) < swapLength && first > second
      indexes.modifyAll { i =>
        if (hasLeftOverlap && i >= first && i < second) {
          -1
        } else if (hasRightOverlap && i >= second + swapLength && i < first + swapLength) {
          -1
        } else if (i >= second && i < second + swapLength) i + offset
        else if (i >= first && i < first + swapLength) i - offset
        else i
      }
    }
    indexes
  }

  /** Modify values in the index buffer tracking [[Buffer.swapRange]] operation effect.
    * @note removed values are marked with -1 */
  final def trackSwapRange[S <: Seq[Int]](first: Int, second: Int, swapLength: Int, indexes: S): S =
    if (indexes.nonEmpty && swapLength > 0 && first >= 0 && second >= 0 && first != second && first + swapLength >= 0 && second + swapLength >= 0) {
      val offset = first - second
      val hasLeftOverlap = Math.abs(offset) < swapLength && first < second
      val hasRightOverlap = !hasLeftOverlap && Math.abs(offset) < swapLength && first > second
      indexes
        .map { i =>
          if (hasLeftOverlap && i >= first && i < second) {
            -1
          } else if (hasRightOverlap && i >= second + swapLength && i < first + swapLength) {
            -1
          } else if (i >= second && i < second + swapLength) i + offset
          else if (i >= first && i < first + swapLength) i - offset
          else i
        }
        .asInstanceOf[S]
    } else indexes
}
