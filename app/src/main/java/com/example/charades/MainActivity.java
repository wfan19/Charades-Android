package com.example.charades;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

        hideUI();

        ImportFromAsset();

        tv_word = findViewById(R.id.tv_word);
        btn_next = findViewById(R.id.btn_next);
        tv_word.setText("" + words.get(0));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hideUI();
    }

    private void hideUI()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
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

    private void ImportFromAsset()
    {
        try {

            CsvReader reader = new CsvReader(this,"words.csv");
            reader.readFromAsset();
            for (String[] row: reader.getRows()) {
                Word word = new Word(row[0], row[1]);
                Log.d(TAG, "New word: " + word);
                bank.add(word);
                words.add(word);
            }
        }catch(IOException e){e.printStackTrace();}
        Log.d(TAG, "Shuffling words");
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
        finished = done.size() + 1;
        remaining = words.size() - 1;
        Log.d(TAG, "Finished words: " + finished);
        Log.d(TAG, "Words remaining: " + remaining);
    }

    private void clear()
    {
        Log.d(TAG, "Clearing words");
        done.clear();
        words.clear();
        bank.clear();
    }

    public final int FILE_REQUEST_CODE = 12345;
    public void Import(View view)
    {
        Log.d(TAG, "Import()...");
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("*/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        //String[] mimeTypes = {"file/csv"};
        //intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,FILE_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case FILE_REQUEST_CODE:
                    Log.d(TAG, "onActivityResult...");
                    clear();
                    Uri selectedFile = data.getData();
                    Log.d(TAG, "Path? " + selectedFile.getPath());
                    CsvReader reader = new CsvReader(this, selectedFile);
                    try {
                        reader.readFromFile();
                        for (String[] row: reader.getRows()) {
                            Word word = new Word(row[0], row[1]);
                            Log.d(TAG, "New word: " + word);
                            bank.add(word);
                            words.add(word);
                        }
                    }catch(IOException e){e.printStackTrace();}
                    Log.d(TAG, "Shuffling words");
                    Collections.shuffle(words);
                    for(Word word : words)
                        Log.d(TAG, "Word: " + word);

                    tv_word.setText("" + words.get(0));
                    remaining = words.size() - 1;
                    finished  = done.size() + 1;
                    Log.d(TAG, "Finished words: " + finished);
                    Log.d(TAG, "Words remaining: " + remaining);

                    break;
            }
    }

}
