package com.matthalstead.workparallelizer.utils

import com.matthalstead.workparallelizer.utils.WorkInputUtils.toWorkInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalCoroutinesApi::class)
class WorkInputChannelTest {
  @Test
  fun `gets all available with exact count`() {
    val channel = listOf(1, 2, 3).toChannel()
    val workInput = channel.toWorkInput()
    runBlocking {
      val taken = workInput.take(3)
      assertThat(taken).isEqualTo(listOf(1, 2, 3))
      assertThat(channel.isEmpty).isTrue
    }
  }

  @Test
  fun `gets all available below max`() {

    val channel = listOf(1, 2, 3).toChannel()
    val workInput = channel.toWorkInput()
    runBlocking {
      val taken = workInput.take(5)
      assertThat(taken).isEqualTo(listOf(1, 2, 3))
      assertThat(channel.isEmpty).isTrue
    }
  }

  @Test
  fun `gets all available above max`() {
    val channel = listOf(1, 2, 3, 4, 5).toChannel()
    val workInput = channel.toWorkInput()
    runBlocking {
      val taken = workInput.take(4)
      assertThat(taken).isEqualTo(listOf(1, 2, 3, 4))
      assertThat(channel.isEmpty).isFalse
    }
  }

  @Test
  fun `blocks until available`() {
    val channel = emptyList<Int>().toChannel()
    val workInput = channel.toWorkInput()
    runBlocking {
      val consumerThreadStarted = AtomicBoolean(false)

      val consumerJob = async {
        consumerThreadStarted.set(true)
        workInput.take(1)
      }

      while (!consumerThreadStarted.get()) {
        delay(100L)
      }
      channel.send(1)
      val taken = consumerJob.await()

      assertThat(taken).isEqualTo(listOf(1))
      assertThat(channel.isEmpty).isTrue

    }
  }

  private fun <T> List<T>.toChannel() = Channel<T>(Channel.UNLIMITED).apply {
    forEach { t ->
      this.trySend(t)
    }
  }

}