import io.ktor.client.call.*
import io.ktor.client.request.*

class Repository {
    suspend fun getByDeptOrCourse(
        semester: String,
        year: Int,
        school: String,
        dept: String,
        course: String = ""
    ): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[semester]",semester)
            parameter("survey[year]",year)
            parameter("survey[school]",school)
            parameter("survey[dept]",dept)
            parameter("survey[course]",course)
            parameter("mode","course")
        }.body()
    }

    suspend fun getByLastName(lastname: String): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[lastname]",lastname)
            parameter("mode","name")
        }.body()
    }

    suspend fun getByID(id: String): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[record]",id)
            parameter("mode","name")
        }.body()
    }

    suspend fun getSchoolsOrDepts(semester: String, year: Int, school: String = ""): String{
        return client.get("https://sirs.ctaar.rutgers.edu/courseFilter.php"){
            parameter("survey[semester]",semester)
            parameter("survey[year]",year)
            parameter("survey[school]",school)
            parameter("mode","course")
        }.body()
    }
}