package com.example.the_messanger;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView userRecyclerView;
    UserAdapter userAdapter;
    List<User> userList;
    TextView noUsersText;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set title to current user's name
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            getSupportActionBar().setTitle(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userList = new ArrayList<>();
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userAdapter = new UserAdapter(this, userList);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(userAdapter);
        userRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        noUsersText = findViewById(R.id.noUsersText);

        loadUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsers() {
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Map<String, Long> lastMessageTimes = new HashMap<>();

    // Listen to chats to get last message times
    FirebaseDatabase.getInstance().getReference().child("chats")
    .addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot chatsSnapshot) {
    lastMessageTimes.clear();
    for (DataSnapshot chatSnapshot : chatsSnapshot.getChildren()) {
        String chatId = chatSnapshot.getKey();
        if (chatId.contains(currentUserId)) {
            String otherUid = getOtherUid(chatId, currentUserId);
        if (otherUid != null) {
                DataSnapshot messagesSnapshot = chatSnapshot.child("messages");
            long lastTime = 0;
                for (DataSnapshot msgSnapshot : messagesSnapshot.getChildren()) {
                        Message msg = msgSnapshot.getValue(Message.class);
                                        if (msg != null && msg.getTimestamp() > lastTime) {
                            lastTime = msg.getTimestamp();
                        }
                                    }
                    if (lastTime > 0) {
                            lastMessageTimes.put(otherUid, lastTime);
                                    }
                                }
                            }
                        }
                        loadUserList(lastMessageTimes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUserList(Map<String, Long> lastMessageTimes) {
        FirebaseDatabase.getInstance().getReference().child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                userList.add(user);
                            }
                        }
                        // Sort by last message time descending
                        userList.sort((u1, u2) -> {
                            long t1 = lastMessageTimes.getOrDefault(u1.getUid(), 0L);
                            long t2 = lastMessageTimes.getOrDefault(u2.getUid(), 0L);
                            return Long.compare(t2, t1); // Descending
                        });
                        userAdapter.notifyDataSetChanged();
                        if (userList.isEmpty()) {
                            noUsersText.setVisibility(View.VISIBLE);
                        } else {
                            noUsersText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String getOtherUid(String chatId, String currentUserId) {
        String[] parts = chatId.split(currentUserId);
        if (parts.length == 2) {
            return parts[0].isEmpty() ? parts[1] : parts[0];
        }
        return null;
    }
}