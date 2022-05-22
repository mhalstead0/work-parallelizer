package com.matthalstead.workparallelizer.perftest

import com.matthalstead.workparallelizer.WorkDefinition
import com.matthalstead.workparallelizer.WorkParallelizerConfig
import com.matthalstead.workparallelizer.WorkParallelizerException
import com.matthalstead.workparallelizer.impl.SimpleWorkParallelizer
import com.matthalstead.workparallelizer.impl.WorkParallelizerContext
import com.matthalstead.workparallelizer.stats.StatsTracker
import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicLong

class SingleThreadedPerfTest {

  private lateinit var executorService: ExecutorService

  @BeforeEach
  fun doSetup() {
    executorService = Executors.newSingleThreadExecutor()
  }

  @AfterEach
  fun doTearDown() {
    executorService.shutdown()
  }

  @Test
  fun `single threaded perf test - SMALL`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.SMALL_TEST_INPUT_COUNT)
  }

  @Test
  fun `single threaded perf test - MEDIUM`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.MEDIUM_TEST_INPUT_COUNT)
  }

  @Test
  fun `single threaded perf test - LARGE`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.LARGE_TEST_INPUT_COUNT)
  }

  private fun singleThreadedPerfTest(inputCount: Long) {
    println("Running perf test with inputCount = $inputCount")
    val inputQueue = LinkedBlockingQueue<String>()
    val countingOutput = PerfTestRunner.CountingOutput<ByteArray>()
    val context = WorkParallelizerContext(
      workDefinition = WorkDefinition(
        input = inputQueue.toWorkInput(),
        transform = PerfTestRunner.transform,
        output = countingOutput,
        errorHandler = PerfTestRunner.errorHandler
      ),
      config = WorkParallelizerConfig(),
      executorService = executorService,
      statsTracker = StatsTracker()
    )

    val perfTestRunner = PerfTestRunner(
      workParallelizer = SimpleWorkParallelizer(context),
      inputAccepter = { inputQueue.add(it) },
      inputCount = inputCount
    )

    perfTestRunner.run()
  }
}