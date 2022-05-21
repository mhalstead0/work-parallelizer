package com.matthalstead.workparallelizer.test

import com.matthalstead.workparallelizer.OrderingType
import com.matthalstead.workparallelizer.WorkParallelizer
import org.assertj.core.api.Assertions
import java.time.Instant
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

object ParallelizerTestCases {

  fun testSimple(
    workParallelizer: WorkParallelizer,
    inputSink: (Int) -> Unit,
    outputQueue: LinkedBlockingQueue<String>,
    ordering: OrderingType
  ) {
    val count = 100
    (0 until count).forEach { inputSink(it) }

    val deadline = Instant.now().plusSeconds(10L)
    val timedOut = { Instant.now().isAfter(deadline) }
    val gotEnoughRecords = { workParallelizer.stats.getTotalProcessedCount() >= count }
    while (!gotEnoughRecords() && !timedOut()) {
      Thread.sleep(100L)
    }

    val outputList = outputQueue.toList()
    when (ordering) {
      OrderingType.STRICT -> {
        Assertions.assertThat(outputQueue.toList()).isEqualTo(outputList)
      }
      OrderingType.LOOSE -> {
        Assertions.assertThat(outputQueue.toSet()).isEqualTo(outputList.toSet())
      }
    }
    Assertions.assertThat(workParallelizer.stats.getTotalProcessedCount()).isEqualTo(count.toLong())
    Assertions.assertThat(workParallelizer.stats.getTotalSuccessCount()).isEqualTo(count.toLong())
  }

  private fun <T> BlockingQueue<T>.toList() = asIterable().toList()

}