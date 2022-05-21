package com.matthalstead.workparallelizer.utils

import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class WorkInputLBQTest {
  @Test
  fun `gets all available with exact count`() {
    val lbq = LinkedBlockingQueue(listOf(1, 2, 3))
    val workInput = lbq.toWorkInput()
    val taken = workInput.takeBlocking(3)
    assertThat(taken).isEqualTo(listOf(1, 2, 3))
  }

  @Test
  fun `gets all available below max`() {
    val lbq = LinkedBlockingQueue(listOf(1, 2, 3))
    val workInput = lbq.toWorkInput()
    val taken = workInput.takeBlocking(5)
    assertThat(taken).isEqualTo(listOf(1, 2, 3))
  }

  @Test
  fun `gets all available above max`() {
    val lbq = LinkedBlockingQueue(listOf(1, 2, 3, 4, 5))
    val workInput = lbq.toWorkInput()
    val taken = workInput.takeBlocking(4)
    assertThat(taken).isEqualTo(listOf(1, 2, 3, 4))
  }

  @Test
  fun `blocks until available`() {
    val lbq = LinkedBlockingQueue<Int>()
    val workInput = lbq.toWorkInput()

    val consumerThreadStarted = AtomicBoolean(false)
    val taken = AtomicReference<List<Int>>(null)
    val consumerThread = Thread {
      consumerThreadStarted.set(true)
      taken.set(workInput.takeBlocking(1))
    }
    consumerThread.start()

    while (!consumerThreadStarted.get()) {
      Thread.sleep(100L)
    }

    lbq.add(1)
    consumerThread.join()

    assertThat(taken.get()).isEqualTo(listOf(1))
  }

}