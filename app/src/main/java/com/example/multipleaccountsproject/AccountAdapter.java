package com.example.multipleaccountsproject;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter {

    ArrayList<Account> accountArrayList;

    final static int NORMAL_ACCOUNT_VIEW = 1;
    final static int UPILITE_ACCOUNT_VIEW = 2;

    int lastSelectedPosition = 0; // Set default selection to position 0

    public AccountAdapter(ArrayList<Account> accountArrayList) {
        this.accountArrayList = accountArrayList;
    }

    @NonNull
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
        if (holder.getItemViewType() == UPILITE_ACCOUNT_VIEW) {
            if (accountArrayList.get(position) instanceof Account)
                ((UpiLiteAccountViewHolder) holder).Bind((Account) accountArrayList.get(position), position);
        } else {
            if (accountArrayList.get(position) instanceof Account)
                ((NormalAccountViewHolder) holder).Bind((Account) accountArrayList.get(position), position);
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

    public void updateSelection(int position) {
        lastSelectedPosition = position;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public class NormalAccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;
        RadioButton normalAccountRadioButton;

        public NormalAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.normal_account_name);
            normalAccountRadioButton = itemView.findViewById(R.id.normal_radioBttn);

            normalAccountRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        lastSelectedPosition = getAdapterPosition();
                        updateSelection(lastSelectedPosition);
                    }
                }
            });
        }

        public void Bind(Account account, int position) {
            accountName.setText(account.getAccountName());
            normalAccountRadioButton.setChecked(position == lastSelectedPosition);
        }
    }

    public class UpiLiteAccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;
        RadioButton upiLiteAccountRadioButton;

        public UpiLiteAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.lite_account_name);
            upiLiteAccountRadioButton = itemView.findViewById(R.id.lite_radioBtn);

            upiLiteAccountRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        lastSelectedPosition = getAdapterPosition();
                        updateSelection(lastSelectedPosition);
                    }
                }
            });
        }

        public void Bind(Account account, int position) {
            accountName.setText(account.getAccountName());
            upiLiteAccountRadioButton.setChecked(position == lastSelectedPosition);
        }
    }
}
