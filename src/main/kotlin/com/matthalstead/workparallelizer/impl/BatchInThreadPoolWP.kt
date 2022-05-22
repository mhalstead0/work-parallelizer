package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.ConfigException
import com.matthalstead.workparallelizer.StartedTwiceException
import com.matthalstead.workparallelizer.WorkInputBlocking
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class BatchInThreadPoolWP<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  private val workDef = workParallelizerContext.workDefinition

  private val workInput: WorkInputBlocking<I> = when (workDef.input) {
    is WorkInputBlocking -> workDef.input
    else -> throw ConfigException("BatchInThreadPoolWP does not support input from ${workDef.input.javaClass}")
  }

  private val statsTracker = workParallelizerContext.statsTracker
  override val stats: WorkParallelizerStats = statsTracker

  private val started = AtomicBoolean(false)
  private val killed = AtomicBoolean(false)

  private lateinit var threadPool: ExecutorService

  override fun start() {
    if (!started.compareAndSet(false, true)) {
      throw StartedTwiceException()
    }

    threadPool = Executors.newFixedThreadPool(16) // TODO configure thread pool
    workParallelizerContext.executorService.submit(this::run)
  }

  private fun run() {
    while (!killed.get()) {

      //TODO handle input exceptions
      val batch = workInput.takeBlocking(workParallelizerContext.config.batchSize)
      val futures: List<Future<*>> = batch.map { input ->
        val callable = Callable { workDef.transform(input) }
        threadPool.submit(callable) //TODO handle transform exceptions
      }

      futures.forEach { f ->
        val output = f.get() as O
        statsTracker.run {
          workDef.output(output) //TODO handle output exceptions
        }
      }
    }
  }

  override fun destroy() {
    killed.set(true)
    threadPool.shutdown()
  }

}