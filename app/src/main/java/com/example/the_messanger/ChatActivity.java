package com.example.the_messanger;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    RecyclerView chatRecyclerView;
    EditText messageBox;
    ImageView sendButton;
    MessageAdapter messageAdapter;
    List<Message> messageList;
    String receiverUid;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    FirebaseApp.initializeApp(this);

    toolbar = findViewById(R.id.toolbar_chat);
    setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");

        getSupportActionBar().setTitle(name);

        messageList = new ArrayList<>();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageBox = findViewById(R.id.messageBox);
        sendButton = findViewById(R.id.sentButton);

        messageAdapter = new MessageAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);
        chatRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        loadMessages();

        sendButton.setOnClickListener(view -> {
            String messageText = messageBox.getText().toString();
            if(messageText.isEmpty()) return;

            sendMessage(messageText);
            messageBox.setText("");
        });
    }

    private void loadMessages() {
        try {
            String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String chatId = senderUid.compareTo(receiverUid) < 0 ? senderUid + receiverUid : receiverUid + senderUid;

            FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            messageList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Message message = dataSnapshot.getValue(Message.class);
                                messageList.add(message);
                            }
                            messageAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            // Firebase not available
        }
    }

    private void sendMessage(String messageText) {
        try {
            String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String chatId = senderUid.compareTo(receiverUid) < 0 ? senderUid + receiverUid : receiverUid + senderUid;

            String messageId = FirebaseDatabase.getInstance().getReference().push().getKey();
            Message message = new Message(messageId, senderUid, messageText, System.currentTimeMillis());

            FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages").child(messageId).setValue(message);
        } catch (Exception e) {
            // Firebase not available
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}