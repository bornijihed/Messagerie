package com.example.the_messanger;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private OnReplyListener listener;
    private static final int ITEM_SENT = 1;
    private static final int ITEM_RECEIVE = 2;

    public interface OnReplyListener {
        void onReply(Message message);
    }

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setOnReplyListener(OnReplyListener listener) {
        this.listener = listener;
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

        if (message.getReplyToText() != null && !message.getReplyToText().isEmpty()) {
            holder.replyLayout.setVisibility(View.VISIBLE);
            holder.txtReplyText.setText(message.getReplyToText());
        } else {
            holder.replyLayout.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            // Show options dialog
            showOptionsDialog(context, message);
            return true;
        });
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
        LinearLayout replyLayout;
        TextView txtReplyText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_sent_message);
            if (txtMessage == null) {
                txtMessage = itemView.findViewById(R.id.txt_receive_message);
            }
            replyLayout = itemView.findViewById(R.id.reply_layout);
            txtReplyText = itemView.findViewById(R.id.txt_reply_text);
        }
    }

    private void showOptionsDialog(Context context, Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Message Options");
        String[] options = {"Reply", "Delete"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Reply
                if (listener != null) {
                    listener.onReply(message);
                }
            } else if (which == 1) { // Delete
                deleteMessage(message);
            }
        });
        builder.show();
    }

    private void deleteMessage(Message message) {
        try {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (!message.getSenderId().equals(currentUserId)) {
                // Only allow delete if sender
                return;
            }
            String receiverUid = ((ChatActivity) context).getReceiverUid();
            String chatId = currentUserId.compareTo(receiverUid) < 0 ? currentUserId + receiverUid : receiverUid + currentUserId;
            FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages").child(message.getMessageId()).removeValue();
        } catch (Exception e) {
            // Handle error
        }
    }
}
