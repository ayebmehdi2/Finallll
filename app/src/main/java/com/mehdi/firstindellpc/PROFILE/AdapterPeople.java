package com.mehdi.firstindellpc.PROFILE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehdi.firstindellpc.R;

import java.util.ArrayList;

public class AdapterPeople extends RecyclerView.Adapter<AdapterPeople.holder> {


    private int type;
    private Context context;

    private ArrayList<profilData> dataPerson = null;

    public interface clickPrson{
        void select(String uid);
        void add(String uid);
        void msg(String uid);
    }

    private final clickPrson clickPrson;

    public AdapterPeople(clickPrson clickPrson, int t){
        this.clickPrson = clickPrson;
        type = t;
    }

    public void swapAdapter(ArrayList<profilData> data){
        if (dataPerson == data) return;
        this.dataPerson = data;
        if (data != null){
            this.notifyDataSetChanged();
        }
    }

    class holder extends RecyclerView.ViewHolder{

        ImageView userImage;
        TextView userName;
        RelativeLayout add;
        RelativeLayout msg;
        ImageView bT;
        public holder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.i);
            userName = itemView.findViewById(R.id.n);
            add = itemView.findViewById(R.id.add_p);
            msg = itemView.findViewById(R.id.msg_p);
            bT = itemView.findViewById(R.id.bl_tyyy);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickPrson.select(dataPerson.get(getAdapterPosition()).getUid());
                }
            });

            if (type == 0){
                add.setVisibility(View.GONE);
                msg.setVisibility(View.VISIBLE);
                msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickPrson.msg(dataPerson.get(getAdapterPosition()).getUid());
                    }
                });
            }else if(type == 1){
                add.setVisibility(View.VISIBLE);
                msg.setVisibility(View.GONE);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickPrson.add(dataPerson.get(getAdapterPosition()).getUid());
                    }
                });
            }



        }
    }

    @NonNull
    @Override
    public  holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        return new  holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_people, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull  holder holder, int i) {
        profilData data = dataPerson.get(i);

        if(type == 23){
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("PROFILES").child(data.getName()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        profilData data = dataSnapshot.getValue(profilData.class);
                        if (data == null) return;
                        holder.userName.setText(data.getName());
                        if (data.getPhoto() == null) return;
                        if (data.getPhoto().length() > 0){
                            Glide.with(context).load(data.getPhoto()).into(holder.userImage);
                        }
                        if (data.getBloodType().length() > 0) whatBlood(data.getBloodType(), holder.bT);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }
        else {
            holder.userName.setText(data.getName());
            if (data.getPhoto() == null) return;
            if (data.getPhoto().length() > 0){
                Glide.with(context).load(data.getPhoto()).into(holder.userImage);
            }
            if (data.getBloodType().length() > 0) whatBlood(data.getBloodType(), holder.bT);

        }


    }

    @Override
    public int getItemCount() {
        if (dataPerson == null){
            return 0;
        }
        return dataPerson.size();
    }

    public void whatBlood(String s, ImageView bloodTy){
        switch (s){
            case "A" :
                bloodTy.setImageResource(R.drawable.ic_a);
                break;
            case "B" :
                bloodTy.setImageResource(R.drawable.ic_b);
                break;
            case "O" :
                bloodTy.setImageResource(R.drawable.ic_o);
                break;
            case "AB" :
                bloodTy.setImageResource(R.drawable.ic_ab);
                break;
        }
    }

}