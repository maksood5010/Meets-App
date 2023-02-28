package com.meetsportal.meets.overridelayout.mention;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.MultiAutoCompleteTextView;

import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.core.content.res.ResourcesCompat;

import com.meetsportal.meets.R;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialMentionAutoComplete2 extends AppCompatMultiAutoCompleteTextView {

    //MentionAutoCompleteAdapter mentionAutoCompleteAdapter;

    public MentionText listener;
    ArrayMap<String, MentionPerson> map = new ArrayMap<>();
    String formattedOfString = "@%s ";

    public SocialMentionAutoComplete2(Context context) {
        super(context);
        initializeComponents();
    }

    public SocialMentionAutoComplete2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeComponents();
    }

    public SocialMentionAutoComplete2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeComponents();
    }

    private void initializeComponents() {
        addTextChangedListener(textWatcher);
        //setOnItemClickListener(onItemSelectedListener);
        //setTokenizer(new SpaceTokenizer());
    }

    /*AdapterView.OnItemClickListener onItemSelectedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            MentionPerson mentionPerson = (MentionPerson) adapterView.getItemAtPosition(i);
            map.put("@" + mentionPerson.getAlt_id(), mentionPerson);
        }
    };*/


    /***
     *This function returns the contents of the AppCompatMultiAutoCompleteTextView into my desired Format
     *You can write your own function according to your needs
     **/

    /*public String getProcessedString() {

        String s = getText().toString();

        for (Map.Entry<String, MentionPerson> stringMentionPersonEntry : map.entrySet()) {
            s = s.replace(stringMentionPersonEntry.getKey(), stringMentionPersonEntry.getValue().getFormattedValue());
        }
        return s;
    }*/

    /**
     * This function will process the incoming text into mention format
     * You have to implement the processing logic
     */
    public void setMentioningText(String text) {

        map.clear();

        //Pattern p = Pattern.compile("\\[([^]]+)]\\(([^ )]+)\\)");
        Pattern p = Pattern.compile("[@][a-zA-Z0-9-.]+");
        Matcher m = p.matcher(text);

        String finalDesc = this.getText().toString();
        Log.i("TAG"," checkincdscsg::: 0 "+finalDesc);
        int cusrsorPosition = getSelectionStart();
        Log.i("TAG"," checkincdscsg::: 1 "+cusrsorPosition);
        int lastindescOfAt = finalDesc.lastIndexOf("@",cusrsorPosition);
        Log.i("TAG"," checkincdscsg::: 2 "+lastindescOfAt);
        String sbd = finalDesc.substring(lastindescOfAt,cusrsorPosition);
        Log.i("TAG"," checkincdscsg::: 3 "+sbd);
        finalDesc = finalDesc.replace(sbd," @"+text+" ");
        Log.i("TAG"," checkincdscsg::: 4 "+finalDesc);


        setText(finalDesc);
        Log.i("TAG"," checkincdscsg::: 5 "+finalDesc.indexOf(text,lastindescOfAt)+text.length());
        int selection = finalDesc.indexOf(text,lastindescOfAt) + text.length();
        Log.i("TAG"," checkincdscsg::: 4 "+selection);
        setSelection(selection);
        //finalDesc = finalDesc.substring(0,finalDesc.lastIndexOf("@"));







        /*while (m.find()) {
            MentionPerson mentionPerson = new MentionPerson();
            String name = m.group(1);
            String id = m.group(2);
            //Processing Logic
            finalDesc = finalDesc.replace("@[" + name + "](" + id + ")", "@" + name);

            //mentionPerson.setAlt_id(name);
            //mentionPerson.setId(id);
            //map.put("@" + name, mentionPerson);
        }*/



        int textColor = ResourcesCompat.getColor(getResources(), R.color.primaryDark, null);
        Spannable spannable = new SpannableString(finalDesc);
        for (Map.Entry<String, MentionPerson> stringMentionPersonEntry : map.entrySet()) {
            int startIndex = finalDesc.indexOf(stringMentionPersonEntry.getKey());
            int endIndex = startIndex + stringMentionPersonEntry.getKey().length();
            spannable.setSpan(new ForegroundColorSpan(textColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(spannable);
    }

   /* public void processText(){

        setMentioningText(this.getText().toString())

    }*/


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int lengthBefore, int lengthAfter) {

            if (!s.toString().isEmpty() && start < s.length()) {

                String name = s.toString().substring(0, start + 1);

                int lastTokenIndex = name.lastIndexOf(" @");
                int lastIndexOfSpace = name.lastIndexOf(" ");
                int nextIndexOfSpace = name.indexOf(" ", start);


                Log.i("TAG"," lastTokenIndex::: "+lastTokenIndex);
                Log.i("TAG"," lastIndexOfSpace::: "+lastIndexOfSpace);
                Log.i("TAG"," nextIndexOfSpace::: "+nextIndexOfSpace);


                Log.i("TAG", "onTextChanged: 0");
                if (lastIndexOfSpace > 0 && lastTokenIndex < lastIndexOfSpace) {
                    String afterString = s.toString().substring(lastIndexOfSpace, s.length());
                    if(listener != null) listener.searchEnd();
                    if (afterString.startsWith(" ")) return;
                }

                Log.i("TAG", "onTextChanged: 1");

                if (lastTokenIndex < 0) {
                    if (!name.isEmpty() && name.length() >= 1 && name.startsWith("@")) {
                        lastTokenIndex = 1;
                    } else{
                        if(listener != null) listener.searchEnd();
                        return;
                    }

                }
                Log.i("TAG", "onTextChanged: 2");

                int tokenEnd = lastIndexOfSpace;

                if (lastIndexOfSpace <= lastTokenIndex) {
                    tokenEnd = name.length();
                    if (nextIndexOfSpace != -1 && nextIndexOfSpace < tokenEnd) {
                        tokenEnd = nextIndexOfSpace;
                    }
                }
                Log.i("TAG", "onTextChanged: 3");

                if (lastTokenIndex >= 0) {
                    name = s.toString().substring(lastTokenIndex, tokenEnd).trim();
                    Pattern pattern = Pattern.compile("^(.+)\\s.+");
                    Matcher matcher = pattern.matcher(name);
                    if (!matcher.find()) {
                        Log.i("TAG", "onTextChanged: 4");
                        name = name.replace("@", "").trim();
                        if (!name.isEmpty()) {
                            Log.i("TAG", "onTextChanged: 5");
                            if(listener != null){
                                Log.i("TAG", "onTextChanged: 6");
                                listener.getMentionedSubString(name);
                            }
                            //getUsers(name);
                        }else{
                            if(listener != null) listener.searchStart();
                        }
                    }
                }
                Log.i("TAG", "onTextChanged: 7");

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    /*
     *This function returns results from the web server according to the user name
     * I have used Retrofit for Api Communications
     * */
    public void getUsers(String name) {

       // Log.i("TAG", " checingName:: " + name + "");

            /*Call<UsersResponse> call = Api.getInstance().getApiMethods().getUsers("79146d29997abef00f8daa4414997af9", name);
            call.enqueue(new Callback<UsersResponse>() {
                @Override
                public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                    if (response.isSuccessful()) {
                        UsersResponse usersResponse = response.body();
                        if (usersResponse != null) {
                            List<MentionPerson> mentionPeople = usersResponse.getData();
                            if (mentionPeople != null && !mentionPeople.isEmpty()) {
                                mentionAutoCompleteAdapter =
                                        new MentionAutoCompleteAdapter(getContext(), mentionPeople);
                                SocialMentionAutoComplete2.this.setAdapter(mentionAutoCompleteAdapter);
                                SocialMentionAutoComplete2.this.showDropDown();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<UsersResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });*/
    }

    public void addMentionListener(MentionText listrner) {
        if (listener == null)
            listener = listrner;
    }


    public interface MentionText {
        void getMentionedSubString(String subString);
        void searchStart();
        void searchEnd();
    }

    public void setMention(){

    }


    public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {

            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {

            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {

            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                // Returns colored text for selected token
                SpannableString sp = new SpannableString(String.format(formattedOfString, text));
                int textColor = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
                sp.setSpan(new ForegroundColorSpan(textColor), 0, text.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return sp;
            }
        }
    }

}