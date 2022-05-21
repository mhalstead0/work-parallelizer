package com.matthalstead.workparallelizer.utils

import com.matthalstead.workparallelizer.WorkInput
import com.matthalstead.workparallelizer.WorkInputBlocking
import java.util.concurrent.LinkedBlockingQueue

object WorkInputUtils {
  fun <T> LinkedBlockingQueue<T>.toWorkInput(): WorkInputBlocking<T> = WorkInputBlocking_BackedByLBQ(this)
}

private class WorkInputBlocking_BackedByLBQ<T>(
  private val linkedBlockingQueue: LinkedBlockingQueue<T>
): WorkInputBlocking<T> {
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