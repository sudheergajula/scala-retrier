# scala-retrier

Scala Based retry 
```
val policy = RetryPolicy("Immediately", 5, 2.second,
      retryStrategy = FixedDelayBackOffStrategy,
      allowException = AllowedExceptions,
      printStackTraceForThrowables = true,
      exceptionProcessor = isARetryableException)
``` 


```
def test(): Unit = {
      if (i % 2 != 0) {
        j += 1
        throw new IllegalArgumentException()
      }
    }


    val handler = new RetryHandler(policy)
    handler.retry(test())
``` 
      
