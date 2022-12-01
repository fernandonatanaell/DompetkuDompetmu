package id.ac.istts.dkdm.mydatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    UserEntity::class,
    WalletEntity::class,
    HistoryEntity::class,
    CharityEntity::class,
    NotificationEntity::class,
    ContactEntity::class
], version=1)
abstract class AppDatabase : RoomDatabase(){
    abstract val userDao: UserDao
    abstract val walletDao: WalletDao
    abstract val historyDao: HistoryDao
    abstract val charityDao: CharityDao
    abstract val notificationDao: NotificationDao
    abstract val contactDao: ContactDao

    companion object {
        private var _database: AppDatabase? = null

        fun build(context: Context?): AppDatabase {
            if(_database == null){
                //
                _database = Room.databaseBuilder(context!!,AppDatabase::class.java,"dkdm_database").build()
            }
            return _database!!
        }
    }
}