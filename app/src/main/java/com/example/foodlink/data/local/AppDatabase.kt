package com.example.foodlink.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.foodlink.data.model.DonationRequest
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.data.model.User
import com.example.foodlink.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the main database class that coordinates all the tables (entities).
 * We define the entities included in the database and the version number.
 */
@Database(entities = [User::class, FoodPost::class, DonationRequest::class, Message::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    // This provides access to the DAO (Data Access Object) where our SQL queries are.
    abstract fun foodDao(): FoodDao

    companion object {
        // INSTANCE will hold our database object once it's created.
        // @Volatile ensures that the database state is always up-to-date across all threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * This function gets the database instance. If it doesn't exist, it creates one.
         * We use the 'Singleton' pattern so that only one database instance exists for the whole app.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it. If it is null, create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodlink_database" // The actual name of the file stored on the phone.
                )
                // If we change the database version, delete the old data and start fresh.
                .fallbackToDestructiveMigration()
                // We add a callback to perform an action the very first time the DB is created.
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // When the DB is first created, we want to add some default data.
                        INSTANCE?.let { database ->
                            // CoroutineScope runs this in the background so the app doesn't freeze.
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDatabase(database.foodDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * This function adds the very first user (the Admin) to the app automatically.
         * Without this, no one could log in as Admin to approve others!
         */
        private suspend fun populateDatabase(dao: FoodDao) {
            dao.insertUser(User(
                id = 1, 
                name = "FoodLink Admin", 
                email = "foodlink@gmail.com", 
                password = "AdminPassword123!", 
                role = "ADMIN", 
                isVerified = true
            ))
        }
    }
}
