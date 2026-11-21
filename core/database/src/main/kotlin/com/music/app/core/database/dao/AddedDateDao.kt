package com.music.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.music.app.core.database.entity.AddedDateEntity

@Dao
interface AddedDateDao {

    @Query("SELECT * FROM added_dates")
    suspend fun getAll(): List<AddedDateEntity>

    @Upsert
    suspend fun upsertAll(dates: List<AddedDateEntity>)

    @Query("UPDATE OR REPLACE added_dates SET path = :toPath WHERE path = :fromPath")
    suspend fun move(fromPath: String, toPath: String)

    @Query("DELETE FROM added_dates WHERE path IN (:paths)")
    suspend fun deleteByPaths(paths: List<String>)
}
