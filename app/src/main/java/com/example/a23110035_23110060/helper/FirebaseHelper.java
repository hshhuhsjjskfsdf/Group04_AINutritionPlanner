package com.example.a23110035_23110060.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {
    private FirebaseHelper() {
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getAuth().getCurrentUser();
        return user == null ? null : user.getUid();
    }

    public static void signOut() {
        getAuth().signOut();
    }
}
