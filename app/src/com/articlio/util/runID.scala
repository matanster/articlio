package com.articlio.util
import scala.sys.process._
import java.net.InetAddress._ //{getLocalHost, getHostName}
import java.net.UnknownHostException
import java.util.Calendar
import java.sql

object hostname {
  val name1 = "hostname".!!.dropRight(1)
  val name2 = getLocalHost.getHostName
  
  val name = name1
  
  if (name1 != name2)
    println(s"apis disagree about server name: hostname per OS command is $name1, whereas hostname per networking api is $name2")
  else
    println(s"running on server $name")
}

class runID {
  val time = new java.sql.Timestamp(Calendar.getInstance.getTime.getTime) // this follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
                                                                          // TODO: need to switch to UTC time for production
  val id = hostname.name + "-" + time 
}