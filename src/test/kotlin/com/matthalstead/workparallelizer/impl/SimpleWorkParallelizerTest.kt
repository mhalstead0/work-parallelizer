package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.WorkDefinition
import com.matthalstead.workparallelizer.WorkParallelizerConfig
import com.matthalstead.workparallelizer.stats.StatsTracker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue

class SimpleWorkParallelizerTest {

  private lateinit var simpleWorkParallelizer: SimpleWorkParallelizer<Int, String>
  private lateinit var inputQueue: LinkedBlockingQueue<Int>
  private lateinit var outputQueue: LinkedBlockingQueue<String>


  @BeforeEach
  fun doSetup() {
//    val workDefinition = WorkDefinition<Int, String>(
//      input =
//    )
//    val context = WorkParallelizerContext(
//      val workDefinition: WorkDefinition<I, O>,
//    val config: WorkParallelizerConfig,
//    val executor: Executor,
//    val statsTracker: StatsTracker
//
//    )
  }

  @AfterEach
  fun doTearDown() {

  }

  @Test
  fun `something here`() {
    fail(message = "x")
  }
}