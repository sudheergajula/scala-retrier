package com.retry.core

import scala.concurrent.duration.Duration.Zero
import scala.concurrent.duration._


case class RetryPolicy(name: String,
                       maxAttempts: Int,
                       retryDuration: FiniteDuration,
                       retryStrategy: BackOffStrategy = FixedDelayBackOffStrategy,
                       jitter: FiniteDuration = 0.second,
                       onRetry: RetryPolicyContext => Unit = _ => (),
                       onFailure: RetryPolicyContext => Unit = _ => (),
                       allowException: Class[_ <: Throwable] => Boolean,
                       exceptionProcessor: Throwable => Boolean = _ => true,
                       printStackTraceForThrowables: Boolean = false)

case class RetryPolicyContext(retriesAttempted: Int, exception: Throwable)

object RetryPolicy {
  val None = RetryPolicy("None", 0, Zero, FixedDelayBackOffStrategy, allowException = Set(classOf[Exception]))

  def Immediately(attempts: Int): RetryPolicy = RetryPolicy("Immediately", attempts, Zero, FixedDelayBackOffStrategy,
    allowException = Set(classOf[Exception]), printStackTraceForThrowables = true, exceptionProcessor = isARetryableException)

  def doSomething(e: IllegalArgumentException) = true

  def isARetryableException(throwable: Throwable): Boolean = {
    throwable match {
      case e: IllegalArgumentException =>
        doSomething(e)
    }
  }
}
