package com.mehdi.firstindellpc.MESSAGING.CHATROOM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mehdi.firstindellpc.R;

import java.util.ArrayList;

public class AdapterPersonMessaging extends RecyclerView.Adapter<AdapterPersonMessaging.holder> {

private ArrayList<PersonMessage> dataMessage = null;

public AdapterPersonMessaging(clickMessagingPerson click) {
        this.click = click;
        }

public void swapAdapter(ArrayList<PersonMessage> data){
        if (dataMessage == data) return;
        this.dataMessage = data;
        if (data != null){
        this.notifyDataSetChanged();
        }
        }

public interface clickMessagingPerson{
    void clickAllItem(String chiledName);
}

    private final clickMessagingPerson click;

class holder extends RecyclerView.ViewHolder{

    ImageView userI;
    TextView name;
    TextView lastMsg;
    public holder(@NonNull View itemView) {
        super(itemView);
        userI = itemView.findViewById(R.id.img);
        name = itemView.findViewById(R.id.username);
        lastMsg = itemView.findViewById(R.id.lastmsg);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click.clickAllItem(dataMessage.get(getAdapterPosition()).getId());
            }
        });
    }
}

Context context;
    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        return new holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int i) {
        PersonMessage userInfo = dataMessage.get(i);

        if (userInfo.getPhoto() != null){
            Glide.with(context).load(userInfo.getPhoto()).into(holder.userI);
        }

        if (userInfo.getName()!=null){
            holder.name.setText(userInfo.getName());
        }

        if (userInfo.getMsg() != null){
            holder.lastMsg.setText(userInfo.getMsg());
        }

    }

    @Override
    public int getItemCount() {
        if (dataMessage == null){
            return 0;
        }
        return dataMessage.size();
    }

}
