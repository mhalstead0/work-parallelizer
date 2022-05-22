package com.matthalstead.workparallelizer.test

import java.util.concurrent.atomic.AtomicReference

class MinMaxTracker<T: Comparable<T>>(initialValue: T? = null) {

  private val minRef = AtomicReference<T>(initialValue)
  private val maxRef = AtomicReference<T>(initialValue)

  fun track(value: T) {
    track(minRef, MinOrMax.MIN, value)
    track(maxRef, MinOrMax.MAX, value)
  }

  private fun track(ref: AtomicReference<T>, minOrMax: MinOrMax, value: T) {
    while (true) {
      val oldValue: T? = ref.get()
      val newIsBetter =
        if (oldValue == null) {
          true
        } else {
          when (minOrMax) {
            MinOrMax.MIN -> (value < oldValue)
            MinOrMax.MAX -> (value > oldValue)
          }
        }
      if (newIsBetter) {
        val casResult = ref.compareAndSet(oldValue, value)
        if (casResult) {
          return
        }
      } else {
        return
      }
    }
  }

  private enum class MinOrMax { MIN, MAX }

  fun getMin(): T? = minRef.get()
  fun getMax(): T? = maxRef.get()

}
