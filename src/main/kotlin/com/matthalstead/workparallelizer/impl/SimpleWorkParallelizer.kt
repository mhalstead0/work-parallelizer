package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.StartedTwiceException
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import java.util.concurrent.atomic.AtomicBoolean

class SimpleWorkParallelizer<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  private val statsTracker = workParallelizerContext.statsTracker
  override val stats: WorkParallelizerStats = statsTracker

  private val workDef = workParallelizerContext.workDefinition


  private val started = AtomicBoolean(false)
  private val killed = AtomicBoolean(false)

  override fun start() {
    if (!started.compareAndSet(false, true)) {
      throw StartedTwiceException()
    }

    workParallelizerContext.executorService.submit(this::run)
  }

  private fun run() {
    while (!killed.get()) {

      //TODO handle input exceptions
      val batch = workDef.input.takeBlocking(workParallelizerContext.config.batchSize)

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
    killed.set(true)
  }

}