package com.example.a23110035_23110060.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PendingSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingSyncEntity pendingSync);

    @Update
    void update(PendingSyncEntity pendingSync);

    @Delete
    void delete(PendingSyncEntity pendingSync);

    @Query("SELECT * FROM pending_sync WHERE status = 'PENDING' AND userId = :userId ORDER BY createdAt ASC")
    List<PendingSyncEntity> getPendingByUser(String userId);

    @Query("SELECT * FROM pending_sync WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<PendingSyncEntity> getAllPending();

    @Query("UPDATE pending_sync SET status = 'DONE', updatedAt = :updatedAt WHERE syncId = :syncId")
    void markDone(String syncId, long updatedAt);

    @Query("DELETE FROM pending_sync WHERE status = 'DONE'")
    void deleteDone();
}
