package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.ConfigException
import com.matthalstead.workparallelizer.WorkInputSuspending
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

  private val lifecycleHelper = LifecycleHelper()

  override fun start() {
    lifecycleHelper.start()

    workParallelizerContext.executorService.submit(this::run)
  }

  private fun run() {
    lifecycleHelper.repeatUntilKilled {

      runBlocking {
        //TODO handle input exceptions
        val batch = workInput.take(workParallelizerContext.config.batchSize)
        batch.forEach { input ->
          Dispatchers.Default.invoke {
            this.launch { //TODO handle transform exceptions
              val output = workDef.transform(input)
              statsTracker.run {
                workDef.output(output) //TODO handle output exceptions
              }
            }
          }
        }
      }
    }
  }

  override fun destroy() {
    lifecycleHelper.kill()
  }

}