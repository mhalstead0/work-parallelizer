package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.ConfigException
import com.matthalstead.workparallelizer.StartedTwiceException
import com.matthalstead.workparallelizer.WorkInputBlocking
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class TransformInCoroutineWP<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  private val workDef = workParallelizerContext.workDefinition

  private val workInput: WorkInputBlocking<I> = when (workDef.input) {
    is WorkInputBlocking -> workDef.input
    else -> throw ConfigException("TransformInCoroutineWP does not support input from ${workDef.input.javaClass}")
  }

  private val statsTracker = workParallelizerContext.statsTracker
  override val stats: WorkParallelizerStats = statsTracker

  private val started = AtomicBoolean(false)
  private val killed = AtomicBoolean(false)

  private val dispatcher = Dispatchers.Default // TODO tunable parallelism

  override fun start() {
    if (!started.compareAndSet(false, true)) {
      throw StartedTwiceException()
    }

    workParallelizerContext.executorService.submit(this::run)
  }

  private fun run() {
    while (!killed.get()) {

      //TODO handle input exceptions
      val batch = workInput.takeBlocking(workParallelizerContext.config.batchSize)

      val outputValues = runBlocking {
        batch.map { input ->
          withContext(dispatcher) {
            async {
              workDef.transform(input) //TODO handle transform exceptions
            }
          }
        }.awaitAll()
      }

      outputValues.forEach {
        statsTracker.run {
          workDef.output(it) //TODO handle output exceptions
        }

      }
    }
  }

  override fun destroy() {
    killed.set(true)
  }

}