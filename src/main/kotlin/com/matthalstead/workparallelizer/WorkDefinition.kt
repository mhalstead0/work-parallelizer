package com.matthalstead.workparallelizer

class WorkDefinition<I, O> (
  val input: () -> I,
  val transform: (I) -> O,
  val output: (O) -> Unit,
  val errorHandler: (WorkParallelizerException) -> Unit
)
