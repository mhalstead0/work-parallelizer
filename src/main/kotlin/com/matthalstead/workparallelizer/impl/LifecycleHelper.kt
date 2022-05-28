package com.matthalstead.workparallelizer.impl

import com.matthalstead.workparallelizer.StartedTwiceException
import java.util.concurrent.atomic.AtomicBoolean

class LifecycleHelper {
  private val started = AtomicBoolean(false)
  private val killed = AtomicBoolean(false)

  fun start() {
    if (!started.compareAndSet(false, true)) {
      throw StartedTwiceException()
    }
  }

  fun kill() {
    killed.set(true)
  }

  fun isKilled() = killed.get()

  fun repeatUntilKilled(f: () -> Unit) {
    while (!isKilled()) {
      f()
    }
  }

}