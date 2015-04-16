package com.articlio.test

/*
 *  simple class for memoizing values, currently not tested & not in use.
 */

class ResultCache { // there can be a more perfect and convoluted implementation (http://stackoverflow.com/questions/25129721/scala-memoization-how-does-this-scala-memo-work)

  val memoized : scala.collection.concurrent.Map[String, Any] = 
    scala.collection.concurrent.TrieMap.empty[String, Any] 
  
  def put(key: String, value: Any) = {
    memoized.put(key, value)
  }
  
  def get(key: String) = {
    memoized.get(key)
  }
}

