package com.example.charades;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WordBankAdapter  extends RecyclerView.Adapter<WordBankAdapter.ViewHolder>{
    //TODO: Literally make the entire class for the wordbank recycler view

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout LL;
        TextView tv_bankName;
        CheckBox cb;

        public ViewHolder(View view){
            super(view);
            tv_bankName = view.findViewById(R.id.tv_wordBank);
            LL = view.findViewById(R.id.linear_layout);
            cb = view.findViewById(R.id.checkBox);
        }
    }

    private Context mContext;
    private ArrayList<WordBank> wordBanks;
    public WordBankAdapter(ArrayList<WordBank>wbs, Context c)
    {
        mContext = c;
        wordBanks = wbs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordbank_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final WordBank wordBank = wordBanks.get(position);
        holder.tv_bankName.setText(wordBank.getTag());

    }

    @Override
    public int getItemCount() {
        return wordBanks.size();
    }
}
