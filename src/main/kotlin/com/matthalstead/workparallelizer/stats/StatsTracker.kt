package com.matthalstead.workparallelizer.stats

import com.matthalstead.workparallelizer.WorkParallelizerStats
import java.util.concurrent.atomic.AtomicLong

class StatsTracker: WorkParallelizerStats {

  private val totalRunCount = AtomicLong(0L)
  private val totalSuccessCount = AtomicLong(0L)

  fun <T> run(f: () -> T): T {
    try {
      val t = f()
      totalSuccessCount.incrementAndGet()
      return t
    } finally {
      totalRunCount.incrementAndGet()
    }
  }

  override fun getTotalProcessedCount(): Long = totalRunCount.get()
  override fun getTotalSuccessCount(): Long = totalSuccessCount.get()
}