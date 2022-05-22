package com.matthalstead.workparallelizer.test

import java.util.concurrent.atomic.AtomicInteger


class ConcurrentCallTracker {
  private val concurrentCalls = AtomicInteger(0)
  private val minMaxTracker = MinMaxTracker(0)

  fun<T> track(f: () -> T): T {
    val newConcurrentCalls = concurrentCalls.incrementAndGet()
    minMaxTracker.track(newConcurrentCalls)
    try {
      return f()
    } finally {
      concurrentCalls.decrementAndGet()
    }
  }

  suspend fun<T> trackSuspending(f: suspend () -> T): T {
    val newConcurrentCalls = concurrentCalls.incrementAndGet()
    minMaxTracker.track(newConcurrentCalls)
    try {
      return f()
    } finally {
      concurrentCalls.decrementAndGet()
    }
  }

  fun getMaxConcurrency() = minMaxTracker.getMax() ?: 0

}