package com.articlio.cache

/*
 *  Riak is not recommended (performance-wise) for files over 50MB, so not the most perfect fit
 *  for caching e.g. a pdf file. Although, I have successfully used it from node.js for files well over 50MB
 *  for a long while.
 */

@deprecated("see comment in source file", "not recommended for large files")
object ScalaRiakClient extends ExceptionDetail {
  import com.basho.riak.client
  import com.basho.riak.client.api._
  import com.basho.riak.client.core.query._
  import com.basho.riak.client.core.util._
  import com.basho.riak.client.api.commands.kv.StoreValue
  import com.basho.riak.client.api.commands.kv.StoreValue.Option
  import com.basho.riak.client.api.cap.Quorum
  
  def simpleStore(bucket: String, key: String, value: String): Boolean = {
    val location = new Location(new Namespace("default", bucket), key)
    val riakObject = new RiakObject().setValue(BinaryValue.create(value))
    val store = new StoreValue.Builder(riakObject).withLocation(location).withOption(Option.W, new Quorum(3)).build();
    val client = RiakClient.newClient
    
    try { client.execute(store) } 
    catch {
      case anyException : Throwable =>
      println(s"Storing to riak (bucket $bucket, key $key) failed with riak java cliet exception")
      getExceptionDetail(anyException)
      false 
    }
    true
    
  }
}