package com.matthalstead.workparallelizer.utils

import java.security.MessageDigest

object Hasher {

  fun hash(str: String): ByteArray {
    return hash(str.toByteArray())
  }

  fun hash(byteArray: ByteArray): ByteArray {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return messageDigest.digest(byteArray)
  }

}
