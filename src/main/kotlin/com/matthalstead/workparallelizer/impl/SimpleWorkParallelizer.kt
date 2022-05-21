package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.WorkParallelizer
import com.matthalstead.workparallelizer.WorkParallelizerStats
import com.matthalstead.workparallelizer.stats.StatsTracker

class SimpleWorkParallelizer<I, O>(
  private val workParallelizerContext: WorkParallelizerContext<I, O>
) : WorkParallelizer {
  override val stats: WorkParallelizerStats = workParallelizerContext.statsTracker

  override fun start() {
    TODO("Not yet implemented")
  }

  override fun destroy() {
    TODO("Not yet implemented")
  }

}