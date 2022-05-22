package com.matthalstead.workparallelizer.utils

import org.junit.jupiter.api.Test
import java.security.MessageDigest

object Hasher {

  fun hash(str: String): ByteArray {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return messageDigest.digest(str.toByteArray())
  }

}
