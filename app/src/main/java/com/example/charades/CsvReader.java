package com.example.charades;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class CsvReader {

    private Context context;
    private String path;
    private Uri uri;
    private ArrayList<String[]> rows = new ArrayList<>();

    private final static String TAG = "CsvReader";

    public CsvReader(Context ctx, String p)
    {
        this.context = ctx;
        path = "csv/" + p;
    }

    public CsvReader(Context ctx, Uri u)
    {
        this.context = ctx;
        uri = u;
    }

    public void readFromAsset() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
        String line;
        String separator = ",";

        while ((line = br.readLine()) != null) {
            String[] row = line.split(separator);
            rows.add(row);
            Log.d(TAG, "New row: " + Arrays.toString(row));
        }
    }

    public void readFromFile() throws IOException
    {
        ContentResolver cr = context.getContentResolver();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(cr.openInputStream(uri)));
            String line;
            String separator = ",";

            while ((line = br.readLine()) != null) {
                String[] row = line.split(separator);
                rows.add(row);
                Log.d(TAG, "New row: " + Arrays.toString(row));
            }
        } catch(FileNotFoundException e){e.printStackTrace();}

    }

    public ArrayList<String[]> getRows()
    {
        return rows;
    }

    public void clear()
    {
        rows.clear();
    }
}
