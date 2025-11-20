package com.example.the_messanger;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
        getSupportActionBar().setTitle("Messenger");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userList = new ArrayList<>();
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userAdapter = new UserAdapter(this, userList);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(userAdapter);
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
}