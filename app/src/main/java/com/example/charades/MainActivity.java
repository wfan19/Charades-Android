package com.example.charades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private TextView tv_word;
    private Button btn_next;

    private DatabaseHelper dbh;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideUI();

        dbh = DatabaseHelper.getInstance(this);

        initWordBanks();

        if(tagList.size() ==0)
            ImportFromAsset();
        else
        {
            importFromDB("n1");
        }


        tv_word = findViewById(R.id.tv_word);
        btn_next = findViewById(R.id.btn_next);
        tv_word.setText("" + allWords.get(0));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
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
        done.add(allWords.remove(0));
        remaining = allWords.size() - 1;
        finished  = done.size() + 1;
        Log.d(TAG, "Finished words: " + finished);
        Log.d(TAG, "Words remaining: " + remaining);

        if(remaining == 0)
            reset();

        tv_word.setText("" + allWords.get(0));
    }

    private ArrayList<Word> bank = new ArrayList<>();
    private ArrayList<Word> allWords = new ArrayList<>();
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
                allWords.add(word);
            }
        }catch(IOException e){e.printStackTrace();}
        Log.d(TAG, "Shuffling words");
        Collections.shuffle(allWords);
        for(Word word : allWords)
            Log.d(TAG, "Word: " + word);
    }

    private void reset()
    {
        Log.d(TAG, "Resetting");
        allWords = (ArrayList<Word>)bank.clone();
        Collections.shuffle(allWords);

        for(Word word : allWords)
            Log.d(TAG, "Word: " + word);

        done.clear();
        finished = done.size() + 1;
        remaining = allWords.size() - 1;
        Log.d(TAG, "Finished words: " + finished);
        Log.d(TAG, "Words remaining: " + remaining);
    }

    private void clear()
    {
        Log.d(TAG, "Clearing words");
        done.clear();
        allWords.clear();
        bank.clear();
    }

    public final int FILE_REQUEST_CODE = 12345;
    public void Import()
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

    public void onActivityResult(int requestCode,int resultCode,final Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case FILE_REQUEST_CODE:
                    Log.d(TAG, "onActivityResult...");



                    final Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.enter_name_dialog);
                    final TextView confirm = dialog.findViewById(R.id.tv_confirm);
                    final TextView cancel = dialog.findViewById(R.id.tv_cancel);

                    final EditText word_bank_name = dialog.findViewById(R.id.wordbank_name_et);

                    dialog.setCanceledOnTouchOutside(false);


                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "Dialog confirm");
                            String name = word_bank_name.getText().toString();

                            clear();
                            Uri selectedFile = data.getData();
                            Log.d(TAG, "Path? " + selectedFile.getPath());
                            CsvReader reader = new CsvReader(MainActivity.this, selectedFile);
                            try {
                                reader.readFromFile();
                                for (String[] row: reader.getRows()) {
                                    Word word = new Word(row[0], row[1]);
                                    Log.d(TAG, "New word: " + word);
                                    bank.add(word);
                                    allWords.add(word);
                                    dbh.insertData(row[0],row[1],name);
                                }
                            }catch(IOException e){e.printStackTrace();}
                            Log.d(TAG, "Shuffling words");
                            Collections.shuffle(allWords);
                            for(Word word : allWords)
                                Log.d(TAG, "Word: " + word);

                            tv_word.setText("" + allWords.get(0));
                            remaining = allWords.size() - 1;
                            finished  = done.size() + 1;
                            Log.d(TAG, "Finished words: " + finished);
                            Log.d(TAG, "Words remaining: " + remaining);
                            wordBanks.add(new WordBank(MainActivity.this, allWords, name));
                            dialog.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "Dialog cancel");
                            dialog.dismiss();
                        }
                    });

                    dialog.show();



                    break;
            }
    }

    private WordBankAdapter adapter;
    public void menu(View view)
    {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_select:
                        Log.d(TAG, "menu_select");


                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.select_menu);

                        final RecyclerView recyclerView = dialog.findViewById(R.id.word_banks);
                        adapter = new WordBankAdapter(wordBanks,MainActivity.this);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);

                        dialog.show();
                        return true;

                    case R.id.menu_import:
                        Log.d(TAG, "menu_import");
                        Import();
                        return true;

                    case R.id.menu_clear:
                        Log.d(TAG, "menu_clear");
                        dbh.clear();
                        wordBanks.clear();
                        return true;

                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    private ArrayList<WordBank> wordBanks;
    private ArrayList<String> tagList;
    private void initWordBanks()
    {
        Cursor tags = dbh.getUnique("tag");
        tagList = new ArrayList<>();
        wordBanks = new ArrayList<>();
        if(tags.moveToFirst())
        {
            do{
                String tag = tags.getString(tags.getColumnIndex("tag"));
                Cursor bank = dbh.search(new String[]{"word1","word2"},"tag",tag);
                Log.d(TAG, "Current word bank: " + tag);
                tagList.add(tag);
                ArrayList<Word> importedWords = new ArrayList<>();
                if(bank.moveToFirst())
                {
                    do{
                        String word1 = bank.getString(bank.getColumnIndex("word1"));
                        String word2 = bank.getString(bank.getColumnIndex("word2"));
                        Word w = new Word(word1, word2);
                        Log.d(TAG, "Current word: "  + w);
                        importedWords.add(w);
                    }while(bank.moveToNext());//Loop through words for each tag
                }
                wordBanks.add(new WordBank(this,importedWords,tag));
                bank.close();
            }while (tags.moveToNext());//Loop through tags
        }

        tags.close();
    }

    public void importFromDB(String tag)
    {
        Log.d(TAG, "Importing from database; looking for " + tag);
        bank.clear();
        allWords.clear();

        for(WordBank wb : wordBanks) {
            Log.d(TAG, "Found wb " + wb.getTag());
            if (wb.getTag().equals(tag)) {
                Log.d(TAG, "------------------- This is it -------------------");
                ArrayList<Word> gotWords = wb.getWords();
                for (Word w : gotWords) {
                    Log.d(TAG, "Adding word " + w);
                    bank.add(w);
                    allWords.add(w);
                }
                Log.d(TAG, "Shuffling words");
                Collections.shuffle(allWords);
                for (Word word : allWords)
                    Log.d(TAG, "Word: " + word);
                Log.d(TAG, "------------------- Finished importing -------------------");
            }
        }
    }



}
