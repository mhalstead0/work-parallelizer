package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.ConfigException
import com.matthalstead.workparallelizer.WorkInputBlocking
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats

class SimpleWorkParallelizer<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  private val workDef = workParallelizerContext.workDefinition

  private val workInput: WorkInputBlocking<I> = when (workDef.input) {
    is WorkInputBlocking -> workDef.input
    else -> throw ConfigException("SimpleWorkParallelizer does not support input from ${workDef.input.javaClass}")
  }

  private val statsTracker = workParallelizerContext.statsTracker
  override val stats: WorkParallelizerStats = statsTracker


  private val lifecycleHelper = LifecycleHelper()

  override fun start() {
    lifecycleHelper.start()

    workParallelizerContext.executorService.submit(this::run)
  }

  private fun run() {
    lifecycleHelper.repeatUntilKilled {

      //TODO handle input exceptions
      val batch = workInput.takeBlocking(workParallelizerContext.config.batchSize)

      batch.forEach { i ->
        statsTracker.run {
          //TODO handle transform exceptions
          val o = workDef.transform(i)

          //TODO handle output exceptions
          workDef.output(o)

        }
      }
    }
  }

  override fun destroy() {
    lifecycleHelper.kill()
  }

}