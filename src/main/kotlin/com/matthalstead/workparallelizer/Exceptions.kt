package com.matthalstead.workparallelizer

open class WorkParallelizerException: Exception {
  constructor(message: String?, cause: Throwable?) : super(message, cause)
}

class InputException(message: String, cause: Throwable?) : WorkParallelizerException(message, cause)
class TransformException(message: String, cause: Throwable?) : WorkParallelizerException(message, cause)
class OutputException(message: String, cause: Throwable?) : WorkParallelizerException(message, cause)