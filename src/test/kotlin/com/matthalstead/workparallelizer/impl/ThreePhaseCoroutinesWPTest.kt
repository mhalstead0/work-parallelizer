package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.OrderingType
import com.matthalstead.workparallelizer.StartedTwiceException
import com.matthalstead.workparallelizer.WorkDefinition
import com.matthalstead.workparallelizer.WorkParallelizerConfig
import com.matthalstead.workparallelizer.WorkParallelizerException
import com.matthalstead.workparallelizer.stats.StatsTracker
import com.matthalstead.workparallelizer.test.ParallelizerTestCases
import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class ThreePhaseCoroutinesWPTest {

  private lateinit var workParallelizer: ThreePhaseCoroutinesWP<Int, String>
  private lateinit var inputChannel: Channel<Int>
  private lateinit var outputQueue: LinkedBlockingQueue<String>
  private lateinit var exceptionQueue: LinkedBlockingQueue<WorkParallelizerException>
  private lateinit var executorService: ExecutorService


  @BeforeEach
  fun doSetup() {
    inputChannel = Channel(Channel.UNLIMITED)
    outputQueue = LinkedBlockingQueue()
    exceptionQueue = LinkedBlockingQueue()

    executorService = Executors.newSingleThreadExecutor()
    val workDefinition = WorkDefinition(
      input = inputChannel.toWorkInput(),
      transform = { it.toString() },
      output = { outputQueue.add(it) },
      errorHandler = { exceptionQueue.add(it) }
    )
    val context = WorkParallelizerContext(
      workDefinition = workDefinition,
      config = WorkParallelizerConfig(),
      executorService = executorService,
      statsTracker = StatsTracker()
    )

    workParallelizer = ThreePhaseCoroutinesWP(context)
    workParallelizer.start()
  }

  @AfterEach
  fun doTearDown() {
    workParallelizer.destroy()
    executorService.shutdownNow()
  }

  @Test
  fun `simple processing`() {
    ParallelizerTestCases.testSimple(
      workParallelizer = workParallelizer,
      inputSink = { inputChannel.trySend(it) },
      outputQueue = outputQueue,
      ordering = OrderingType.LOOSE
    )
  }

  @Test
  fun `detect double-start`() {
    assertThrows<StartedTwiceException> { workParallelizer.start() }
  }
}