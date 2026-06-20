package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_sync")
public class PendingSyncEntity {
    @PrimaryKey
    @NonNull
    public String syncId = "";
    public String userId;
    public String actionType;
    public String collectionName;
    public String localDataJson;
    public String status;
    public long createdAt;
    public long updatedAt;
}
