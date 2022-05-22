package com.matthalstead.workparallelizer

data class WorkParallelizerConfig(
  val ordering: OrderingType = OrderingType.STRICT,
  val inputParallelism: ParallelismType = ParallelismType.SERIAL,
  val outputParallelism: ParallelismType = ParallelismType.SERIAL,
  val batchSize: Int = 1000
) {
  fun clean() =
    if (ordering == OrderingType.STRICT) {
      this.copy(
        inputParallelism = ParallelismType.SERIAL,
        outputParallelism = ParallelismType.SERIAL
      )
    } else {
      this
    }
}

enum class OrderingType {
  STRICT,
  LOOSE
}

enum class ParallelismType {
  SERIAL,
  PARALLEL
}