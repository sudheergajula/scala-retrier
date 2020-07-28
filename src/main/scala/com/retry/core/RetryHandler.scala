package com.retry.core

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.util.concurrent.ThreadLocalRandom

import scala.util.control.NonFatal

class RetryHandler(policy: RetryPolicy) {
  private var retryAttempt: Int = 0
  private var delay: Long = 0

  def setDelay(delay: Long): Unit = this.delay = delay

  def getDelay: Long = delay

  private def shouldRetry: Boolean =
    retryAttempt < this.policy.maxAttempts

  private def isAllowedEx(exception: Throwable): Boolean =
    this.policy.allowException(exception.getClass)

  def incrementAttempts(): Unit = (retryAttempt = retryAttempt + 1)


  private def computeDelayBeforeNextRetry(attempt: Int, policy: RetryPolicy): Long = {
    val nextDelay = policy.retryStrategy.calculateDelay(
      attempt, policy.retryDuration.toMillis) +
      jitter(policy.jitter.toMillis)
    setDelay(nextDelay)
    nextDelay
  }

  private def jitter(maxMills: Long): Long =
    if (maxMills == 0) 0 else (ThreadLocalRandom.current().nextDouble() * maxMills).toLong


  def retry[T](task: => T): T = {
    val result = null.asInstanceOf[T]
    while (true) {
      try {
        return task
      } catch {
        case NonFatal(exception: Throwable) =>
          if (shouldRetry && isAllowedEx(exception)) {
            val context = RetryPolicyContext(retryAttempt + 1, exception)
            policy.onFailure(context)
            policy.exceptionProcessor(exception)


            if (policy.printStackTraceForThrowables)
              printStackTrace(exception)

            policy.onRetry(context)
            incrementAttempts()
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