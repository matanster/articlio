package com.articlio.util

object Time {
  // gets the current server time  
  def localNow = new java.sql.Timestamp(java.util.Calendar.getInstance.getTime.getTime) // follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
                                                                                        // TODO: need to switch to UTC time for production
}