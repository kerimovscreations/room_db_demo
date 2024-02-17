package az.edu.bhos.android.l6_coroutines

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val viewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val preferences = this.getSharedPreferences(
//            "az.edu.bhos.android.l6_coroutines.FIRST_FILE",
//            Context.MODE_PRIVATE
//        )
//
//        preferences.edit {
//            putInt(getString(R.string.user_id_key), 101)
//            apply()
//        }
//
//        println(preferences.getInt("USER_ID", 0))

        viewModel.testDb(this)
    }
}

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM user_table")
    suspend fun getAllAsFlow(): Flow<List<User>>

    @Query("SELECT * FROM user_table WHERE uid IN (:userIds)")
    suspend fun loadAllByIds(userIds: IntArray): List<User>

    @Query(
        "SELECT * FROM user_table WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    suspend fun findByName(first: String, last: String): User

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg users: User)

    @Delete
    suspend fun delete(user: User)
}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

/*
{
"firstName": "value",
"lastName": "value2"
}
 */

class MainViewModel : ViewModel() {

    fun testDb(context: Context) {
        val database = Room.databaseBuilder(
            context = context,
            AppDatabase::class.java, "user_db"
        ).build()

        val dao = database.userDao()

        val testUser = User(
            uid = 1,
            firstName = "Karim",
            lastName = "Karimov3"
        )

        viewModelScope.launch {
            dao.insertAll(testUser)

            val allUsers = dao.getAll()
            println(allUsers)
        }
    }
//    fun test() {
////        viewModelScope.launch {
////            val startTime = System.currentTimeMillis()
////            val token = doLongWork()
////            println(token)
////            println("Time taken ${System.currentTimeMillis() - startTime}")
////        }
//
//        newSingleThreadContext("Ctx1").use { ctx1 ->
//            newSingleThreadContext("Ctx2").use { ctx2 ->
//                viewModelScope.launch {
//                    runBlocking(ctx1) {
//                        println("Run on ctx1")
//
//                        withContext(ctx2) {
//                            println("Run on ctx2")
//                        }
//
//                        println("End on ctx1")
//                    }
//                }
//            }
//        }
//    }

//    private suspend fun doLongWork(): String {
//        delay(1000)
//        return "Delayed by 1 sec"
//    }
}