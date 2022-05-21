package com.matthalstead.workparallelizer


interface WorkParallelizerStats {
  fun getTotalProcessedCount(): Long
  fun getTotalSuccessCount(): Long

//TODO Add step stats:  fun getStepStats(stepType: StepType): StepStats
}

//enum class StepType {
//  INPUT,
//  TRANSFORM,
//  OUTPUT
//}
//
//data class StepStats(
//  val count: Long,
//  val totalDuration: Duration,
//  val averageDuration: Duration,
//  val maxDuration: Duration
//)