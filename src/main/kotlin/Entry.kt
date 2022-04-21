class Entry(s : String){
    val instructor : String = s.substringBefore("  ")
    val term = s.substringAfterBefore("<br> ","\n")
    val code = s.substringAfterBefore("<br>  "," ")
    val courseName = s.substringAfterBefore("<q>","<")
    //    val indexNum = s.substringAfterBefore("#",")")
    val enrolled = s.substringAfterBefore("Enrollment=  ",",").toInt()
    val responses = s.substringAfterBefore("Responses= "," ").toInt()
    val scores = s.split("<td  class=\"mono").drop(1)
        .map {
            it.substringAfterBefore(">","<").toDouble()
        }//indices 0-99 are all the numbers for one entry, row by row

//    fun shortTerm(): String{
//        return term.first()+term.takeLast(2)
//    }
}