package com.matthalstead.workparallelizer.perftest

import com.matthalstead.workparallelizer.WorkDefinition
import com.matthalstead.workparallelizer.WorkParallelizerConfig
import com.matthalstead.workparallelizer.impl.ThreePhaseCoroutinesWP
import com.matthalstead.workparallelizer.impl.WorkParallelizerContext
import com.matthalstead.workparallelizer.stats.StatsTracker
import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class ThreePhaseCoroutinesPerfTest {

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
  fun `perf test - SMALL`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.SMALL_TEST_INPUT_COUNT)
  }

  @Test
  fun `perf test - MEDIUM`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.MEDIUM_TEST_INPUT_COUNT)
  }

  @Test
  fun `perf test - LARGE`() {
    singleThreadedPerfTest(inputCount = PerfTestRunner.LARGE_TEST_INPUT_COUNT)
  }

  private fun singleThreadedPerfTest(inputCount: Long) {
    println("Running perf test with inputCount = $inputCount")
    val inputChannel = Channel<String>(capacity = Channel.UNLIMITED)
    val countingOutput = PerfTestRunner.CountingOutput<ByteArray>()
    val context = WorkParallelizerContext(
      workDefinition = WorkDefinition(
        input = inputChannel.toWorkInput(),
        transform = PerfTestRunner.transform,
        output = countingOutput,
        errorHandler = PerfTestRunner.errorHandler
      ),
      config = WorkParallelizerConfig(),
      executorService = executorService,
      statsTracker = StatsTracker()
    )

    val perfTestRunner = PerfTestRunner(
      workParallelizer = ThreePhaseCoroutinesWP(context),
      inputAccepter = { inputChannel.trySend(it) },
      inputCount = inputCount
    )

    perfTestRunner.run()
  }
}