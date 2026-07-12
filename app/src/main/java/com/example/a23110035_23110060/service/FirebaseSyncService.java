package com.example.a23110035_23110060.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.local.PendingSyncEntity;
import com.example.a23110035_23110060.data.repository.FirebaseRepository;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class FirebaseSyncService extends IntentService {
    private static final String TAG = "FirebaseSyncService";

    public FirebaseSyncService() {
        super("FirebaseSyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!NetworkHelper.isNetworkAvailable(this)) {
            return;
        }
        AppDatabase database = AppDatabase.getInstance(this);
        FirebaseFirestore firestore = FirebaseHelper.getFirestore();
        String currentUserId = FirebaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            return;
        }
        
        List<PendingSyncEntity> pendingItems = database.pendingSyncDao().getPendingByUser(currentUserId);
        for (PendingSyncEntity pending : pendingItems) {
            try {
                Tasks.await(firestore.collection("pending_sync")
                        .document(pending.syncId)
                        .set(FirebaseRepository.pendingSyncMap(pending), SetOptions.merge()));

                if ("meal_logs".equals(pending.collectionName)) {
                    syncMealLog(database, firestore, pending);
                } else if ("meal_plans".equals(pending.collectionName)) {
                    syncMealPlan(database, firestore, pending);
                }

                pending.status = "DONE";
                pending.updatedAt = System.currentTimeMillis();
                Tasks.await(firestore.collection("pending_sync")
                        .document(pending.syncId)
                        .set(FirebaseRepository.pendingSyncMap(pending), SetOptions.merge()));
                database.pendingSyncDao().markDone(pending.syncId, pending.updatedAt);
            } catch (Exception e) {
                Log.e(TAG, "Pending sync failed", e);
            }
        }
        database.pendingSyncDao().deleteDone();
    }

    private void syncMealLog(AppDatabase database, FirebaseFirestore firestore, PendingSyncEntity pending) throws Exception {
        if ("DELETE".equals(pending.actionType)) {
            String id = new JSONObject(pending.localDataJson).optString("mealLogId");
            if (!id.isEmpty()) {
                Tasks.await(firestore.collection("meal_logs").document(id).delete());
            }
            return;
        }
        MealLogEntity mealLog = MealLogEntity.fromJson(pending.localDataJson);
        if (mealLog.mealLogId == null || mealLog.mealLogId.isEmpty()) {
            return;
        }
        String imageUrl = mealLog.imageUrl;
        if (mealLog.imagePath != null && !mealLog.imagePath.trim().isEmpty()
                && (imageUrl == null || imageUrl.trim().isEmpty())) {
            File localFile = new File(mealLog.imagePath);
            if (localFile.exists()) {
                imageUrl = uploadImageBlocking(mealLog.userId, mealLog.mealLogId, mealLog.imagePath);
                mealLog.imageUrl = imageUrl;
            }
        }
        Tasks.await(firestore.collection("meal_logs")
                .document(mealLog.mealLogId)
                .set(FirebaseRepository.mealLogMap(mealLog), SetOptions.merge()));
        database.mealLogDao().markSynced(mealLog.mealLogId, imageUrl == null ? "" : imageUrl, System.currentTimeMillis());
    }

    private void syncMealPlan(AppDatabase database, FirebaseFirestore firestore, PendingSyncEntity pending) throws Exception {
        if ("DELETE".equals(pending.actionType)) {
            String id = new JSONObject(pending.localDataJson).optString("mealPlanId");
            if (!id.isEmpty()) {
                Tasks.await(firestore.collection("meal_plans").document(id).delete());
            }
            return;
        }
        MealPlanEntity mealPlan = MealPlanEntity.fromJson(pending.localDataJson);
        if (mealPlan.mealPlanId == null || mealPlan.mealPlanId.isEmpty()) {
            return;
        }
        Tasks.await(firestore.collection("meal_plans")
                .document(mealPlan.mealPlanId)
                .set(FirebaseRepository.mealPlanMap(mealPlan), SetOptions.merge()));
        database.mealPlanDao().markSynced(mealPlan.mealPlanId, System.currentTimeMillis());
    }

    private String uploadImageBlocking(String userId, String mealLogId, String imagePath) throws Exception {
        Uri uri = imagePath.startsWith("content://") || imagePath.startsWith("file://")
                ? Uri.parse(imagePath)
                : Uri.fromFile(new File(imagePath));
        StorageReference reference = FirebaseHelper.getStorage()
                .getReference()
                .child("meal_logs/" + userId + "/" + mealLogId + ".jpg");
        Tasks.await(reference.putFile(uri));
        return Tasks.await(reference.getDownloadUrl()).toString();
    }
}
