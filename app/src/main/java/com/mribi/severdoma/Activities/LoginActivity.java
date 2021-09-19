package com.mribi.severdoma.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mribi.severdoma.Firebase.FirebaseSign;
import com.mribi.severdoma.Firebase.Interfaces.Assignable;
import com.mribi.severdoma.R;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity implements Assignable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

            findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText email = findViewById(R.id.email);
                    EditText password = findViewById(R.id.password);

                    if (isOnline()) {
                        FirebaseSign fireBaseSign = new FirebaseSign(LoginActivity.this);
                        fireBaseSign.signIn(email.getText().toString(), password.getText().toString());
                    }
                    else
                        Toast.makeText(getApplicationContext(), "нет подключения к интернету", LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onSuccess() {
        Intent intent = new Intent(getApplicationContext(), ModeratorActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFailure() {
        Toast.makeText(getApplicationContext(), "Почта или пароль указаны неверно", LENGTH_SHORT).show();
    }


    @Override
    public void onEmptyFills() {
        Toast.makeText(getApplicationContext(), "Были введены пустые поля", LENGTH_SHORT).show();
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
