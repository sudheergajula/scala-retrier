package com.retry.core

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.util.concurrent.ThreadLocalRandom

import scala.util.control.NonFatal

class RetryHandler(policy: RetryPolicy) {
  private var retryAttempt: Int = 0
  private var delay: Long = 0


  private def shouldRetry(): Boolean = {
    this.policy.maxAttempts < retryAttempt
  }

  private def isAllowedEx(exception: Throwable): Boolean = {
    this.policy.allowException(exception.getClass)
  }

  def setDelay(delay: Long): Unit = {
    this.delay = delay
  }

  def getDelay: Long = delay


  private def computeDelayBeforeNextRetry(attempt: Int, policy: RetryPolicy): Long = {
    val nextDelay = policy.retryStrategy.calculateDelay(attempt, policy.retryDuration.toMillis) + jitter(policy.jitter.toMillis)
    setDelay(nextDelay)
    nextDelay
  }

  private def jitter(maxMills: Long): Long = {
    if (maxMills == 0) 0 else (ThreadLocalRandom.current().nextDouble() * maxMills).toLong
  }

  def retry[T](f: => T): T = {
    val result = null.asInstanceOf[T]
    while (true) {
      try {
        return f
      } catch {
        case NonFatal(exception: Throwable) =>
          if (policy.allowException(exception.getClass)) {
            val context = RetryPolicyContext(retryAttempt + 1, exception)

            if (shouldRetry() && isAllowedEx(exception)) {
              policy.onFailure(context)
              policy.exceptionProcessor(exception)
            }

            if (policy.printStackTraceForThrowables)
              printStackTrace(exception)

            policy.onRetry(context)
            retryAttempt = retryAttempt + 1
            println(s"wait $getDelay ms for next retry, retried $retryAttempt attempt(s).")
            Thread.sleep(computeDelayBeforeNextRetry(retryAttempt, policy))
          } else {
            val traceAsString = printStackTrace(exception)
            println(s"Caught disallowed throwable. " +
              s"(retrierName: ${policy.name}) " +
              s"$traceAsString. Rethrowing...")
            throw exception
          }
      }
    }
    result
  }

  private def printStackTrace(exception: Throwable): String = {
    val stream = new ByteArrayOutputStream()
    val writer = new PrintWriter(stream)
    exception.printStackTrace(writer)
    writer.close()
    stream.close()
    s"stackTrace:\n${stream.toString()}"
  }
}