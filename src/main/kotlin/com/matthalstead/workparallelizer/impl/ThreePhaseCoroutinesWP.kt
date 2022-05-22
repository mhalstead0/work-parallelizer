package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.ConfigException
import com.matthalstead.workparallelizer.StartedTwiceException
import com.matthalstead.workparallelizer.WorkInputBlocking
import com.matthalstead.workparallelizer.WorkInputSuspending
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ThreePhaseCoroutinesWP<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  private val workDef = workParallelizerContext.workDefinition

  private val workInput: WorkInputSuspending<I> = when (workDef.input) {
    is WorkInputSuspending -> workDef.input
    else -> throw ConfigException("ThreePhaseCoroutinesWP does not support input from ${workDef.input.javaClass}")
  }

  private val statsTracker = workParallelizerContext.statsTracker
  override val stats: WorkParallelizerStats = statsTracker

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

      runBlocking {
        //TODO handle input exceptions
        val batch = workInput.take(workParallelizerContext.config.batchSize)
        println("Batch size: ${batch.size}")
        batch.forEach { input ->
          async { //TODO handle transform exceptions
            val output = workDef.transform(input)
            statsTracker.run {
              workDef.output(output) //TODO handle output exceptions
            }
          }
        }
      }
    }
  }

  override fun destroy() {
    killed.set(true)
  }

}