package com.matthalstead.workparallelizer

data class WorkParallelizerConfig(
  val ordering: OrderingType = OrderingType.STRICT,
  val inputParallelism: ParallelismType = ParallelismType.SERIAL,
  val outputParallelismType: ParallelismType = ParallelismType.SERIAL,
  val batchSize: Int = 1000
)

enum class OrderingType {
  STRICT,
  LOOSE
}

enum class ParallelismType {
  SERIAL,
  PARALLEL
}