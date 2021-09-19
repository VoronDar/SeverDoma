package com.mribi.severdoma.Activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mribi.severdoma.R;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mribi.severdoma.Activities.MapsActivity.*;

public class FilterSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_filter_select);

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(new FilterAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(MapsActivity.FILTER_TYPE_PARAM, position);
                setResult(FILTER_RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_back);
            }
        });
    }


    class FilterAdapter extends BaseAdapter {

        LayoutInflater lInflater;
        String[] allThings;


        FilterAdapter() {
            lInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            allThings = new String[MAX_TYPE+2];
            allThings[0] = "Все предприятия";
            String[] now = getResources().getStringArray(R.array.types);
            for (int i = 0; i < now.length; i++){
                allThings[i+1] = now[i];
            }
        }

        private String[] getArray(){
            return allThings;
        }

        @Override
        public int getCount() {
            return getArray().length;
        }

        @Override
        public Object getItem(int position) {
            return getArray()[position];
        }

        // id по позиции
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = lInflater.inflate(R.layout.filter_unit, parent, false);
            TextView label = row.findViewById(R.id.label);
            ImageView icon = row.findViewById(R.id.filter_icon);
            label.setText(allThings[position]);
            icon.setImageDrawable(getResources().getDrawable(getIcon(position)));

            return row;
        }




        private int getIcon(int pos){
            int type = pos;
            switch (type) {
                case ICON_JAPAN:
                    return R.drawable.icon_1;
                case ICON_FAST:
                    return R.drawable.icon_2;
                case ICON_FLOWER:
                    return R.drawable.icon_3;
                case ICON_PIZZA:
                    return R.drawable.icon_15;
                case ICON_FISH:
                    return R.drawable.icon_4;
                case ICON_WATER:
                    return R.drawable.icon_5;
                case ICON_RESTAURANT:
                    return R.drawable.icon_6;
                case ICON_ICE_SCREAM:
                    return R.drawable.icon_7;
                case ICON_ANIMAL:
                    return R.drawable.icon_8;
                case ICON_MED:
                    return R.drawable.icon_9;
                case ICON_GYGIENIC:
                    return R.drawable.icon_10;
                case ICON_TOOLS:
                    return R.drawable.icon_11;
                case ICON_TECHNIQUE:
                    return R.drawable.icon_12;
                case ICON_FOOD:
                    return R.drawable.icon_14;
                case ICON_CLOTHES:
                    return R.drawable.icon_13;
                default:
                    return R.drawable.icon_differ;
            }
        }


    }
}
