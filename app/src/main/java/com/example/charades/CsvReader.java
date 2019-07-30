package com.example.charades;

import android.content.Context;
import android.renderscript.ScriptGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CsvReader {

    private Context context;
    private String path;
    private ArrayList<String[]> rows = new ArrayList<>();
    public CsvReader(Context ctx, String p)
    {
        this.context = ctx;
        path = p;
    }

    public void read() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
        String line;
        String separator = ",";

        while ((line = br.readLine()) != null) {
            String[] row = line.split(separator);
            rows.add(row);
        }
    }

    public ArrayList<String[]> getRows()
    {
        return rows;
    }
}
