package com.matthalstead.workparallelizer.utils

import com.matthalstead.workparallelizer.WorkInputBlocking
import com.matthalstead.workparallelizer.WorkInputSuspending
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

object WorkInputUtils {
  fun <T> LinkedBlockingQueue<T>.toWorkInput(): WorkInputBlocking<T> = WorkInputBlocking_BackedByLBQ(this)
  fun <T> Channel<T>.toWorkInput(): WorkInputSuspending<T> = WorkInputSuspending_BackedByChannel(this)
}

private class WorkInputBlocking_BackedByLBQ<T>(
  private val blockingQueue: BlockingQueue<T>
): WorkInputBlocking<T> {
  override fun takeBlocking(maxCount: Int): List<T> {
    val first = blockingQueue.take()

    val result = mutableListOf(first)
    var remainingToTake = maxCount - 1
    while (remainingToTake > 0) {
      val next = blockingQueue.poll()
      if (next == null) {
        remainingToTake = 0
      } else {
        result.add(next)
        remainingToTake--
      }
    }
    return result.toList()
  }

}

private class WorkInputSuspending_BackedByChannel<T>(
  private val channel: Channel<T>
): WorkInputSuspending<T> {

  override suspend fun take(maxCount: Int): List<T> {
    val first = channel.receive()

    val result = mutableListOf(first)
    var remainingToTake = maxCount - 1
    while (remainingToTake > 0) {
      val nextChannelResult = channel.tryReceive()
      if (nextChannelResult.isSuccess) {
        val next = nextChannelResult.getOrNull()
        if (next == null) {
          remainingToTake = 0
        } else {
          result.add(next)
          remainingToTake--
        }
      } else {
        remainingToTake = 0
      }
    }
    return result.toList()

  }


}