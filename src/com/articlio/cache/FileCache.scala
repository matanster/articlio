package com.articlio.cache

import com.basho.riak.client.api.cap.Quorum

object ScalaRiakClient {
  import com.basho.riak.client
  import com.basho.riak.client.api._
  //import com.basho.riak.client.api.RiakClient
  import com.basho.riak.client.core.query._
  import com.basho.riak.client.core.util._
  import com.basho.riak.client.api.commands.kv.StoreValue
  import com.basho.riak.client.api.commands.kv.StoreValue.Option
  val ns = new Namespace("default", "my_bucket")
  val location = new Location(ns, "my_key")
  val riakObject = new RiakObject()
  riakObject.setValue(BinaryValue.create("my_value"));
  val store = new StoreValue.Builder(riakObject).withLocation(location).withOption(Option.W, new Quorum(3)).build();
  val client = RiakClient.newClient
  client.execute(store);
}

class FileCache {
  
  private def filePathLocallyExists(filePath: String) : Boolean = {
    import java.nio.file.{Paths, Files}
    Files.exists(Paths.get(filePath))
  }

  def getFile(path: String) : Boolean = {
    filePathLocallyExists(path) match {
      case true  => true
      case false => true 
    }
  }
}