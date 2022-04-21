import kotlinx.coroutines.*
import java.io.File

fun main() {
    val repository = Repository()

    //Currently map of Code=Pair(Name,Depts) but maybe should be 2 maps Code=Name, Code=Depts?
    //Or just an object
    val schoolDeptsMap = runBlocking { getDepts(repository) }

    val worthySchools =
        schoolDeptsMap.filterKeys {
            listOf("01")
//            listOf("01","04","07","10","11","14")
                .contains(it)
        }

    makeCSVs(repository,worthySchools,listOf("198"))
}

fun makeCSVs(
    repository: Repository,
    schoolDeptsMap:  Map<String, Pair<String, List<String>>>,
    depts: List<String> = emptyList()
){
    schoolDeptsMap.forEach { (school, value) ->
        depts.ifEmpty { value.second }.forEach dept@{
            //Wanted to have "launch" here for async but that breaks Rutgers servers
            val entries = runBlocking { getDeptEntries(repository,school, it) }
            val csv = parseEntriesToCSVString(entries) ?: return@dept

            val file = File("res/test/${school}/${it}m.txt")
            file.parentFile.mkdirs()
            file.writeText("Professor;Rating;Total Responses\n$csv")
        }
    }
}

fun parseEntriesToCSVString(entries: List<Entry>): String? {
    val names = entries.map { formatName(it.instructor) }
    val mapOfProfs = entries.groupBy { parseName(it.instructor, names) }
        .filterKeys { it.isNotEmpty() && it != "TA" }

    val profRatings = mapOfProfs.filter { it.value.isNotEmpty() }
        .mapValues { (k, v) ->
            v.map { i ->
                i.scores.chunked(10)//grouped by question
                    .map {
                        it.flatMapIndexed { index, k ->
                            if (index in 0..4)
                                List(k.toInt()) { index + 1 }
                            else
                                emptyList()
                        }
                    }//maps to all answers as list
                    //ex. 2 5s and 3 4s gives [5,5,4,4,4]
                    //this allows for keeping total # of responses and average calculation after flattening
            }
            .flatMap { it.withIndex() }
            .groupBy({ it.index }, { it.value }).values
            .map { it.flatten() }
        }

    if (profRatings.isEmpty())
        return null

    val profAves = profRatings.map {
        val row = it.value[8]//This is the teaching effectiveness question
        Pair(it.key,Pair(row.average().roundToDecimal(2),row.size))
    }

    val deptAve = profAves.map { it.second.first }.average().roundToDecimal(2)
    val totalNum = profAves.sumOf { it.second.second }

    return (profAves + Pair("Average", Pair(deptAve,totalNum)))
        .sortedBy { -it.second.first }
        .joinToString("\n") { "${it.first};${it.second.first};${it.second.second}" }
}

fun formatName(name: String): String{
    return name.replace(Regex(" \\(.*\\)|[,]"),"")//removes stuff in parentheses & removes commas
        .split(" ")
        .run {
            get(0) + (getOrNull(1)?.let { ", ${it.first()}" } ?: "")//Adds first initial if present
        }.uppercase()
}

//This exists so that "Smith" and "Smith, John" are grouped together IFF John is the only Smith in the department
fun parseName(name: String, names: List<String>): String{
    with(formatName(name)){
        if(contains(','))
            return this

        val filtered = names.filter {
            val split = it.split(',')
            split[0]==this && split.size>1
        }.toSet()

        return if (filtered.size==1) filtered.first() else this
    }
}

suspend fun getDeptEntries(
    repository: Repository,
    school: String,
    dept: String,
    semesters: List<Int> = (4028..4043).toList()
): List<Entry>{
    return semesters.pmap { i ->
            repository.getByDeptOrCourse(if (i%2==0) "Spring" else "Fall", i/2, school, dept)
                .split("\t\t<strong>  ").drop(1)
                .map(::Entry).filter { it.scores.size==100 }
        }.flatten()
}

suspend fun getDepts(
    repository: Repository,
    semester: String = "Fall",
    year: Int = 2021
): Map<String, Pair<String, List<String>>>{
    return repository.getSchoolsOrDepts(semester, year)
        .substringAfterBefore("\"schools\":[[","]]").split("],[")
        .pmap {
            val (code,school) = it.removeSurrounding("\"").split("\",\"")
                .zipWithNext().first()//Fancy (read: bad) way of doing Pair(x[0],x[1])
            val depts = repository.getSchoolsOrDepts(semester, year, code)
                .substringAfterBefore("\"depts\":[\"", "\"]}").split("\",\"")
            Pair(code,Pair(school,depts))
        }.toMap()
}