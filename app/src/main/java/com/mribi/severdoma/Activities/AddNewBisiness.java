package com.mribi.severdoma.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mribi.severdoma.Firebase.FirebaseDatabase;
import com.mribi.severdoma.Firebase.Interfaces.DataBaseAcceptable;
import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddNewBisiness extends AppCompatActivity implements DataBaseAcceptable {

    private static final int LOCATION_REQUEST = 11;
    private static final int IMAGE_REQUEST = 113;
    private double latitude = 0;
    private double longitude = 0;

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String PHONE = "phone";
    public static final String WEB = "web";
    public static final String VK = "vk";
    public static final String FB = "fb";
    public static final String INSTA = "insta";
    public static final String ADDRESS = "address";

    private FirebaseDatabase fb;


    public static final int RESULT_CODE_OK = 1;
    private Spinner spinner;
    private Bitmap bitmap = null;


    private static AddNewBisiness addNewBisiness;

    public AddNewBisiness() {
        addNewBisiness = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_add_new_bisiness);
        if (savedInstanceState != null){
            latitude = savedInstanceState.getDouble(LATITUDE, 0);
            longitude = savedInstanceState.getDouble(LONGITUDE, 0);
            ((EditText)findViewById(R.id.title_bis)).setText(savedInstanceState.getString(TITLE, ""));
            ((EditText)findViewById(R.id.description)).setText(savedInstanceState.getString(DESCRIPTION, ""));
            ((EditText)findViewById(R.id.phone)).setText(savedInstanceState.getString(PHONE, ""));
            ((EditText)findViewById(R.id.email)).setText(savedInstanceState.getString(WEB, ""));
            ((EditText)findViewById(R.id.address)).setText(savedInstanceState.getString(ADDRESS, ""));
            ((EditText)findViewById(R.id.vk)).setText(savedInstanceState.getString(VK, ""));
            ((EditText)findViewById(R.id.fb)).setText(savedInstanceState.getString(FB, ""));
            ((EditText)findViewById(R.id.insta)).setText(savedInstanceState.getString(INSTA, ""));
        }

        spinner = findViewById(R.id.spinner);


        findViewById(R.id.location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectLocationActivity.class);
                intent.putExtra(LATITUDE, latitude);
                intent.putExtra(LONGITUDE, longitude);
                startActivityForResult(intent, LOCATION_REQUEST);
            }
        });


        findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.push).setEnabled(false);
                if (checkCorrect())
                    pushData();
                else
                    findViewById(R.id.push).setEnabled(true);
                }
        });


        findViewById(R.id.image_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, IMAGE_REQUEST);
            }
        });



        fb = new FirebaseDatabase(this);


    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(LATITUDE, latitude);
        outState.putDouble(LONGITUDE, longitude);
        outState.putString(DESCRIPTION, ((EditText)findViewById(R.id.description)).getText().toString());
        outState.putString(TITLE, ((EditText)findViewById(R.id.title_bis)).getText().toString());
        outState.putString(PHONE, ((EditText)findViewById(R.id.phone)).getText().toString());
        outState.putString(WEB, ((EditText)findViewById(R.id.email)).getText().toString());
        outState.putString(ADDRESS, ((EditText)findViewById(R.id.address)).getText().toString());
        outState.putString(VK, ((EditText)findViewById(R.id.vk)).getText().toString());
        outState.putString(FB, ((EditText)findViewById(R.id.fb)).getText().toString());
        outState.putString(INSTA, ((EditText)findViewById(R.id.insta)).getText().toString());
    }

    private void pushData(){

        Log.i("main", "push");
        Bisiness bisiness = new Bisiness();
        bisiness.setType(spinner.getSelectedItemPosition()+1);

        bisiness.setLatitude(latitude);
        bisiness.setLongitude(longitude);
        bisiness.setName(((TextView)findViewById(R.id.title_bis)).getText().toString());
        bisiness.setDescription(((TextView)findViewById(R.id.description)).getText().toString());
        bisiness.setPhoneNumber(((TextView)findViewById(R.id.phone)).getText().toString());
        bisiness.setMail(((TextView)findViewById(R.id.email)).getText().toString());
        bisiness.setAddress(((TextView)findViewById(R.id.address)).getText().toString());
        bisiness.setVk(((TextView)findViewById(R.id.vk)).getText().toString());
        bisiness.setFb(((TextView)findViewById(R.id.fb)).getText().toString());
        bisiness.setInsta(((TextView)findViewById(R.id.insta)).getText().toString());

        fb.pushData(FirebaseDatabase.LOAD_COLLECTION, bisiness);


    }

    private void downloadImage(String name){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference child = storageRef.child("images/" + name + ".jpeg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        UploadTask uploadTask = child.putBytes(datas);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "не удалось отправить изображение", Toast.LENGTH_SHORT).show();
                findViewById(R.id.push).setEnabled(true);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "успешно", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST) {
            if (data != null) {
                latitude = data.getDoubleExtra(LATITUDE, 0);
                longitude = data.getDoubleExtra(LONGITUDE, 0);
            }
        } else if (requestCode == IMAGE_REQUEST) {
                try {
                    if (data == null || data.getData() == null)
                        throw new Exception();

                    Uri imageUri = data.getData();
                    InputStream imageStream;
                    imageStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                    ((ImageView) findViewById(R.id.image)).setImageBitmap(bitmap);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "не удалось получить изображение", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onDataGet(QuerySnapshot snapshots) {
    }

    @Override
    public void onDataSendSuccess(String id) {
        downloadImage(id);
    }

    @Override
    public void onDataSendFailure() {
        findViewById(R.id.push).setEnabled(true);
        Toast.makeText(getApplicationContext(), "не удалось загрузить", Toast.LENGTH_SHORT).show();
    }


    // если нажато "назад" и открыта информация - сбросить
    @Override
    public void onBackPressed() {

        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                this);
        quitDialog.setTitle("Выйти? Ваши изменения не сохранятся");

        quitDialog.setPositiveButton("да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }


    // проверялка корректности введенных данных
    private enum Checker{
        addressChecker{
            @Override
            boolean check(ToastMessage message) {
                String string = ((TextView)addNewBisiness.findViewById(R.id.address)).getText().toString();
                if (string.length() < 2)
                    return true;

                Pattern pattern = Pattern.compile(
                        "(ул|ул\\.|улица)\\s*\\D*(\\s|,)*(д\\.|д|дом)\\s*\\d{1,3}(\\s|,)*((к\\.|корпус|к).+\\s*(\\s|,))*((кв\\.|квартира|кв)\\s*\\d{1,3})*");

                Matcher matcher = pattern.matcher(string);
                if (!matcher.find()){
                    message.message = "Неверный адрес. Укажите адрес в порядке: улица, дом, корпус(не обязательно), квартира (не обязательно)";
                    return false;
                }
                return true;
            }
        },
        netChecker{
            boolean check(ToastMessage message){
                ConnectivityManager cm =
                        (ConnectivityManager) addNewBisiness.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
                    message.message = "нет подключения к интернету";
                    return false;
                }
                return true;
            }
        },
        coordsChecker{
            boolean check(ToastMessage message){
                if ((int)addNewBisiness.latitude ==0 || (int)addNewBisiness.longitude == 0){
                    message.message = "указаны не действительные координаты";
                    return false;
                }
                return true;
            }
        },
        nameChecker{
            boolean check(ToastMessage message){
                if (((TextView)addNewBisiness.findViewById(R.id.title_bis)).getText().toString().length() < 3){
                    message.message = "Не указано название предприятия";
                    return false;
                }
                return true;
            }
        },
        descriptionChecker {
            boolean check(ToastMessage message) {
                if (((TextView) addNewBisiness.findViewById(R.id.description)).getText().toString().length() < 3) {
                    message.message = "Не указано описание предприятия";
                    return false;
                }
                return true;
            }
        },
        pictureChecker {
        boolean check(ToastMessage message) {
            if (addNewBisiness.bitmap == null) {
                message.message = "Не добавлено изображение";
                return false;
            }
            return true;
            }
        },
        contactChecker {
            boolean check(ToastMessage message) {
                if ((((TextView)addNewBisiness.findViewById(R.id.phone)).getText().toString().length() < 2) &&
                        (((TextView)addNewBisiness.findViewById(R.id.email)).getText().toString().length() < 2) &&
                        (((TextView)addNewBisiness.findViewById(R.id.vk)).getText().toString().length() < 2) &&
                        (((TextView)addNewBisiness.findViewById(R.id.insta)).getText().toString().length() < 2) &&
                        (((TextView)addNewBisiness.findViewById(R.id.fb)).getText().toString().length() < 2)){
                    message.message = "Не добавлено контактов";
                    return false;
                }
                return true;
            }
        },
        phoneChecker{
            boolean check(ToastMessage message){
                TextView phone = addNewBisiness.findViewById(R.id.phone);
                if (phone.getText().toString().length() < 2)
                    return true;
                Pattern pattern = Pattern.compile("(((\\+7)|(8))\\d{10})|(\\d{6})");
                Matcher matcher = pattern.matcher(phone.getText().toString());
                if (!matcher.matches()) {
                    message.message = "Неправильно набран номер";
                    return false;
                }
                return true;
            }
        },
        webChecker{
            boolean check(ToastMessage message){
                TextView web = addNewBisiness.findViewById(R.id.email);
                if (web.getText().toString().length() < 2)
                    return true;

                String check_result = isCorrectUrl(web.getText().toString());
                if (check_result == null) {
                    message.message = "Указана некорректная ссылка на сайт";
                    return false;
                }
                web.setText(check_result);
                return true;
            }
        },
        vkChecker{
            boolean check(ToastMessage message){
                TextView vk = addNewBisiness.findViewById(R.id.vk);
                if (vk.getText().toString().length() < 2)
                    return true;

                String check_result = isCorrectUrl(vk.getText().toString());
                if (check_result == null)
                    return false;
                vk.setText(check_result);

                Pattern pattern = Pattern.compile("vk\\.com");
                Matcher matcher = pattern.matcher(vk.getText().toString());
                if (!matcher.find()) {
                    message.message = "Неверная ссылка на социальную сеть Вконтакте";
                    return false;
                }
                return true;
            }
        },
        fbChecker{
            boolean check(ToastMessage message){
                TextView fb = addNewBisiness.findViewById(R.id.fb);
                if (fb.getText().toString().length() < 2)
                    return true;


                String check_result = isCorrectUrl(fb.getText().toString());
                if (check_result == null)
                    return false;
                fb.setText(check_result);

                Pattern pattern = Pattern.compile("facebook\\.com");
                Matcher matcher = pattern.matcher(fb.getText().toString());
                if (!matcher.find()) {
                    message.message = "Неверная ссылка на социальную сеть FaceBook";
                    return false;
                }
                return true;
            }
        },
        instaChecker{
            boolean check(ToastMessage message){
                TextView insta = addNewBisiness.findViewById(R.id.insta);
                if (insta.getText().toString().length() < 2)
                    return true;


                String check_result = isCorrectUrl(insta.getText().toString());
                if (check_result == null)
                    return false;
                insta.setText(check_result);

                Pattern pattern = Pattern.compile("instagram\\.com");
                Matcher matcher = pattern.matcher(insta.getText().toString());
                if (!matcher.find()) {
                    message.message = "Неверная ссылка на социальную сеть Instagram";
                    return false;
                }
                return true;
            }
        };
        abstract boolean check(ToastMessage message);
    }

    private static class ToastMessage{
        String message;
        private ToastMessage() {
        }
    }

    // проверяет правильный ли URL и возвращает его-же видоизмененный, если ошибка только в отсутствии https
    private static String isCorrectUrl(String check) {
        try {
            new URL(check);
            return check;
        } catch (MalformedURLException e) {
            try {
                new URL("https://" + check);
                return "https://" + check;
            } catch (MalformedURLException e1) {
                return null;
            }
        }
    }


    private boolean checkCorrect(){
        for (Checker checker: Checker.values()){
            ToastMessage message = new ToastMessage();
            if (!checker.check(message)){
                Toast.makeText(getApplicationContext(), message.message, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


}
