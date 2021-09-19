package com.mribi.severdoma.Adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mribi.severdoma.R;
import com.mribi.severdoma.pojo.Bisiness;

import java.util.ArrayList;

import static android.view.View.GONE;

public class BisinessModeratorAdapter extends RecyclerView.Adapter<BisinessModeratorAdapter.ButtonLetterViewHolder>{

    private ArrayList<Bisiness> units;
    private ArrayList<Bitmap> maps;
    private BlockListener acceptListener;
    private BlockListener declineListener;
    private BlockListener checkLocationListener;
    private Context context;
    private int openPage;


    public Bisiness getUnit(int position){
        return units.get(position);
    }

    public BisinessModeratorAdapter(Context context, ArrayList<Bisiness> blocks, ArrayList<Bitmap> bitmaps) {
        resetUnits(blocks);
        this.context = context;
        this.openPage = -1;
        this.maps = bitmaps;
    }

    public interface BlockListener {
        public void onClick(int position);

    }

    public void resetUnits(ArrayList<Bisiness> newUnits){
        this.units = newUnits;
        notifyDataSetChanged();
    }

    public ArrayList<Bisiness> getUnits() {
        return units;
    }

    public void setAcceptListener(BlockListener block_listener) {
        this.acceptListener = block_listener;
    }
    public void setDeclineListener(BlockListener block_listener) {
        this.declineListener = block_listener;
    }

    public void setCheckLocationListener(BlockListener block_listener) {
        this.checkLocationListener = block_listener;
    }

    @NonNull
    @Override
    public ButtonLetterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bisiness_card_add, viewGroup, false);
        return new ButtonLetterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ButtonLetterViewHolder holder, final int position) {
        Bisiness unit = units.get(position);
        holder.setIsRecyclable(false);
        holder.name.setText(unit.getName());
        holder.description.setText(unit.getDescription());
        holder.type.setText(context.getResources().getStringArray(R.array.types)[unit.getType()-1]);
        if (maps.size() > position) {
            if (maps.get(position) != null)
                holder.image.setImageBitmap(maps.get(position));
        }
        Log.i("main", Integer.toString(position));


        if (unit.getPhoneNumber() != null && unit.getPhoneNumber().length() > 1){
            holder.phone.setText(unit.getPhoneNumber());
        } else
            holder.phone.setVisibility(GONE);

        if (unit.getMail() != null && unit.getMail().length() > 1){
            holder.mail.setText("сайт: " + unit.getMail());
        } else
            holder.mail.setVisibility(GONE);

        if (unit.getVk() != null && unit.getVk().length() > 1){
            holder.vk.setText("ВКонтакте: " + unit.getVk());
        } else
            holder.vk.setVisibility(GONE);

        if (unit.getInsta() != null && unit.getInsta().length() > 1){
            holder.insta.setText("Инстаграм: " + unit.getInsta());
        } else
            holder.insta.setVisibility(GONE);

        if (unit.getFb() != null && unit.getFb().length() > 1){
            holder.fb.setText("FaceBook: " + unit.getFb());
        } else
            holder.fb.setVisibility(GONE);

        if (unit.getAddress() != null && unit.getAddress().length() > 1){
            holder.address.setText("Адресс: " + unit.getAddress());
        } else
            holder.address.setVisibility(GONE);




        if (openPage == position){
            holder.description.setVisibility(View.VISIBLE);
            holder.phone.setVisibility(View.VISIBLE);
            holder.mail.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.VISIBLE);
            holder.border1.setVisibility(View.VISIBLE);
            holder.border2.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.VISIBLE);
        }
        else{
            holder.description.setVisibility(View.GONE);
            holder.phone.setVisibility(View.GONE);
            holder.mail.setVisibility(View.GONE);
            holder.location.setVisibility(View.GONE);
            holder.border1.setVisibility(View.GONE);
            holder.border2.setVisibility(View.GONE);
            holder.image.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openPage != position)
                    openPage = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    class ButtonLetterViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView description;
        private TextView phone;
        private TextView mail;
        private TextView type;
        private View itemView;
        private Button location;
        private View border1;
        private View border2;
        private ImageView image;
        private TextView address;
        private TextView vk;
        private TextView insta;
        private TextView fb;

        public ButtonLetterViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            phone = itemView.findViewById(R.id.phone);
            mail = itemView.findViewById(R.id.mail);
            type = itemView.findViewById(R.id.type);
            location = itemView.findViewById(R.id.location);
            border1 = itemView.findViewById(R.id.border1);
            border2 = itemView.findViewById(R.id.border2);
            image = itemView.findViewById(R.id.image);
            fb = itemView.findViewById(R.id.label_fb);
            insta = itemView.findViewById(R.id.label_insta);
            address = itemView.findViewById(R.id.address);
            vk = itemView.findViewById(R.id.label_vk);
            this.itemView = itemView;

            final ImageView accept =  itemView.findViewById(R.id.accept);
            final ImageView decline =  itemView.findViewById(R.id.decline);

            accept.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (acceptListener != null)
                        acceptListener.onClick(getAdapterPosition());
                }});
            decline.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (declineListener != null)
                        declineListener.onClick(getAdapterPosition());
                }});

            location.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (checkLocationListener != null)
                        checkLocationListener.onClick(getAdapterPosition());
                }});
        }
    }
}
