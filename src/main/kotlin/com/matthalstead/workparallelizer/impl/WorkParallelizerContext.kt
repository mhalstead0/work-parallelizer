package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.WorkDefinition
import com.matthalstead.workparallelizer.WorkParallelizerConfig
import com.matthalstead.workparallelizer.stats.StatsTracker
import java.util.concurrent.Executor

class WorkParallelizerContext<I, O>(
  val workDefinition: WorkDefinition<I, O>,
  val config: WorkParallelizerConfig,
  val executor: Executor,
  val statsTracker: StatsTracker
)