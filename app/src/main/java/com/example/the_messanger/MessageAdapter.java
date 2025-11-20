package com.example.the_messanger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private static final int ITEM_SENT = 1;
    private static final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recive, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.txtMessage.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        try {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (message.getSenderId().equals(currentUserId)) {
                return ITEM_SENT;
            } else {
                return ITEM_RECEIVE;
            }
        } catch (Exception e) {
            return ITEM_RECEIVE; // Default
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_sent_message);
            if (txtMessage == null) {
                txtMessage = itemView.findViewById(R.id.txt_receive_message);
            }
        }
    }
}
