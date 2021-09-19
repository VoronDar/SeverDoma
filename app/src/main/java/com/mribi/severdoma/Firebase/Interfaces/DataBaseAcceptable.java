package com.mribi.severdoma.Firebase.Interfaces;

import com.google.firebase.firestore.QuerySnapshot;

public interface DataBaseAcceptable {
    void onDataGet(QuerySnapshot snapshots);
    void onDataSendSuccess(String id);
    void onDataSendFailure();
}
