package com.matthalstead.workparallelizer.test

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

object TestUtils {

  fun retryUntilTrueOrTimeout_Blocking(
    maxDuration: Duration,
    pauseTime: Duration = Duration.ofMillis(100L),
    f: () -> Boolean
  ): RetryUntilTrueOrTimeoutResult {
    val deadline = Instant.now().plus(maxDuration)
    val pauseTimeMillis = pauseTime.toMillis().coerceAtLeast(1L)

    while (true) {
      if (f()) return ReturnedTrue

      val millisLeft = Instant.now().until(deadline, ChronoUnit.MILLIS)
      if (millisLeft <= 0L) return TimedOut

      val thisPauseTimeMillis = minOf(pauseTimeMillis, millisLeft)
      Thread.sleep(thisPauseTimeMillis)
    }
  }

  sealed class RetryUntilTrueOrTimeoutResult
  object ReturnedTrue : RetryUntilTrueOrTimeoutResult()
  object TimedOut : RetryUntilTrueOrTimeoutResult()



}