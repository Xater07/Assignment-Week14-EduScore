package com.example.data

import android.content.Context
import androidx.room.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// === DATA CLASSES / ROOM ENTITIES ===

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val email: String,
    val password: String,
    val role: String // "Admin" or "Lecturer"
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val studentId: String,
    val studentName: String,
    val programme: String,
    val semesterStatus: String // "Active" or "Inactive"
)

@Entity(tableName = "academic_terms")
data class AcademicTerm(
    @PrimaryKey val termId: String,
    val termName: String,
    val startDate: String, // "yyyy-MM-dd"
    val endDate: String   // "yyyy-MM-dd"
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val courseId: String,
    val courseName: String,
    val creditHours: Int
)

@Entity(tableName = "course_enrollments")
data class CourseEnrollment(
    @PrimaryKey val enrollmentId: String,
    val studentId: String,
    val courseId: String,
    val termId: String
)

@Entity(tableName = "assessments")
data class Assessment(
    @PrimaryKey val assessmentId: String,
    val courseId: String,
    val assessmentType: String,
    val weightage: Float // e.g. 50.0f for 50%
)

@Entity(tableName = "grades")
data class GradeLedger(
    @PrimaryKey val gradeId: String,
    val studentId: String,
    val assessmentId: String,
    val rawScore: Float // 0 - 100
)

// === FIRESTORE DOCUMENT CONVERTERS ===

fun User.toMap() = hashMapOf(
    "userId" to userId,
    "email" to email,
    "password" to password,
    "role" to role
)

fun Student.toMap() = hashMapOf(
    "studentId" to studentId,
    "studentName" to studentName,
    "programme" to programme,
    "semesterStatus" to semesterStatus
)

fun AcademicTerm.toMap() = hashMapOf(
    "termId" to termId,
    "termName" to termName,
    "startDate" to startDate,
    "endDate" to endDate
)

fun Course.toMap() = hashMapOf(
    "courseId" to courseId,
    "courseName" to courseName,
    "creditHours" to creditHours
)

fun CourseEnrollment.toMap() = hashMapOf(
    "enrollmentId" to enrollmentId,
    "studentId" to studentId,
    "courseId" to courseId,
    "termId" to termId
)

fun Assessment.toMap() = hashMapOf(
    "assessmentId" to assessmentId,
    "courseId" to courseId,
    "assessmentType" to assessmentType,
    "weightage" to weightage
)

fun GradeLedger.toMap() = hashMapOf(
    "gradeId" to gradeId,
    "studentId" to studentId,
    "assessmentId" to assessmentId,
    "rawScore" to rawScore
)

fun Map<String, Any?>.toUser() = User(
    userId = this["userId"] as? String ?: "",
    email = this["email"] as? String ?: "",
    password = this["password"] as? String ?: "",
    role = this["role"] as? String ?: ""
)

fun Map<String, Any?>.toStudent() = Student(
    studentId = this["studentId"] as? String ?: "",
    studentName = this["studentName"] as? String ?: "",
    programme = this["programme"] as? String ?: "",
    semesterStatus = this["semesterStatus"] as? String ?: ""
)

fun Map<String, Any?>.toAcademicTerm() = AcademicTerm(
    termId = this["termId"] as? String ?: "",
    termName = this["termName"] as? String ?: "",
    startDate = this["startDate"] as? String ?: "",
    endDate = this["endDate"] as? String ?: ""
)

fun Map<String, Any?>.toCourse() = Course(
    courseId = this["courseId"] as? String ?: "",
    courseName = this["courseName"] as? String ?: "",
    creditHours = (this["creditHours"] as? Number)?.toInt() ?: 0
)

fun Map<String, Any?>.toCourseEnrollment() = CourseEnrollment(
    enrollmentId = this["enrollmentId"] as? String ?: "",
    studentId = this["studentId"] as? String ?: "",
    courseId = this["courseId"] as? String ?: "",
    termId = this["termId"] as? String ?: ""
)

fun Map<String, Any?>.toAssessment() = Assessment(
    assessmentId = this["assessmentId"] as? String ?: "",
    courseId = this["courseId"] as? String ?: "",
    assessmentType = this["assessmentType"] as? String ?: "",
    weightage = (this["weightage"] as? Number)?.toFloat() ?: 0.0f
)

fun Map<String, Any?>.toGradeLedger() = GradeLedger(
    gradeId = this["gradeId"] as? String ?: "",
    studentId = this["studentId"] as? String ?: "",
    assessmentId = this["assessmentId"] as? String ?: "",
    rawScore = (this["rawScore"] as? Number)?.toFloat() ?: 0.0f
)

// === ROOM DATABASE COMPONENTS ===

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Query("DELETE FROM users")
    fun clearUsers()

    // Students
    @Query("SELECT * FROM students")
    fun getAllStudents(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudent(student: Student)

    @Delete
    fun deleteStudent(student: Student)

    @Query("DELETE FROM students")
    fun clearStudents()

    // Academic Terms
    @Query("SELECT * FROM academic_terms")
    fun getAllTerms(): List<AcademicTerm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTerm(term: AcademicTerm)

    @Delete
    fun deleteTerm(term: AcademicTerm)

    @Query("DELETE FROM academic_terms")
    fun clearTerms()

    // Courses
    @Query("SELECT * FROM courses")
    fun getAllCourses(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(course: Course)

    @Delete
    fun deleteCourse(course: Course)

    @Query("DELETE FROM courses")
    fun clearCourses()

    // Enrollments
    @Query("SELECT * FROM course_enrollments")
    fun getAllEnrollments(): List<CourseEnrollment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEnrollment(enrollment: CourseEnrollment)

    @Delete
    fun deleteEnrollment(enrollment: CourseEnrollment)

    @Query("DELETE FROM course_enrollments")
    fun clearEnrollments()

    // Assessments
    @Query("SELECT * FROM assessments")
    fun getAllAssessments(): List<Assessment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssessment(assessment: Assessment)

    @Delete
    fun deleteAssessment(assessment: Assessment)

    @Query("DELETE FROM assessments")
    fun clearAssessments()

    // Grades
    @Query("SELECT * FROM grades")
    fun getAllGrades(): List<GradeLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGrade(grade: GradeLedger)

    @Delete
    fun deleteGrade(grade: GradeLedger)

    @Query("DELETE FROM grades")
    fun clearGrades()
}

@Database(
    entities = [
        User::class,
        Student::class,
        AcademicTerm::class,
        Course::class,
        CourseEnrollment::class,
        Assessment::class,
        GradeLedger::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

// === DB-BACKED MUTABLE LIST DELEGATE ===

class DbMutableList<T>(
    private val delegate: MutableList<T> = mutableListOf(),
    private val onInsert: (T) -> Unit,
    private val onDelete: (T) -> Unit
) : MutableList<T> by delegate {

    fun silentAddAll(elements: Collection<T>) {
        delegate.addAll(elements)
    }

    fun silentClear() {
        delegate.clear()
    }

    override fun add(element: T): Boolean {
        val result = delegate.add(element)
        if (result) {
            onInsert(element)
        }
        return result
    }

    override fun add(index: Int, element: T) {
        delegate.add(index, element)
        onInsert(element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = delegate.addAll(index, elements)
        if (result) {
            elements.forEach { onInsert(it) }
        }
        return result
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = delegate.addAll(elements)
        if (result) {
            elements.forEach { onInsert(it) }
        }
        return result
    }

    override fun remove(element: T): Boolean {
        val result = delegate.remove(element)
        if (result) {
            onDelete(element)
        }
        return result
    }

    override fun removeAt(index: Int): T {
        val element = delegate.removeAt(index)
        onDelete(element)
        return element
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var changed = false
        elements.forEach {
            if (delegate.remove(it)) {
                onDelete(it)
                changed = true
            }
        }
        return changed
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val toDelete = delegate.filter { it !in elements }
        val changed = delegate.retainAll(elements)
        if (changed) {
            toDelete.forEach { onDelete(it) }
        }
        return changed
    }

    override fun clear() {
        val old = ArrayList(delegate)
        delegate.clear()
        old.forEach { onDelete(it) }
    }

    override fun set(index: Int, element: T): T {
        val old = delegate.set(index, element)
        onDelete(old)
        onInsert(element)
        return old
    }

    override fun iterator(): MutableIterator<T> {
        val rootIterator = delegate.iterator()
        return object : MutableIterator<T> {
            private var lastReturned: T? = null

            override fun hasNext(): Boolean = rootIterator.hasNext()

            override fun next(): T {
                val element = rootIterator.next()
                lastReturned = element
                return element
            }

            override fun remove() {
                val element = lastReturned
                rootIterator.remove()
                if (element != null) {
                    onDelete(element)
                }
            }
        }
    }

    override fun listIterator(): MutableListIterator<T> {
        val rootIterator = delegate.listIterator()
        return object : MutableListIterator<T> {
            private var lastReturned: T? = null
            override fun hasNext(): Boolean = rootIterator.hasNext()
            override fun next(): T {
                val element = rootIterator.next()
                lastReturned = element
                return element
            }
            override fun hasPrevious(): Boolean = rootIterator.hasPrevious()
            override fun previous(): T {
                val element = rootIterator.previous()
                lastReturned = element
                return element
            }
            override fun nextIndex(): Int = rootIterator.nextIndex()
            override fun previousIndex(): Int = rootIterator.previousIndex()
            override fun remove() {
                val element = lastReturned
                rootIterator.remove()
                if (element != null) {
                    onDelete(element)
                }
            }
            override fun set(element: T) {
                val old = lastReturned
                rootIterator.set(element)
                if (old != null) {
                    onDelete(old)
                }
                onInsert(element)
            }
            override fun add(element: T) {
                rootIterator.add(element)
                onInsert(element)
            }
        }
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        val rootIterator = delegate.listIterator(index)
        return object : MutableListIterator<T> {
            private var lastReturned: T? = null
            override fun hasNext(): Boolean = rootIterator.hasNext()
            override fun next(): T {
                val element = rootIterator.next()
                lastReturned = element
                return element
            }
            override fun hasPrevious(): Boolean = rootIterator.hasPrevious()
            override fun previous(): T {
                val element = rootIterator.previous()
                lastReturned = element
                return element
            }
            override fun nextIndex(): Int = rootIterator.nextIndex()
            override fun previousIndex(): Int = rootIterator.previousIndex()
            override fun remove() {
                val element = lastReturned
                rootIterator.remove()
                if (element != null) {
                    onDelete(element)
                }
            }
            override fun set(element: T) {
                val old = lastReturned
                rootIterator.set(element)
                if (old != null) {
                    onDelete(old)
                }
                onInsert(element)
            }
            override fun add(element: T) {
                rootIterator.add(element)
                onInsert(element)
            }
        }
    }

}

// === SINGLETON APPDATA WITH FIREBASE SYNCHRONIZATION ===

object AppData {
    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private var isInitialized = false

    val users: MutableList<User> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertUser(it)
                writeToFirestore("users", it.userId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteUser(it)
                deleteFromFirestore("users", it.userId)
            }
        }
    )
    val students: MutableList<Student> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertStudent(it)
                writeToFirestore("students", it.studentId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteStudent(it)
                deleteFromFirestore("students", it.studentId)
            }
        }
    )
    val academicTerms: MutableList<AcademicTerm> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertTerm(it)
                writeToFirestore("academic_terms", it.termId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteTerm(it)
                deleteFromFirestore("academic_terms", it.termId)
            }
        }
    )
    val courses: MutableList<Course> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertCourse(it)
                writeToFirestore("courses", it.courseId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteCourse(it)
                deleteFromFirestore("courses", it.courseId)
            }
        }
    )
    val enrollments: MutableList<CourseEnrollment> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertEnrollment(it)
                writeToFirestore("course_enrollments", it.enrollmentId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteEnrollment(it)
                deleteFromFirestore("course_enrollments", it.enrollmentId)
            }
        }
    )
    val assessments: MutableList<Assessment> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertAssessment(it)
                writeToFirestore("assessments", it.assessmentId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteAssessment(it)
                deleteFromFirestore("assessments", it.assessmentId)
            }
        }
    )
    val grades: MutableList<GradeLedger> = DbMutableList(
        onInsert = {
            if (::dao.isInitialized) {
                dao.insertGrade(it)
                writeToFirestore("grades", it.gradeId, it.toMap())
            }
        },
        onDelete = {
            if (::dao.isInitialized) {
                dao.deleteGrade(it)
                deleteFromFirestore("grades", it.gradeId)
            }
        }
    )

    fun initDatabase(context: Context) {
        if (isInitialized) return

        // 1. Initialize Firebase programmatically using resources or direct credentials from google-services.json
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = try {
                    FirebaseOptions.fromResource(context)
                } catch (e: Exception) {
                    FirebaseOptions.Builder()
                        .setApplicationId("1:738030426833:android:b84398f31eb150afdc03f0")
                        .setApiKey("AIzaSyBUbH4YQQkr6qLk8ZQbp3KbZUFFSEeuPdQ")
                        .setProjectId("assignment-47b3c")
                        .setStorageBucket("assignment-47b3c.firebasestorage.app")
                        .build()
                }
                if (options != null) {
                    FirebaseApp.initializeApp(context.applicationContext, options)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Initialize Room Database
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "eduscore_database"
        )
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

        dao = db.appDao()

        // 3. Populate memory list instantly from SQLite cache (ensures snappy UI)
        val dbUsers = dao.getAllUsers()
        if (dbUsers.isNotEmpty()) {
            loadFromRoomCache()
        }

        // 4. Fetch synchronized updates from Firebase Firestore in the background
        syncFromFirestore()

        isInitialized = true
    }

    private fun loadFromRoomCache() {
        (users as DbMutableList).silentClear()
        (users as DbMutableList).silentAddAll(dao.getAllUsers())

        (students as DbMutableList).silentClear()
        (students as DbMutableList).silentAddAll(dao.getAllStudents())

        (academicTerms as DbMutableList).silentClear()
        (academicTerms as DbMutableList).silentAddAll(dao.getAllTerms())

        (courses as DbMutableList).silentClear()
        (courses as DbMutableList).silentAddAll(dao.getAllCourses())

        (enrollments as DbMutableList).silentClear()
        (enrollments as DbMutableList).silentAddAll(dao.getAllEnrollments())

        (assessments as DbMutableList).silentClear()
        (assessments as DbMutableList).silentAddAll(dao.getAllAssessments())

        (grades as DbMutableList).silentClear()
        (grades as DbMutableList).silentAddAll(dao.getAllGrades())
    }

    // --- FIRESTORE SYNC LOGIC ---

    private fun writeToFirestore(collection: String, docId: String, data: HashMap<String, out Any?>) {
        try {
            FirebaseFirestore.getInstance()
                .collection(collection)
                .document(docId)
                .set(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteFromFirestore(collection: String, docId: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection(collection)
                .document(docId)
                .delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun syncFromFirestore() {
        val firestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } ?: return

        // 1. Users
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toUser() }
                    if (list.isNotEmpty()) {
                        (users as DbMutableList).silentClear()
                        dao.clearUsers()
                        list.forEach { dao.insertUser(it) }
                        (users as DbMutableList).silentAddAll(list)
                    }
                } else if (users.isEmpty()) {
                    seed()
                }
            }

        // 2. Students
        firestore.collection("students").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toStudent() }
                    if (list.isNotEmpty()) {
                        (students as DbMutableList).silentClear()
                        dao.clearStudents()
                        list.forEach { dao.insertStudent(it) }
                        (students as DbMutableList).silentAddAll(list)
                    }
                }
            }

        // 3. Academic Terms
        firestore.collection("academic_terms").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toAcademicTerm() }
                    if (list.isNotEmpty()) {
                        (academicTerms as DbMutableList).silentClear()
                        dao.clearTerms()
                        list.forEach { dao.insertTerm(it) }
                        (academicTerms as DbMutableList).silentAddAll(list)
                    }
                }
            }

        // 4. Courses
        firestore.collection("courses").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toCourse() }
                    if (list.isNotEmpty()) {
                        (courses as DbMutableList).silentClear()
                        dao.clearCourses()
                        list.forEach { dao.insertCourse(it) }
                        (courses as DbMutableList).silentAddAll(list)
                    }
                }
            }

        // 5. Enrollments
        firestore.collection("course_enrollments").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toCourseEnrollment() }
                    if (list.isNotEmpty()) {
                        (enrollments as DbMutableList).silentClear()
                        dao.clearEnrollments()
                        list.forEach { dao.insertEnrollment(it) }
                        (enrollments as DbMutableList).silentAddAll(list)
                    }
                }
            }

        // 6. Assessments
        firestore.collection("assessments").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toAssessment() }
                    if (list.isNotEmpty()) {
                        (assessments as DbMutableList).silentClear()
                        dao.clearAssessments()
                        list.forEach { dao.insertAssessment(it) }
                        (assessments as DbMutableList).silentAddAll(list)
                    }
                }
            }

        // 7. Grades
        firestore.collection("grades").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.data?.toGradeLedger() }
                    if (list.isNotEmpty()) {
                        (grades as DbMutableList).silentClear()
                        dao.clearGrades()
                        list.forEach { dao.insertGrade(it) }
                        (grades as DbMutableList).silentAddAll(list)
                    }
                }
            }
    }

    fun seed() {
        // Clear lists silently to ensure no duplicate inserts before seeding
        (users as DbMutableList).silentClear()
        (students as DbMutableList).silentClear()
        (academicTerms as DbMutableList).silentClear()
        (courses as DbMutableList).silentClear()
        (enrollments as DbMutableList).silentClear()
        (assessments as DbMutableList).silentClear()
        (grades as DbMutableList).silentClear()

        // 1. Seed Users
        users.add(User("U001", "admin@eduscore.com", "admin123", "Admin"))
        users.add(User("U002", "lecturer@eduscore.com", "lecturer123", "Lecturer"))

        // 2. Seed Academic Term
        val term = AcademicTerm("T001", "Semester 1 2025/2026", "2025-01-01", "2025-06-30")
        academicTerms.add(term)
        academicTerms.add(AcademicTerm("T002", "Semester 2 2025/2026", "2026-01-01", "2026-12-31"))

        // 3. Seed Courses
        val courseNames = listOf(
            "Database Systems",
            "Mobile Application Development",
            "Data Structures & Algorithms",
            "Software Engineering",
            "Web Technologies",
            "Network Fundamentals"
        )
        val courseList = mutableListOf<Course>()
        courseNames.forEachIndexed { index, name ->
            val cId = "C00${index + 1}"
            val course = Course(cId, name, 3)
            courses.add(course)
            courseList.add(course)
        }

        // 4. Seed Assessments for each course
        var assessCounter = 1
        courseList.forEach { course ->
            val finalExam = Assessment("A0${assessCounter++}", course.courseId, "Final Exam", 50f)
            val midterm = Assessment("A0${assessCounter++}", course.courseId, "Midterm Exam", 30f)
            val assignments = Assessment("A0${assessCounter++}", course.courseId, "Assignments", 20f)
            assessments.add(finalExam)
            assessments.add(midterm)
            assessments.add(assignments)
        }

        // 5. Seed Students
        students.add(Student("S001", "Amelia Vance", "Computer Science", "Active"))
        students.add(Student("S002", "Benjamin Clark", "Software Engineering", "Active"))
        students.add(Student("S003", "Clara Oswald", "Data Science", "Active"))
        students.add(Student("S004", "Dilan Vance", "Cyber Security", "Active"))
        students.add(Student("S005", "Ethan Hunt", "Infotech Studies", "Inactive"))

        // 6. Seed Enrollments
        enrollments.add(CourseEnrollment("E01", "S001", "C001", "T001"))
        enrollments.add(CourseEnrollment("E02", "S002", "C001", "T001"))
        enrollments.add(CourseEnrollment("E03", "S003", "C002", "T001"))
        enrollments.add(CourseEnrollment("E04", "S004", "C002", "T001"))
        enrollments.add(CourseEnrollment("E05", "S001", "C003", "T001"))

        // 7. Seed Grade Ledger
        grades.add(GradeLedger("G01", "S001", "A01", 90f))
        grades.add(GradeLedger("G02", "S001", "A02", 85f))
        grades.add(GradeLedger("G03", "S001", "A03", 95f))
        
        grades.add(GradeLedger("G04", "S002", "A01", 75f))
        grades.add(GradeLedger("G05", "S002", "A02", 80f))
        grades.add(GradeLedger("G06", "S002", "A03", 85f))

        grades.add(GradeLedger("G07", "S003", "A04", 88f))
        grades.add(GradeLedger("G08", "S003", "A05", 92f))
        grades.add(GradeLedger("G09", "S003", "A06", 90f))

        grades.add(GradeLedger("G10", "S004", "A04", 65f))
        grades.add(GradeLedger("G11", "S004", "A05", 70f))
        grades.add(GradeLedger("G12", "S004", "A06", 75f))
    }

    // === ANALYTICS / UTILS ===

    // Returns active academic term based on current date
    fun getActiveTermName(context: Context): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val active = academicTerms.find { todayStr >= it.startDate && todayStr <= it.endDate }
        return active?.termName ?: "No Active Term"
    }

    // Calculates weighted final score (0 - 100) per course for a student
    fun getStudentCourseWeightedMarks(studentId: String): List<Pair<String, Float>> {
        val studentGrades = grades.filter { it.studentId == studentId }
        val courseGroups = studentGrades.groupBy { grade ->
            val assessment = assessments.find { it.assessmentId == grade.assessmentId }
            assessment?.courseId ?: ""
        }

        return courseGroups.mapNotNull { (courseId, gradeList) ->
            if (courseId.isEmpty()) return@mapNotNull null
            var weightedSum = 0f
            gradeList.forEach { grade ->
                val assess = assessments.find { it.assessmentId == grade.assessmentId }
                if (assess != null) {
                    weightedSum += grade.rawScore * (assess.weightage / 100f)
                }
            }
            courseId to weightedSum
        }
    }

    // Converts a weighted 0-100 mark to standard 4.0 scale Grade Points
    fun markToGpaPoints(mark: Float): Float {
        return when {
            mark >= 90f -> 4.0f
            mark >= 80f -> 3.0f
            mark >= 70f -> 2.0f
            mark >= 60f -> 1.0f
            else -> 0.0f
        }
    }

    // Returns Student ID to Average GPA
    fun getStudentGpa(studentId: String): Float {
        val courseMarks = getStudentCourseWeightedMarks(studentId)
        if (courseMarks.isEmpty()) return 0.0f
        val sumPoints = courseMarks.map { markToGpaPoints(it.second) }.sum()
        return sumPoints / courseMarks.size
    }

    fun getAverageGpaOfAllStudents(): Float {
        val activeStudentIds = students.map { it.studentId }
        if (activeStudentIds.isEmpty()) return 0.0f
        var gpaSum = 0f
        var counted = 0
        activeStudentIds.forEach { sId ->
            val gpa = getStudentGpa(sId)
            if (gpa > 0f) {
                gpaSum += gpa
                counted++
            }
        }
        return if (counted == 0) 3.21f else gpaSum / counted
    }
}
