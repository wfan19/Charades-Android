package com.example.charades;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private TextView tv_word;
    private Button btn_next;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Import();

        tv_word = findViewById(R.id.tv_word);
        btn_next = findViewById(R.id.btn_next);
        tv_word.setText("" + words.get(0));

    }

    int remaining;
    int finished;
    public void Next(View view)
    {
        done.add(words.remove(0));
        remaining = words.size() - 1;
        finished  = done.size() + 1;
        Log.d(TAG, "Finished words: " + finished);
        Log.d(TAG, "Words remaining: " + remaining);

        if(remaining == 0)
            reset();

        tv_word.setText("" + words.get(0));
    }

    private ArrayList<Word> bank = new ArrayList<>();
    private ArrayList<Word> words = new ArrayList<>();
    private ArrayList<Word> done = new ArrayList<>();

    private void Import()
    {
        try {

            CsvReader reader = new CsvReader(this,"words.csv");
            reader.read();
                for (String[] row: reader.getRows()) {
                    Word word = new Word(row[0], row[1]);
                    Log.d(TAG, "New word: " + word);
                    bank.add(word);
                    words.add(word);
                }
        }catch(IOException e){e.printStackTrace();}
        Log.d(TAG, "Shuffling");
        Collections.shuffle(words);
        for(Word word : words)
            Log.d(TAG, "Word: " + word);
    }

    private void reset()
    {
        Log.d(TAG, "Resetting");
        words = (ArrayList<Word>)bank.clone();
        Collections.shuffle(words);

        for(Word word : words)
            Log.d(TAG, "Word: " + word);

        done.clear();
    }
}
