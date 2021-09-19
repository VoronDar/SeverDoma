package com.mribi.severdoma.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mribi.severdoma.Adapters.BisinessAdapter;
import com.mribi.severdoma.Adapters.TypesAdapter;
import com.mribi.severdoma.Firebase.FirebaseDatabase;
import com.mribi.severdoma.Firebase.Interfaces.DataBaseAcceptable;
import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.GONE;



/*
    Структура:

    из OnStart() запускается слушатель базы данных - он в свою очередь запускает onDataGet()
    за отображение информации отвечают setInfo() и removeInfo()
    onMapReady() - подготавливает карту

    Поиск и связанные с ним методы:

    searchForCard()    поиск
    selectByType()     фильтрация
    resetAfterFilter() восстановление после фильтрации


 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, DataBaseAcceptable {

    private GoogleMap                   mMap;

    private EditText                    search;

    private static List<Bisiness>       allUnits;
    private static ArrayList<Bitmap>    bitmaps;
    private static ArrayList<Bisiness>  filtatedUnits;
    private static ArrayList<Bitmap>    filtatedBitmaps;

    private Bisiness                    now;

    private RecyclerView                recyclerView;
    private BisinessAdapter             adapter;

    private boolean                     isInfoShowed = false;
    private boolean                     isFiltrated = false;
    private int                         filtratedType = 0;




    private enum ViewMode{
        regular,
        mapOnly,
        listOnly
    };
    private ViewMode viewMode;




    private final int FILTER_REQUEST = 41;
    static final int FILTER_RESULT_OK = 1;
    static final String FILTER_TYPE_PARAM = "type";


    private final int VOICE_RECOGNITION_REQUEST = 111;





    private void prepareVoiceRecognition(){
        PackageManager manager = getPackageManager();
        List<ResolveInfo> activities = manager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        ImageView image = findViewById(R.id.voice);
        if (activities.size() == 0){
            image.setVisibility(GONE);
        } else{
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speak();
                }
            });
        }
    }

   private void speak(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL ,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST);
   }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_maps);

        prepareVoiceRecognition();

        ////// СОЗДАТЬ КАРТУ ///////////////////////////////////////////////////////////////////////
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        else
            Toast.makeText(this, "не удалось подключить карту", Toast.LENGTH_SHORT).show();
        ////////////////////////////////////////////////////////////////////////////////////////////


        ////// ПОДГОТОВИТЬ КОЛЛЕКЦИИ ///////////////////////////////////////////////////////////////
        bitmaps = new ArrayList<>();
        adapter = new BisinessAdapter(this, new ArrayList<Bisiness>(), new ArrayList<Bitmap>());
        filtatedBitmaps = new ArrayList<>();
        filtatedUnits = new ArrayList<>();
        adapter = new BisinessAdapter(this, new ArrayList<Bisiness>(), new ArrayList<Bitmap>());
        ////////////////////////////////////////////////////////////////////////////////////////////


        ////// ЗАПУСТИТЬ ПОЛУЧЕНИЕ ДАННЫХ //////////////////////////////////////////////////////////
        FirebaseDatabase fb = new FirebaseDatabase(this);
        fb.setOnChangeListener(FirebaseDatabase.COLLECTION_NAME);
        ////////////////////////////////////////////////////////////////////////////////////////////




        ////// ПЕРЕХОДЫ ////////////////////////////////////////////////////////////////////////////
        findViewById(R.id.link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + now.getPhoneNumber())));
            }
        });
        findViewById(R.id.link_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(now.getMail())));
            }
        });
        findViewById(R.id.link_vk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(now.getVk())));
            }
        });
        findViewById(R.id.link_insta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(now.getInsta())));
            }
        });
        findViewById(R.id.link_fb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(now.getFb())));
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////


        ////// ПОИСК И ФИЛЬТРАЦИЯ //////////////////////////////////////////////////////////////////
        search = findViewById(R.id.search);
        search.setText("");
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForCards();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilterSelectActivity.class);
                startActivityForResult(intent, FILTER_REQUEST);
                overridePendingTransition(R.anim.slide, R.anim.nothing);
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                resetAnimation(v);
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////




        ////// СКРЫТИЕ КАРТЫ ИЛИ СПИСКА/////////////////////////////////////////////////////////////
        viewMode = ViewMode.regular;
        /*
        findViewById(R.id.hide_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewMode == ViewMode.regular) {
                    viewMode = ViewMode.listOnly;


                    findViewById(R.id.map).setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.hide_map).setVisibility(GONE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.open_map).getLayoutParams();

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    params.bottomMargin = (int)(displayMetrics.heightPixels/1.2);
                }else{
                    viewMode = ViewMode.regular;
                    findViewById(R.id.map).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.panel).setVisibility(View.VISIBLE);
                    findViewById(R.id.open_map).setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.hide_map).getLayoutParams();
                    params.topMargin = 0;
                }
            }
        });
        findViewById(R.id.open_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewMode == ViewMode.regular) {
                    viewMode = ViewMode.mapOnly;
                    findViewById(R.id.map).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(GONE);
                    findViewById(R.id.panel).setVisibility(GONE);

                    findViewById(R.id.open_map).setVisibility(GONE);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.hide_map).getLayoutParams();

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    //params.bottomToBottom = 0;
                    //params.topMargin = (int)(displayMetrics.heightPixels/1.25);
                } else{
                    viewMode = ViewMode.regular;
                    findViewById(R.id.map).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.panel).setVisibility(View.VISIBLE);
                    findViewById(R.id.hide_map).setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.open_map).getLayoutParams();
                    params.bottomMargin = 0;
                }
            }
        });

         */

        ////////////////////////////////////////////////////////////////////////////////////////////

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeInfo();
            }
        });
    }

    // анимация для сброса фильров. В ней же запускается resetData, который и сбрасывает фильтры
    private void resetAnimation(final View v){
        ////// АНИМАЦИИ ОБНОВЛЕНИЯ /////////////////////////////////////////////////////////
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.touch_button);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.touch_button_back);
                v.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);


        animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(250);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(250);
                recyclerView.startAnimation(animation);
                resetData();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recyclerView.startAnimation(animation);
        ////////////////////////////////////////////////////////////////////////////////////




    }

    private void setSearchFail(){
        recyclerView.setVisibility(GONE);
        findViewById(R.id.search_failed).setVisibility(View.VISIBLE);

    }
    private void releaseSearchFail(){
        recyclerView.setVisibility(View.VISIBLE);
        findViewById(R.id.search_failed).setVisibility(GONE);
    }


    private void resetData(){
        isFiltrated = false;
        if (filtratedType != 0 || search.getText().toString().length() >= 2) {
            search.setText("");
            filtratedType = 0;
            adapter.resetBitmaps(bitmaps);
            adapter.resetUnits((ArrayList<Bisiness>) allUnits);
            pushMarkers((ArrayList<Bisiness>) allUnits);
            releaseSearchFail();
        }
    }


    // запускается при получении данных из базы данных
    @Override
    public void onDataGet(QuerySnapshot snapshots) {
        // Запихивает инфо о точках и картинки в адаптер

        if (snapshots != null) {
            mMap.clear();                                                                           // чистит от маркеров, чтобы нанести их по новой

            ////// ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЙ ///////////////////////////////////////////////////////////
            final List<Bisiness> list = snapshots.toObjects(Bisiness.class);
            bitmaps.clear();

            for (int i = 0; i < list.size(); i++) {

                ////// ПОДГОТОВКА К ПОЛУЧЕНИЮ ИЗОБРАЖЕНИЙ  /////////////////////////////////////////
                bitmaps.add(null);
                final long ONE_MEGABYTE = 1024 * 1024;
                final int pos = i;

                if (!isOnline())                                                                    // если оффлайн - то сразу загружаем изображения с памяти
                    setLocalImage(pos, list.get(i).getId());

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                final StorageReference child = storageRef.child("images/" + list.get(i).getId() + ".jpeg");
                ////////////////////////////////////////////////////////////////////////////////////


                ////// ЧТЕНИЕ ИЗОБРАЖЕНИЙ С ОБЛАКА  ////////////////////////////////////////////////
                final int finalI = i;
                child.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        bitmaps.set(pos, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        adapter.notifyDataSetChanged();

                        ////// ЗАПИСЬ В ЛОКАЛЬНЫЙ ФАЙЛ  /////////////////////////////////////////////
                        File f = new File(getApplicationContext().getCacheDir(), list.get(finalI).getId() + ".jpeg");
                        try {
                            f.createNewFile();
                            Bitmap bitmap = bitmaps.get(pos);
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ////////////////////////////////////////////////////////////////////////////

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        setLocalImage(pos, list.get(finalI).getId());
                        //adapter.notifyItemChanged(bitmaps.size() - 1);
                    }
                });
                ////////////////////////////////////////////////////////////////////////////////////
            }
            ////////////////////////////////////////////////////////////////////////////////////////


            ////// ПОДГОТОВКА АДАПТЕРА ДЛЯ ТОЧЕК  //////////////////////////////////////////////////
            allUnits = list;
            Log.i("main", Integer.toString(list.size()));
            if (recyclerView == null) {                                                             // если данные загружаются впервый раз
                recyclerView = findViewById(R.id.things);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                        RecyclerView.VERTICAL, false));
                recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                adapter.resetBitmaps(bitmaps);
                adapter.resetUnits((ArrayList<Bisiness>) list);
                //adapter.setHasStableIds(true);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                adapter.setBlockListener(new BisinessAdapter.BlockListener() {                      // клик на карточку - переход на ее информацию
                    @Override
                    public void onClick(int position) {
                        Bisiness unit = adapter.getUnit(position);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(
                                new LatLng(unit.getLatitude(), unit.getLongitude())));
                        setInfo(unit, adapter.getNowBitmap(position));
                    }
                });

            } else {                                                                                // если данные подгружаются
                adapter.resetUnits((ArrayList<Bisiness>) list);
                pushMarkers((ArrayList<Bisiness>) list);
                adapter.notifyDataSetChanged();
            }
            ////////////////////////////////////////////////////////////////////////////////////////
            pushMarkers((ArrayList<Bisiness>)list);
        }
    }


    // поиск карт по названию и типу
    private void searchForCards() {

        if (!isFiltrated) {                                                                          // поиск среди всех точек
            if (search.getText().length() < 2) {                                                     // поиск начинается с 2 введеных символов
                adapter.resetBitmaps(bitmaps);
                adapter.resetUnits((ArrayList<Bisiness>) allUnits);
                pushMarkers((ArrayList<Bisiness>)allUnits);
            } else {
                LinkedHashSet<Bisiness> set = new LinkedHashSet<>();
                ArrayList<Bitmap> Bitset = new ArrayList<>();

                Pattern pattern = Pattern.compile(search.getText().toString(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = null;

                ///// ПОИСК ПО ИМЕНИ ///////////////////////////////////////////////////////////////
                int i = -1;
                for (Bisiness b : allUnits) {
                    i++;
                    if (matcher == null)
                        matcher = pattern.matcher(b.getName());
                    else
                        matcher.reset(b.getName());
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(bitmaps.get(i));
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////


                ///// ПОИСК ПО ТИПУ ////////////////////////////////////////////////////////////////
                i = -1;
                for (Bisiness b : allUnits) {
                    i++;
                    matcher.reset(getResources().getStringArray(R.array.types)[b.getType() - 1]);
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(bitmaps.get(i));
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////

                ///// ПОИСК ПО АДРЕСУ //////////////////////////////////////////////////////////////
                i = -1;
                for (Bisiness b : allUnits) {
                    i++;
                    if (b.getAddress() == null)
                        continue;
                    matcher.reset(b.getAddress());
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(bitmaps.get(i));
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////


                adapter.resetBitmaps(Bitset);
                adapter.resetUnits(new ArrayList<>(set));
                pushMarkers(adapter.getUnits());
            }
        } else {                                                                                    // поиск в фильтрованном
            if (search.getText().length() < 2) {
                adapter.resetBitmaps(filtatedBitmaps);
                adapter.resetUnits(filtatedUnits);
                pushMarkers(filtatedUnits);
            } else {
                LinkedHashSet<Bisiness> set = new LinkedHashSet<>();
                ArrayList<Bitmap> Bitset = new ArrayList<>();

                Pattern pattern = Pattern.compile(search.getText().toString(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = null;

                ///// ПОИСК ПО ИМЕНИ ///////////////////////////////////////////////////////////////
                int i = -1;
                for (Bisiness b : filtatedUnits) {
                    i++;
                    if (matcher == null)
                        matcher = pattern.matcher(b.getName());
                    else
                        matcher.reset(b.getName());
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(filtatedBitmaps.get(i));
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////

                ///// ПОИСК ПО ТИПУ ////////////////////////////////////////////////////////////////
                i = -1;
                for (Bisiness b : filtatedUnits) {
                    i++;
                    matcher.reset(getResources().getStringArray(R.array.types)[b.getType() - 1]);
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(filtatedBitmaps.get(i));
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////

                ///// ПОИСК ПО АДРЕСУ //////////////////////////////////////////////////////////////
                i = -1;
                for (Bisiness b : filtatedUnits) {
                    i++;
                    if (b.getAddress() == null)
                        continue;
                    matcher.reset(b.getAddress());
                    if (matcher.find()) {
                        if (set.add(b))
                            Bitset.add(bitmaps.get(i));
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////



                adapter.resetBitmaps(Bitset);
                adapter.resetUnits(new ArrayList<>(set));
                pushMarkers(adapter.getUnits());
            }
        }


        if (adapter.getItemCount() == 0){
            setSearchFail();
        } else
            releaseSearchFail();
    }

    private void pushMarkers(ArrayList<Bisiness> units){
        mMap.clear();
        for (Bisiness u: units) {
            mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(u.getLatitude(), u.getLongitude()))
                        .title(u.getName())
                        .snippet(u.getDescription())
                        .icon(BitmapDescriptorFactory.fromResource(getIcon(u.getType()))));
        }
    }


    // изменение макета - вставка блока с информацией
    private void setInfo(Bisiness unit, Bitmap bitmap) {
        isInfoShowed = true;
        now = unit;

        ////// ИЗМЕНЯЕМ ВИДЖЕТЫ ////////////////////////////////////////////////////////////////////
        recyclerView.setVisibility(GONE);
        findViewById(R.id.info).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.this_name)).setText(unit.getName());
        ((TextView) findViewById(R.id.this_description)).setText(unit.getDescription());

        LinearLayout phoneLayout = findViewById(R.id.phone);
        LinearLayout mailLayout = findViewById(R.id.mail);
        LinearLayout vkLayout = findViewById(R.id.vk);
        LinearLayout instaLayout = findViewById(R.id.insta);
        LinearLayout fbLayout = findViewById(R.id.fb);
        if (unit.getPhoneNumber() != null && unit.getPhoneNumber().length() > 1){
            phoneLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.label)).setText("телефон: " + unit.getPhoneNumber());
        } else
            phoneLayout.setVisibility(GONE);

        if (unit.getMail() != null && unit.getMail().length() > 1){
            mailLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.label_mail)).setText("сайт: " + unit.getMail());
        } else
            mailLayout.setVisibility(GONE);

        if (unit.getVk() != null && unit.getVk().length() > 1){
            vkLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.label_vk)).setText("ВКонтакте: " + unit.getVk());
        } else
            vkLayout.setVisibility(GONE);

        if (unit.getInsta() != null && unit.getInsta().length() > 1){
            instaLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.label_insta)).setText("Инстаграм: " + unit.getInsta());
        } else
            instaLayout.setVisibility(GONE);

        if (unit.getFb() != null && unit.getFb().length() > 1){
            fbLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.label_fb)).setText("FaceBook: " + unit.getFb());
        } else
            fbLayout.setVisibility(GONE);

        if (unit.getAddress() != null && unit.getAddress().length() > 1){
            ((TextView) findViewById(R.id.address)).setText("Адрес: " + unit.getAddress());
        } else
            findViewById(R.id.address).setVisibility(GONE);


        search.setClickable(false);
        //search.setVisibility(View.GONE);
        findViewById(R.id.search_panel).setVisibility(GONE);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setScrollY(0);
        ////////////////////////////////////////////////////////////////////////////////////////////


        ////// СТАВИМ КАРТИНКУ /////////////////////////////////////////////////////////////////////
        ImageView image = findViewById(R.id.image_p);
        if (bitmap != null) {
            image.setVisibility(View.VISIBLE);
            image.setImageBitmap(bitmap);
        } else
            image.setVisibility(GONE);
        ////////////////////////////////////////////////////////////////////////////////////////////


        ////// УБИРАЕМ КЛАВИАТУРУ //////////////////////////////////////////////////////////////////
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(findViewById(R.id.info).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        ////////////////////////////////////////////////////////////////////////////////////////////


        if (viewMode == ViewMode.mapOnly) {
            findViewById(R.id.panel).setVisibility(View.VISIBLE);
            findViewById(R.id.hide_map).setVisibility(View.GONE);
        }


        /*
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeInfo();
            }
        });

         */

    }

    // изменение макета - удаление блока с информацией
    private void removeInfo() {
        isInfoShowed = false;
        findViewById(R.id.search_panel).setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        findViewById(R.id.info).setVisibility(GONE);
        search.setClickable(true);

        if (viewMode == ViewMode.mapOnly) {
            findViewById(R.id.panel).setVisibility(View.GONE);
            findViewById(R.id.hide_map).setVisibility(View.VISIBLE);
        }
    }

    // слушатель клика на маркер - он вроде не работает хз почему  - но мне страшно его трогать))
    public boolean onMarkerClick(final Marker marker) {
        /*
        Log.i("main", "CLICCCCK");
        String name = marker.getTitle();
        Log.i("main", "title - " + marker.getTitle());
        int pos = 0;
        if (search.getText().length() > 1 && recyclerView.getVisibility() == View.GONE) {
            search.setText("");
            adapter.resetBitmaps(bitmaps);
            adapter.resetUnits((ArrayList<Bisiness>) allUnits);
            pushMarkers((ArrayList<Bisiness>) allUnits);
        }
        for (Bisiness n : adapter.getUnits()) {
            if (n.getName().equals(name)) {
                setInfo(n, adapter.getNowBitmap(pos));
                break;
            }
            pos++;
        }


         */
        return true;
    }


    // запускается при подготовке карты
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10);
        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(68.9791700, 33.0925100)));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new
                        LatLng(marker.getPosition().latitude, marker.getPosition().longitude)));

                ///////// ОЧИСТКА ПОИСКА ///////////////////////////////////////////////////////
                String name = marker.getTitle();
                /*
                if (search.getText().length() > 1 && recyclerView.getVisibility() == View.GONE) {
                    search.setText("");
                    adapter.resetBitmaps(bitmaps);
                    adapter.resetUnits((ArrayList<Bisiness>) allUnits);
                }

                 */
                ////////////////////////////////////////////////////////////////////////////////


                ///////// ОПРЕДЕЛЕНИЕ ТОЧКИ И ОТКРЫТИЕ ИНФО ////////////////////////////////////
                int pos = 0;
                for (Bisiness n : allUnits) {
                    if (n.getName().equals(name)) {
                        setInfo(n, bitmaps.get(pos));
                        break;
                    }
                    pos++;
                }
                ////////////////////////////////////////////////////////////////////////////////

                return true;
            }
        });


    }


    // получение иконок
    public static final int ICON_JAPAN = 1;
    public static final int ICON_FAST = 2;
    public static final int ICON_FLOWER = 3;
    public static final int ICON_PIZZA = 4;
    public static final int ICON_FISH = 5;
    public static final int ICON_WATER = 6;
    public static final int ICON_RESTAURANT = 7;
    public static final int ICON_ICE_SCREAM = 8;
    public static final int ICON_ANIMAL = 9;
    public static final int ICON_MED = 10;
    public static final int ICON_GYGIENIC = 11;
    public static final int ICON_TOOLS = 12;
    public static final int ICON_TECHNIQUE = 13;
    public static final int ICON_FOOD = 14;
    public static final int ICON_CLOTHES = 15;
    public static final int MAX_TYPE = 15;

    public static int getIcon(int type) {
        switch (type) {
            case ICON_JAPAN:
                return R.drawable.g1222;
            case ICON_FAST:
                return R.drawable.g1402;
            case ICON_FLOWER:
                return R.drawable.g1479;
            case ICON_PIZZA:
                return R.drawable.g1897;
            case ICON_FISH:
                return R.drawable.g1949;
            case ICON_WATER:
                return R.drawable.g978;
            case ICON_RESTAURANT:
                return R.drawable.g985;
            case ICON_ICE_SCREAM:
                return R.drawable.g997;
            case ICON_ANIMAL:
                return R.drawable.g2739;
            case ICON_MED:
                return R.drawable.g2751;
            case ICON_GYGIENIC:
                return R.drawable.g2700;
            case ICON_TOOLS:
                return R.drawable.g2714;
            case ICON_TECHNIQUE:
                return R.drawable.g2681;
            case ICON_FOOD:
                return R.drawable.g2672;
            case ICON_CLOTHES:
                return R.drawable.g2691;
            default:
                return R.drawable.neitral;
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    // получение изображения из внутренней памяти и вставка его в bitmaps
    private void setLocalImage(int pos, String id) {
        try {
            Uri file = Uri.fromFile(new File(getApplicationContext().getCacheDir().getPath() +
                    "/" + id + ".jpeg"));

            if (file == null)
                throw new FileNotFoundException();
            InputStream imageStream;
            imageStream = getContentResolver().openInputStream(file);
            bitmaps.set(pos, BitmapFactory.decodeStream(imageStream));
            adapter.notifyDataSetChanged();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDataSendSuccess(String name) {
    }

    @Override
    public void onDataSendFailure() {
    }


    // если нажато "назад" и открыта информация/включен фильтр - сбросить
    @Override
    public void onBackPressed() {
        if (isInfoShowed)
            removeInfo();
        else if (isFiltrated)
            resetData();
        else
            finish();
    }


    // фильтрация. Изменение и списка и карты
    private void selectByType(int type) {
        isFiltrated = true;
        filtratedType = type+1;
        filtatedUnits = new ArrayList<>();
        filtatedBitmaps = new ArrayList<>();
        mMap.clear();
        for (int i = 0; i < allUnits.size(); i++) {
            if (allUnits.get(i).getType() == type) {
                filtatedUnits.add(allUnits.get(i));
                filtatedBitmaps.add(bitmaps.get(i));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(allUnits.get(i).getLatitude(), allUnits.get(i).getLongitude()))
                        .title(allUnits.get(i).getName())
                        .snippet(allUnits.get(i).getDescription())
                        .icon(BitmapDescriptorFactory.fromResource(getIcon(allUnits.get(i).getType()))));
            }
        }
        adapter.resetBitmaps(filtatedBitmaps);
        adapter.resetUnits(filtatedUnits);
        pushMarkers(filtatedUnits);
        if (adapter.getItemCount() == 0){
            setSearchFail();
        } else
            releaseSearchFail();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ///////// ПОЛУЧЕНИЕ ФИЛЬТРА ////////////////////////////////////////////////////////////////
        if (requestCode == FILTER_REQUEST){
            if (resultCode == FILTER_RESULT_OK) {
                int type = data.getIntExtra(FILTER_TYPE_PARAM, 0);

                // УБЕРИ ОТ СЮДА НАХЕР
                    if (type == 0) {
                        filtratedType = 0;
                        isFiltrated = false;
                        adapter.resetBitmaps(bitmaps);
                        adapter.resetUnits((ArrayList<Bisiness>) allUnits);
                    }
                    else {
                        selectByType(type);
                    }
                    if (search.getText().toString().length() > 1)
                        searchForCards();
                }
            }
        else if (requestCode == VOICE_RECOGNITION_REQUEST){
            if (data != null) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!matches.isEmpty()) {
                    search.setText(matches.get(0));
                }
            } else{
                Toast.makeText(this, "распознавание не удалось", Toast.LENGTH_SHORT).show();
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
    }
}
