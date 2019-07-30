package com.example.charades;

public class Word {

    private String Chinese;
    private String English;

    public Word(String ch, String eng){
        Chinese = ch;
        English = eng;
    }

    public String getChinese(){return Chinese;}

    public String getEnglish(){return English;}

    public String setChinese(String in){Chinese = in; return Chinese;}

    public String setEnglish(String in){English = in; return Chinese;}

    @Override
    public String toString(){return Chinese + ", " + English;}
}
