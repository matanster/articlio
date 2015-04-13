package com.articlio.util
import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.util.{Success, Failure, Try}

/*
 *  One or more implicit enhancements to the scala's Future class
 */

object FutureAdditions {
  implicit class FutureAdditions[T](future: Future[T]) {
    
    // 
    // Reverses the completion status of a future - from failure ( = exception) to success, and vice versa. Useful for test functions.
    //
    def reverse[S](implicit executor: ExecutionContext): Future[Unit] = {
      val p = Promise[Unit]()
      future.onComplete {
        // reverse the result of the future
        case Success(r) => p.failure(new Throwable(s"should not have received result (received result: $r)")) 
        case Failure(t) => p.success(Unit)              
      }
      p.future
    }
  }
}
