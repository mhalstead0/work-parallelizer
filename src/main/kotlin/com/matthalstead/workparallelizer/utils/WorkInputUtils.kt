package com.matthalstead.workparallelizer.utils

import com.matthalstead.workparallelizer.WorkInput
import java.util.concurrent.LinkedBlockingQueue

object WorkInputUtils {
  fun <T> LinkedBlockingQueue<T>.toWorkInput(): WorkInput<T> = WorkInput_BackedByLBQ(this)
}

private class WorkInput_BackedByLBQ<T>(
  private val linkedBlockingQueue: LinkedBlockingQueue<T>
): WorkInput<T> {
  override fun takeBlocking(maxCount: Int): List<T> {
    val first = linkedBlockingQueue.take()

    val result = mutableListOf(first)
    var remainingToTake = maxCount - 1
    while (remainingToTake > 0) {
      val next = linkedBlockingQueue.poll()
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