package com.matthalstead.workparallelizer

import com.matthalstead.workparallelizer.test.TestUtils.retryUntilTrueOrTimeout_Blocking
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

class SimpleFactoryTest {

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
  fun `blocking implementation`() {
    val inputQueue = LinkedBlockingQueue<Int>()
    val outputQueue = LinkedBlockingQueue<String>()
    val workParallelizer = WorkParallelizer.Factory.create(
      workDefinition = WorkDefinition(
        input = inputQueue.toWorkInput(),
        transform = { "$it" },
        output = { outputQueue.add(it) },
        errorHandler = { it.printStackTrace() }
      ),
      config = WorkParallelizerConfig(),
      executorService = executorService
    )

    try {
      workParallelizer.start()
      inputQueue.add(123)

      retryUntilTrueOrTimeout_Blocking(
        maxDuration = Duration.ofSeconds(10)
      ) {
        workParallelizer.stats.getTotalProcessedCount() >= 1L
      }

      assertThat(workParallelizer.stats.getTotalProcessedCount()).isEqualTo(1L)
      assertThat(outputQueue.size).isEqualTo(1)
      assertThat(outputQueue.toList()).isEqualTo(listOf("123"))

    } finally {
      workParallelizer.destroy()
    }
  }

  @Test
  fun `suspending implementation`() {
    val inputChannel = Channel<Int>(capacity = Channel.UNLIMITED)
    val outputQueue = LinkedBlockingQueue<String>()
    val workParallelizer = WorkParallelizer.Factory.create(
      workDefinition = WorkDefinition(
        input = inputChannel.toWorkInput(),
        transform = { "$it" },
        output = { outputQueue.add(it) },
        errorHandler = { it.printStackTrace() }
      ),
      config = WorkParallelizerConfig(),
      executorService = executorService
    )

    try {
      workParallelizer.start()
      inputChannel.trySend(123)

      retryUntilTrueOrTimeout_Blocking(
        maxDuration = Duration.ofSeconds(10)
      ) {
        workParallelizer.stats.getTotalProcessedCount() >= 1L
      }

      assertThat(workParallelizer.stats.getTotalProcessedCount()).isEqualTo(1L)
      assertThat(outputQueue.size).isEqualTo(1)
      assertThat(outputQueue.toList()).isEqualTo(listOf("123"))

    } finally {
      workParallelizer.destroy()
    }
  }

}