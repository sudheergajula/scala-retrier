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

    def doSomething(e: IllegalArgumentException) = true

    def isARetryableException(throwable: Throwable): Boolean = {
      throwable match {
        case e: IllegalArgumentException =>
          println("Match")
          doSomething(e)
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
        throw new IllegalArgumentException()
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
