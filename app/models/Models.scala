package models

case class Match1(docName: String,
                    runID   : String,
                    sentence: String,
                    matchPattern: String,
                    locationActual: String,
                    locationTest: String,
                    isFinalMatch: Boolean,
                    matchIndication: String) { 
  }

