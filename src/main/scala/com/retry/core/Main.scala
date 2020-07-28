package com.retry.core

import java.io.FileNotFoundException

import scala.concurrent.duration._


object Main {

  def main(args: Array[String]): Unit = {

    val AllowedExceptions: Set[Class[_ <: Throwable]] =
      Set(
        classOf[IllegalAccessException],
        classOf[IllegalAccessError],
        classOf[ArithmeticException],
        classOf[ArrayIndexOutOfBoundsException],
        classOf[FileNotFoundException],
        classOf[ClassCastException])

    def doSomething(e: Throwable) = true

    def isARetryableException(throwable: Throwable): Boolean = {
      throwable match {
        case e: IllegalArgumentException =>
          println("Match")
          doSomething(e)

        case e: FileNotFoundException =>
          println("Match")
          doSomething(e)

        case cause: Throwable =>
          println(
            s"An unknown cause exception: $cause is thrown. " +
              s"Will not be retrying.",
            cause)
          false
      }
    }

    val policy = RetryPolicy("Immediately", 10, 2.second,
      retryStrategy = FixedDelayBackOffStrategy,
      allowException = AllowedExceptions,
      printStackTraceForThrowables = true,
      exceptionProcessor = isARetryableException)



    val i: Int = 3
    var j: Int = 1

    def test(): Unit = {
      if (i % 2 != 0) {
        j += 1
        throw new FileNotFoundException()
      }
    }

    //    val result: String = RetryHandler().withPolicy(policy).retry {
    //      if (i % 2 != 0) {
    //        j += 1
    //        throw new Exception()
    //      }
    //      "Hello World"
    //    }

    val handler = new RetryHandler(policy)
    handler.retry(test())

  }

}
