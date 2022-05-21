package com.matthalstead.workparallelizer

class WorkDefinition<I, O> (
  val input: WorkInput<I>,
  val transform: (I) -> O,
  val output: (O) -> Unit,
  val errorHandler: (WorkParallelizerException) -> Unit
)

interface WorkInput<T> {
  fun takeBlocking(maxCount: Int): List<T>
}
