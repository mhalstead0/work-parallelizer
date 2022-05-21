package com.matthalstead.workparallelizer

open class WorkParallelizerException: Exception {
  constructor(message: String? = null, cause: Throwable? = null) : super(message, cause)
}

class ConfigException(message: String): WorkParallelizerException(message)
class StartedTwiceException: WorkParallelizerException()
class InputException(message: String? = null, cause: Throwable? = null) : WorkParallelizerException(message, cause)
class TransformException(message: String, cause: Throwable?) : WorkParallelizerException(message, cause)
class OutputException(message: String, cause: Throwable?) : WorkParallelizerException(message, cause)