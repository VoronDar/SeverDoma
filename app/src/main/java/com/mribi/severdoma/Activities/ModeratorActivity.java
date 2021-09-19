package com.mribi.severdoma.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mribi.severdoma.Adapters.BisinessModeratorAdapter;
import com.mribi.severdoma.Firebase.FirebaseDatabase;
import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.util.ArrayList;
import java.util.List;

public class ModeratorActivity extends AppCompatActivity {

    public static final String LOAD_COLLECTION = "loadCollection";
    private ArrayList<Bisiness> bisinessList;
    private FirebaseFirestore db;
    private BisinessModeratorAdapter adapter;
    private ArrayList<Bitmap> maps;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_moderator);

        getBisiness();
    }

    private void getBisiness(){
        db = FirebaseFirestore.getInstance();
        db.collection(LOAD_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            if (snapshot == null){
                                Log.d("main", "nothing");
                                return;
                            }


                            storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();

                            List<Bisiness> list = snapshot.toObjects(Bisiness.class);
                            maps = new ArrayList<>(list.size());
                            for (int d = 0; d < list.size(); d++){
                                maps.add(null);
                            }
                            int i = 0;
                            for (QueryDocumentSnapshot s : snapshot){
                                list.get(i++).setId(s.getId());
                                StorageReference child = storageRef.child("images/" + s.getId() + ".jpeg");

                                final long ONE_MEGABYTE = 1024 * 1024;
                                final int pos = i-1;
                                child.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        maps.set(pos, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                        //adapter.notifyItemChanged(pos);
                                        adapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        maps.set(pos, null);
                                        //adapter.notifyItemChanged(maps.size()-1);
                                    }
                                });


                            }
                            setBisiness(list);
                        } else {
                            Log.w("main", "Error getting documents.", task.getException());
                            Toast.makeText(getApplicationContext(), "ошибка загрузки данных", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void setBisiness(List<Bisiness> list){
        bisinessList = (ArrayList<Bisiness>)list;
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new BisinessModeratorAdapter(this, bisinessList, maps);
        adapter.setAcceptListener(new BisinessModeratorAdapter.BlockListener() {
            @Override
            public void onClick(int position) {
                Log.i("main", "accept");
                Bisiness bisiness = bisinessList.get(position);
                pushData(bisiness, position);
            }
        });
        adapter.setDeclineListener(new BisinessModeratorAdapter.BlockListener() {
            @Override
            public void onClick(int position) {
                Bisiness bisiness = bisinessList.get(position);
                deletePicture(bisiness.getId());
                deleteDocument(bisiness.getId(), position);
            }
        });
        adapter.setCheckLocationListener(new BisinessModeratorAdapter.BlockListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getApplicationContext(), CheckLocationActivity.class);
                Bisiness bisiness = bisinessList.get(position);
                intent.putExtra(AddNewBisiness.LATITUDE, bisiness.getLatitude());
                intent.putExtra(AddNewBisiness.LONGITUDE, bisiness.getLongitude());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
    }

    private void deleteDocument(String id, final int pos){
        db.collection(LOAD_COLLECTION).document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        bisinessList.remove(pos);
                        maps.remove(pos);
                        //adapter.notifyItemRemoved(pos);
                        adapter.notifyDataSetChanged();

                        Log.d("main", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main", "Error deleting document", e);
                        Toast.makeText(getApplicationContext(), "не удалось удалить элемент", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deletePicture(String id){
        StorageReference storageRef = storage.getReference();
        StorageReference child = storageRef.child("images/" + id + ".jpeg");
        child.delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "не удалось удалить изображение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pushData(final Bisiness bisiness, final int pos){
        db.collection(FirebaseDatabase.COLLECTION_NAME)
                .add(bisiness)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        deleteDocument(bisiness.getId(), pos);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "не удалось добавить элемент", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
