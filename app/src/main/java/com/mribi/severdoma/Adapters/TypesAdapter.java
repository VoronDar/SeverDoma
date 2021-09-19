package com.mribi.severdoma.Adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mribi.severdoma.Activities.MapsActivity;
import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.mribi.severdoma.Activities.MapsActivity.MAX_TYPE;

public class TypesAdapter extends RecyclerView.Adapter<TypesAdapter.ButtonLetterViewHolder>{

    private ArrayList<Boolean> units;
    private BlockListener blockListener;
    private Context context;


    public TypesAdapter(Context context) {
        notifyDataSetChanged();
        this.context = context;

        units = new ArrayList<>(MAX_TYPE);
        for (int i = 1; i <= MAX_TYPE; i++){
            units.add(false);
        }
        notifyDataSetChanged();
    }

    public interface BlockListener {
        void onClick(int position);

    }
    public void setBlockListener(BlockListener block_listener) {
        this.blockListener = block_listener;
    }

    public boolean getPressed(int pos){
        return units.get(pos);
    }
    public void setPressed(int pos, boolean pressed){
        units.set(pos, pressed);
        notifyItemChanged(pos);
    }

    @NonNull
    @Override
    public ButtonLetterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_by_type_unit, viewGroup, false);
        return new ButtonLetterViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull ButtonLetterViewHolder holder, int position) {
        holder.image.setImageDrawable(context.getResources().getDrawable(MapsActivity.getIcon(position+1)));
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    class ButtonLetterViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;

        ButtonLetterViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (blockListener != null)
                        blockListener.onClick(getAdapterPosition());
                }});
        }
    }
}
