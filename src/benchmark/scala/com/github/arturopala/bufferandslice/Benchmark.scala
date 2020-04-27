package com.github.arturopala.bufferandslice

import org.scalameter.api._

object Benchmark extends Bench.LocalTime {

  val sizes: Gen[Int] = Gen.exponential("size")(100, 1000000, 10)

}
