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

import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.util.ArrayList;

public class BisinessAdapter extends RecyclerView.Adapter<BisinessAdapter.ButtonLetterViewHolder>{

    private ArrayList<Bisiness> units;
    private ArrayList<Bitmap> bitmaps;
    private BlockListener blockListener;


    public Bisiness getUnit(int position){
        return units.get(position);
    }

    public BisinessAdapter(Context context, ArrayList<Bisiness> blocks, ArrayList<Bitmap> bitmaps) {
        resetUnits(blocks);
        this.bitmaps = bitmaps;
    }

    public interface BlockListener {
        void onClick(int position);

    }

    public void reset(){
        this.bitmaps = new ArrayList<>();
        this.units = new ArrayList<>();
        notifyDataSetChanged();
    }
    public void resetBitmaps(ArrayList<Bitmap> newUnits){
        this.bitmaps = newUnits;
    }
    public void resetUnits(ArrayList<Bisiness> newUnits){
        this.units = newUnits;
        notifyDataSetChanged();
    }

    public ArrayList<Bisiness> getUnits() {
        return units;
    }

    public void setBlockListener(BlockListener block_listener) {
        this.blockListener = block_listener;
    }

    @NonNull
    @Override
    public ButtonLetterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bisiness_card, viewGroup, false);
        return new ButtonLetterViewHolder(view);

    }

    public Bitmap getNowBitmap(int pos){
        return bitmaps.get(pos);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonLetterViewHolder holder, int position) {
        Bisiness unit = units.get(position);
        holder.setIsRecyclable(false);
        holder.name.setText(unit.getName());
        if (bitmaps.size() > position) {
            if (bitmaps.get(position) != null)
                holder.image.setImageBitmap(bitmaps.get(position));
        }
        Log.i("main", bitmaps.size() + " " + position);
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    class ButtonLetterViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView image;

        ButtonLetterViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
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
