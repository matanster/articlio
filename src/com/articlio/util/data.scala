package com.articlio.util

//
// Experimental class for basic descriptive statistics for a sequence of numbers, and my own relevant quantifiers
//
class Descriptive(vals: Seq[java.lang.Number], title:String) {
  import org.apache.commons.math3.stat.descriptive.{DescriptiveStatistics} // for using descriptive statistics over collections    
  var descriptive = new DescriptiveStatistics                              // NOT thread safe (http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/stat/descriptive/DescriptiveStatistics.html#addValue(double))
  vals.foreach(value => descriptive.addValue(value.doubleValue))
  
  // Some descriptive statistics from http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/stat/descriptive/DescriptiveStatistics.html
  val average  = descriptive.getMean
  val std      = descriptive.getStandardDeviation
  val variance = descriptive.getVariance
  val zerosP   = (vals.count(_ == 0).doubleValue / vals.length.doubleValue)

  def allApacheCommons: String = descriptive.toString 

  def basic = {
    val printables = Seq(("average", average),
                         ("variance", variance),
                         ("std", std),
                         ("% zero", zerosP))
    val maxLen = (printables map (p => p._1.length)).max
    printables.foreach(p => println(p._1 + ':' + (" " * (1 + maxLen - p._1.length)) + p._2.toString)) // padded printout of each statistic
  }

  def input = {
    println(vals.mkString(" "))
  }

  def all = { 
    println()
    println(title)
    input 
    basic
    println()
  }

}
