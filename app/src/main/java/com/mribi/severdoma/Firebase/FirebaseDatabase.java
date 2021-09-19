package com.mribi.severdoma.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mribi.severdoma.Firebase.Interfaces.DataBaseAcceptable;
import com.mribi.severdoma.pojo.Bisiness;


public class FirebaseDatabase {
    public static final String COLLECTION_NAME = "bisiness";
    public static final String LOAD_COLLECTION = "loadCollection";
    private static final String DB_TAG = "FDatabase";
    private DataBaseAcceptable view;

    public FirebaseDatabase(DataBaseAcceptable view) {
        this.view = view;
    }

    public void setOnChangeListener(String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // возвращает всю таблицу при обнаружении изменений
        db.collection(name).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                view.onDataGet(queryDocumentSnapshots);
            }});
    }

    public void pushData(String name, Bisiness bisiness){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(name)
                .add(bisiness)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(DB_TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        view.onDataSendSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DB_TAG, "Error adding document", e);
                        view.onDataSendFailure();

                    }
                });


    }

}
