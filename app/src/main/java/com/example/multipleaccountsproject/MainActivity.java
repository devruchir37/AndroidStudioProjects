package com.example.multipleaccountsproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AccountAdapter accountAdapter;
    ArrayList<Account> accountArrayList = new ArrayList<>();
    Button withlite, withoutlite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        LoadData();
        recyclerView = findViewById(R.id.recycler);
        withlite = findViewById(R.id.withlite);
        withoutlite = findViewById(R.id.withoutlite);
        accountAdapter = new AccountAdapter(accountArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(accountAdapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        withoutlite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountAdapter.updateSelection(1);
            }
        });


        withlite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountAdapter.updateSelection(0);
            }
        });

    }

    void LoadData() {


        Account upiLiteAccount = new Account();
        upiLiteAccount.setAccountName("sbiliteaccoujnt");
        upiLiteAccount.setIsUpiLiteEnabled("Y");
        accountArrayList.add(upiLiteAccount);


        for (int i = 0; i < 5; i++) {
            Account account = new Account();
            account.setAccountName("SBI" + i);
            account.setIsUpiLiteEnabled("N");
            accountArrayList.add(account);
        }


    }
}