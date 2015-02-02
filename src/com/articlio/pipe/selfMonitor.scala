package com.articlio.pipe
import com.articlio.util.{Console}

//
// Monitor own-JVM memory usage
//
object Monitor {

  // get the hook to memory consumption
  import runtime.{ totalMemory, freeMemory, maxMemory, gc } // http://stackoverflow.com/questions/3571203/what-is-the-exact-meaning-of-runtime-getruntime-totalmemory-and-freememory
  private val runtime = Runtime.getRuntime()

  def percentThreshold = 10
  def interval = 1000
  val timer = new java.util.Timer("selfMonitor") // give the timer thread a name for ops-friendliness

  var former = getMem
  var current = former

  def getMem: Map[String, Float] = {
    val heapUsed  = totalMemory - freeMemory
    val heapTotal = totalMemory
    Map("heapUsed" -> heapUsed,
        "heapTotal" -> heapTotal,
        "heapPercent" -> heapUsed.toFloat / heapTotal * 100)
  }
  
  def logUsage (verb: String) {
    Console.log(f"JVM Heap usage ${verb} ${current("heapPercent")}%.1f" + "%" + f" of JVM heap (${current("heapUsed")/1024/1024}%.0fMB of ${current("heapTotal")/1024/1024}%.0fMB)", "performance")
  }

  def logUsageIfChanged {

    current = getMem

    if (math.abs(current("heapPercent") - former("heapPercent")) / former("heapPercent") > (percentThreshold.toFloat/100)) {    
      if (current("heapPercent") > former("heapPercent")) {
        logUsage("increased to")
      }
      else {
        logUsage("decreased to")        
      }
      former = current
    }

  }

  // Garbage-collect right before logging usage. JVM may or may not actually garbage collect here. 
  // This is just a request to the JVM which depending on various factors (JVM type, JVM params) may not 
  // result in a garbage collection. 
  def logUsageAfterGC (verb: String) {
    gc
    logUsage(verb)
  }

  def shutdown {
    logUsage("before shutting down self-monitoring is")
    timer.cancel
  }

  def start { /* keep function name for vert.x compatibility */
    println("starting self-monitoring...")

    val maxHeapLimit = maxMemory // This is determined by the JVM runtime according to the XMX value, but does not precisely equal it (http://stackoverflow.com/questions/13729652/runtime-maxmemory-and-xmx)
    Console.log(f"JVM Max Heap Allocation Limit (determined mostly by its XMX) is ${maxHeapLimit/1024/1024}%.0fMB", "performance")

    logUsageAfterGC("is") // This initial garbage collections can dip way beyond the JVM Xms startup allocation value -
                          // thus providing a better sense of application memory usage during development. Indeed, with 
                          // Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_51 under SBT, this significantly reduces
                          // heap size after SBT/Scala bootsrapped and this self-monitoring object started.
                          // As startup time is of little concern, this would probably not adversely affect production.


    logUsageIfChanged
    // under vertx, simply: val timer = vertx.setPeriodic(interval, { timerID: Long => logUsageIfChanged })
    val recur = new java.util.TimerTask { override def run = logUsageIfChanged }
    timer.schedule(recur, 0.toLong, interval.toLong)
  }

  start

}