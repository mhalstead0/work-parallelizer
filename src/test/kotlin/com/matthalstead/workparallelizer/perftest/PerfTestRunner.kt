package com.matthalstead.workparallelizer.perftest

import com.google.common.base.Stopwatch
import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerException
import com.matthalstead.workparallelizer.test.TestUtils
import com.matthalstead.workparallelizer.test.TestUtils.retryUntilTrueOrTimeout_Blocking
import com.matthalstead.workparallelizer.utils.Hasher
import org.junit.jupiter.api.fail
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

class PerfTestRunner(
  val workParallelizer: WorkParallelizer,
  val inputAccepter: (String) -> Unit,
  val inputCount: Long,
  val maxDuration: Duration = Duration.ofMinutes(10L),
  val warmupCount: Long = 100L
) {

  fun run() {
    try {
      workParallelizer.start()

      val stopwatch = Stopwatch.createStarted()
      run(warmupCount, maxDuration)
      val warmupElapsed = stopwatch.elapsed()

      val remainingTime = maxDuration.minus(warmupElapsed)

      reportStats {
        run(inputCount, remainingTime)
      }
    } finally {
      workParallelizer.destroy()
    }
  }

  private fun run( count: Long, maxDuration: Duration) {
    val wpStats = workParallelizer.stats

    val startProcessedCount = wpStats.getTotalProcessedCount()
    val targetProcessedCount = startProcessedCount + count
    (1..count).forEach { inputNumber ->
      inputAccepter("$inputNumber")
    }

    val retryResult = retryUntilTrueOrTimeout_Blocking(
      maxDuration = maxDuration
    ) {
      wpStats.getTotalProcessedCount() >= targetProcessedCount
    }

    when (retryResult) {
      is TestUtils.ReturnedTrue -> { /* no-op */ }
      is TestUtils.TimedOut -> fail("Timed out while waiting for processing")
    }
  }

  private fun reportStats(f: () -> Unit) {
    val startCount = workParallelizer.stats.getTotalProcessedCount()
    val stopwatch = Stopwatch.createStarted()
    f()
    val elapsed = stopwatch.elapsed()
    val endCount = workParallelizer.stats.getTotalProcessedCount()

    val processedCount = endCount - startCount
    val millis = elapsed.toMillis()
    println("Processed count: $processedCount")
    println("Duration (ms): $millis")
    println("Throughput (rec/s): ${getRecordsPerSecond(processedCount, elapsed)}")

  }

  private fun getRecordsPerSecond(recordCount: Long, duration: Duration): Long {
    val dRecordCount = recordCount.toDouble()
    val dMillis = duration.toMillis().toDouble()
    val dSeconds = dMillis / 1000.0
    val dRPS = dRecordCount / dSeconds
    return dRPS.toLong()
  }

  companion object {
    const val SMALL_TEST_INPUT_COUNT = 100L
    const val MEDIUM_TEST_INPUT_COUNT = 10_000L
    const val LARGE_TEST_INPUT_COUNT = 1_000_000L
    val transform: (String) -> ByteArray = { Hasher.hash(it) }
    val errorHandler: (WorkParallelizerException) -> Unit = { e -> e.printStackTrace() }
  }

  class CountingOutput<T>: (T) -> Unit {
    private val counter = AtomicLong(0L)
    override fun invoke(t: T) {
      counter.incrementAndGet()
    }

    fun getCount() = counter.get()

  }

}