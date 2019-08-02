package com.example.charades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

        Log.d(TAG, "Current is " + dbh.getCurrent());

        ImportFromAsset();
        Log.d(TAG, "Possible tags: " + tagList.toString());
        if(dbh.getCurrent() == null)
            dbh.createCurrent(tagList.get(0));
        importFromDB(dbh.getCurrent());


        tv_word = findViewById(R.id.tv_word);
        btn_next = findViewById(R.id.btn_next);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(tv_word, 12, 100, 1,
                TypedValue.COMPLEX_UNIT_DIP);

        tv_word.setText("" + allWords.get(0));

        registerReceiver(mBroadcastReceiver, new IntentFilter("com.example.charades.UPDATE"));
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
        if(tagList.size() == 0) {
            AssetManager assetManager = getAssets();
            try {
                String[] files = assetManager.list("csv");
                Log.d(TAG, "CSV's: " + Arrays.toString(files));
                ArrayList<Word> w = new ArrayList<>();
                for (String p : files) {
                    w.clear();
                    CsvReader reader = new CsvReader(this, p);
                    reader.readFromAsset();
                    for (String[] row : reader.getRows()) {
                        Word word = new Word(row[0], row[1]);
                        Log.d(TAG, "New word: " + word);
                        dbh.insertData(row[0], row[1], p);
                        w.add(word);
                    }
                    if(dbh.search(new String[]{"word2"},"word1","current").getCount()==0)
                        dbh.createCurrent(p);
                    else{dbh.setCurrent(p);}
                    wordBanks.add(new WordBank(this,w,p));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Current is now " + dbh.getCurrent());
        }
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
                            if (!(name.equals(""))) {
                                if(dbh.search(new String[]{"tag"},"tag",name).getCount() == 0) {
                                    clear();
                                    Uri selectedFile = data.getData();
                                    Log.d(TAG, "Path? " + selectedFile.getPath());
                                    CsvReader reader = new CsvReader(MainActivity.this, selectedFile);
                                    try {
                                        reader.readFromFile();
                                        for (String[] row : reader.getRows()) {
                                            Word word = new Word(row[0], row[1]);
                                            Log.d(TAG, "New word: " + word);
                                            bank.add(word);
                                            allWords.add(word);
                                            dbh.insertData(row[0], row[1], name);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "Shuffling words");
                                    Collections.shuffle(allWords);
                                    for (Word word : allWords)
                                        Log.d(TAG, "Word: " + word);

                                    tv_word.setText("" + allWords.get(0));
                                    remaining = allWords.size() - 1;
                                    finished = done.size() + 1;
                                    Log.d(TAG, "Finished words: " + finished);
                                    Log.d(TAG, "Words remaining: " + remaining);
                                    wordBanks.add(new WordBank(MainActivity.this, allWords, name));

                                    dbh.setCurrent(name);
                                    Log.d(TAG, "Current is now " + dbh.getCurrent());
                                    dialog.dismiss();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Words added!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "That name is already in use", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            } else
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                                    }
                                });
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
                        adapter = new WordBankAdapter(wordBanks,MainActivity.this,dbh);
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
                        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Warning")
                                .setMessage("You are about to delete every word bank.\n" +
                                        "Are you sure you want to continue?")
                                .create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbh.clear();
                                wordBanks.clear();
                            }
                        });
                        alertDialog.show();
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
                if(tag!=null) {
                    Cursor bank = dbh.search(new String[]{"word1", "word2"}, "tag", tag);
                    Log.d(TAG, "Current word bank: " + tag);
                    tagList.add(tag);
                    ArrayList<Word> importedWords = new ArrayList<>();
                    if (bank.moveToFirst()) {
                        do {
                            String word1 = bank.getString(bank.getColumnIndex("word1"));
                            String word2 = bank.getString(bank.getColumnIndex("word2"));
                            Word w = new Word(word1, word2);
                            Log.d(TAG, "Current word: " + w);
                            importedWords.add(w);
                        } while (bank.moveToNext());//Loop through words for each tag
                    }
                    wordBanks.add(new WordBank(this, importedWords, tag));
                    bank.close();
                }
            }while (tags.moveToNext());//Loop through tags
        }

        tags.close();
    }

    public void importFromDB(String tag)
    {
        Log.d(TAG, "Importing from database; looking for " + tag);
        bank.clear();
        allWords.clear();
        done.clear();

        for(WordBank wb : wordBanks) {
            Log.d(TAG, "Found wb " + wb.getTag());
            if (null != wb.getTag() && wb.getTag().equals(tag)) {
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
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals("com.example.charades.UPDATE"))
            {
                Log.d(TAG, "Switching wb to " + dbh.getCurrent());
                Log.d(TAG, "Remaining words: " + allWords.toString());
                Log.d(TAG, "Remaining bank: " + bank.toString());
                importFromDB(dbh.getCurrent());
                tv_word.setText("" + allWords.get(0));
            }
        }
    };

}
