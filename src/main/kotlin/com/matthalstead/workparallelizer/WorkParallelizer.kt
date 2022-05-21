package com.matthalstead.workparallelizer

import java.util.concurrent.Executor

interface WorkParallelizer {
  fun start()
  fun destroy()

  val stats: WorkParallelizerStats

  object Factory {
    fun<I, O> create(
      workDefinition: WorkDefinition<I, O>,
      config: WorkParallelizerConfig,
      executor: Executor
    ): WorkParallelizer = TODO()
  }
}

