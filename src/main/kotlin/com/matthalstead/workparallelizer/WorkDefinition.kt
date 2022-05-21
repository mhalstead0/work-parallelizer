package com.matthalstead.workparallelizer

class WorkDefinition<I, O> (
  val input: WorkInput<I>,
  val transform: (I) -> O,
  val output: (O) -> Unit,
  val errorHandler: (WorkParallelizerException) -> Unit
)

sealed interface WorkInput<T>

interface WorkInputBlocking<T> : WorkInput<T> {
  fun takeBlocking(maxCount: Int): List<T>
}

interface WorkInputSuspending<T> : WorkInput<T> {
  suspend fun take(maxCount: Int): List<T>
}
