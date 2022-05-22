package com.matthalstead.workparallelizer

import com.matthalstead.workparallelizer.test.ConcurrentCallTracker
import com.matthalstead.workparallelizer.test.TestUtils.retryUntilTrueOrTimeout_Blocking
import com.matthalstead.workparallelizer.test.WorkInputSuspendingWithConcurrencyTracking
import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import kotlinx.coroutines.channels.Channel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class InputParallelismTest {

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
  fun `serial input and output`() {
    val inputChannel = Channel<Int>(capacity = Channel.UNLIMITED)
    val outputQueue = Channel<String>(capacity = Channel.UNLIMITED)

    val inputConcurrentCallTracker = ConcurrentCallTracker()
    val outputConcurrentCallTracker = ConcurrentCallTracker()

    val input = WorkInputSuspendingWithConcurrencyTracking(
      inputChannel.toWorkInput(),
      inputConcurrentCallTracker
    )

    val output: (String) -> Unit = { s ->
      outputConcurrentCallTracker.track {
        outputQueue.trySend(s)
      }
    }

    val workParallelizer = WorkParallelizer.Factory.create(
      workDefinition = WorkDefinition(
        input = input,
        transform = { "$it" },
        output = output,
        errorHandler = { it.printStackTrace() }
      ),
      config = WorkParallelizerConfig(
        ordering = OrderingType.LOOSE,
        inputParallelism = ParallelismType.SERIAL,
        outputParallelism = ParallelismType.SERIAL
    ),
      executorService = executorService
    )

    try {
      val numberToSend = 1_000
      workParallelizer.start()
      (1..numberToSend).forEach {
        inputChannel.trySend(it)
      }

      retryUntilTrueOrTimeout_Blocking(maxDuration = Duration.ofSeconds(10)) {
        workParallelizer.stats.getTotalProcessedCount() >= numberToSend
      }

      assertThat(workParallelizer.stats.getTotalProcessedCount()).isEqualTo(numberToSend.toLong())
      assertThat(inputConcurrentCallTracker.getMaxConcurrency()).isEqualTo(1)
      assertThat(outputConcurrentCallTracker.getMaxConcurrency()).isEqualTo(1)

    } finally {
      workParallelizer.destroy()
    }
  }


}