package com.example.charades;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WordBankAdapter  extends RecyclerView.Adapter<WordBankAdapter.ViewHolder>{

    private static final String TAG = "WordBankAdapter";

    static class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout RL;
        TextView tv_bankName;
        CheckBox cb;


        public ViewHolder(View view){
            super(view);
            tv_bankName = view.findViewById(R.id.tv_wordBank);
            RL = view.findViewById(R.id.relative_layout);
            cb = view.findViewById(R.id.checkBox);
        }
    }

    private Context mContext;
    private ArrayList<WordBank> wordBanks;
    private DatabaseHelper dbh;
    public WordBankAdapter(ArrayList<WordBank>wbs, Context c, DatabaseHelper databaseHelper)
    {
        mContext = c;
        wordBanks = wbs;
        dbh = databaseHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordbank_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder");
        final String current = dbh.getCurrent();
        final WordBank wordBank = wordBanks.get(position);
        holder.tv_bankName.setText(wordBank.getTag());

        if(holder.tv_bankName.getText().equals(current))
        {
            holder.cb.setChecked(true);
            holder.RL.setBackgroundColor(Color.parseColor("#33009688"));
        }
        else
        {
            holder.cb.setChecked(false);
            holder.RL.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        }

        holder.RL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!holder.tv_bankName.getText().equals(current))
                {
                    holder.cb.setChecked(false);
                    dbh.setCurrent((String)holder.tv_bankName.getText());
                    Intent i  = new Intent("com.example.charades.UPDATE");
                    mContext.sendBroadcast(i);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordBanks.size();
    }
}
