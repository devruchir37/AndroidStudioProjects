package com.example.multipleaccountsproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class IotManageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpandableAdapter adapter;
    private List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_manage);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        // Add items to your list
        itemList.add(new Item("Item 1", "Details for item 1"));
        itemList.add(new Item("Item 2", "Details for item 2"));

        adapter = new ExpandableAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }
}