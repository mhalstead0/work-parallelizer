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
    val result = linkedBlockingQueue.take(maxCount)
    return result.ifEmpty {
      listOf(linkedBlockingQueue.take())
    }
  }

}