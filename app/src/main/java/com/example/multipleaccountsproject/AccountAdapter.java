package com.example.multipleaccountsproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter {

    ArrayList<Account> accountArrayList;

    final static int NORMAL_ACCOUNT_VIEW = 1;
    final static int UPILITE_ACCOUNT_VIEW = 2;

    public AccountAdapter(ArrayList<Account> accountArrayList) {
        this.accountArrayList = accountArrayList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case NORMAL_ACCOUNT_VIEW:
                return new NormalAccountViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_item, parent, false));
            case UPILITE_ACCOUNT_VIEW:
                return new UpiLiteAccountViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_upilite, parent, false));
            default:
                return new NormalAccountViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (position==0){
            if (accountArrayList.get(position) instanceof Account)
                ((UpiLiteAccountViewHolder) holder).Bind((Account) accountArrayList.get(position));
        }else {
            if (accountArrayList.get(position) instanceof Account)
                ((NormalAccountViewHolder) holder).Bind((Account) accountArrayList.get(position));
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (accountArrayList.get(position).isUpiLiteEnabled.equals("Y"))
            return UPILITE_ACCOUNT_VIEW;
        else
            return NORMAL_ACCOUNT_VIEW;

    }

    @Override
    public int getItemCount() {
        return accountArrayList.size();
    }

    public class NormalAccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;
        RadioButton normalAccountRadioButtonn;

        public NormalAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.normal_account_name);
            normalAccountRadioButtonn = itemView.findViewById(R.id.normal_radioBttn);
        }

        public void Bind(Account account) {
            accountName.setText(account.getAccountName());

        }
    }

    public class UpiLiteAccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;
        RadioButton upiLiteAccountRadioButtonn;

        public UpiLiteAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.lite_account_name);
            upiLiteAccountRadioButtonn = itemView.findViewById(R.id.lite_radioBtn);
        }

        public void Bind(Account account) {
            accountName.setText(account.getAccountName());
        }
    }
}