package com.mribi.severdoma.Firebase;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.mribi.severdoma.Firebase.Interfaces.Assignable;

// класс для авторизации
public class FirebaseSign{
    private Assignable view;
    private FirebaseAuth mAuth;
    private static final String SIGN_TAG = "FAuth";

    public FirebaseSign(Assignable view){
        this.view = view;
    }

    public void signIn(String email, String password) {
        if (email.length() > 0 && password.length() > 0) {
            mAuth = FirebaseAuth.getInstance();
            Log.i(SIGN_TAG, "signInWithEmail:start");
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) view, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i(SIGN_TAG, "signInWithEmail:success");
                                view.onSuccess();
                            } else {
                                Log.i(SIGN_TAG, "signInWithEmail:failure");
                                view.onFailure();
                            }

                        }
                    });
        }
        else {
            Log.i(SIGN_TAG, "signInWithEmail:empty_fills");
            view.onEmptyFills();
        }
    }
}
