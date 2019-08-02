package com.example.charades;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

public class WordBank {

    private ArrayList<Word> words;
    private Context mContext;
    private String tag;

    private static final String TAG = "WordBank";

    public WordBank(Context ctx, ArrayList<Word> w, String t)
    {
        mContext = ctx;
        words = w;
        tag = t;
    }

    public void addWord(Word word)
    {
        words.add(word);
    }

    public ArrayList<Word> getWords(){return words;}

    public int getSize(){return words.size();}

    public String getTag(){return tag;}

    @Override
    public String toString(){return words.toString();}

}
