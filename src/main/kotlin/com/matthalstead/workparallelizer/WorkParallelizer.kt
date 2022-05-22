package com.matthalstead.workparallelizer

import com.matthalstead.workparallelizer.impl.ThreePhaseCoroutinesWP
import com.matthalstead.workparallelizer.impl.TransformInCoroutineWP
import com.matthalstead.workparallelizer.impl.WorkParallelizerContext
import com.matthalstead.workparallelizer.stats.StatsTracker
import java.util.concurrent.ExecutorService

interface WorkParallelizer {
  fun start()
  fun destroy()

  val stats: WorkParallelizerStats

  object Factory {
    fun<I, O> create(
      workDefinition: WorkDefinition<I, O>,
      config: WorkParallelizerConfig,
      executorService: ExecutorService
    ): WorkParallelizer = WorkParallelizerBuilder(workDefinition, config.clean(), executorService).create()
  }
}


private class WorkParallelizerBuilder<I, O>(
  private val workDefinition: WorkDefinition<I, O>,
  config: WorkParallelizerConfig,
  executorService: ExecutorService
) {

  private val context = WorkParallelizerContext(
    workDefinition = workDefinition,
    config = config,
    executorService = executorService,
    statsTracker = StatsTracker()
  )

  fun create(): WorkParallelizer {
    return when (workDefinition.input) {
      is WorkInputBlocking -> createBlocking()
      is WorkInputSuspending -> createSuspending()
    }
  }

  private fun createBlocking(): WorkParallelizer = TransformInCoroutineWP(context)

  private fun createSuspending(): WorkParallelizer = ThreePhaseCoroutinesWP(context)

}

