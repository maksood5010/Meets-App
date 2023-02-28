package com.meetsportal.meets.overridelayout.mention

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.ArrayMap
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import java.util.regex.Pattern


class SocialMentionAutoComplete @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null,defStyleAttr:Int = 0) :
    AppCompatMultiAutoCompleteTextView(context,attrs) {

        init {
            Log.i("TAG"," defStyleAttr:: $defStyleAttr")
            initializeComponents()
        }

    //MentionAutoCompleteAdapter mentionAutoCompleteAdapter;
    var listener: MentionText? = null
    var temp = ArrayMap<String, MentionPerson>()
    var map = ArrayMap<String, MentionPerson>()
    var formattedOfString = "@%s "

    private fun initializeComponents() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                Log.i("TAG", "onTextChanged: 66 $s")
                if (!s.toString().isEmpty() && start < s.length) {
                    Log.i("TAG", "onTextChanged: start $start")
                    var name = s.toString().substring(0, start + 1)
                    var lastTokenIndex = name.lastIndexOf(" @")
                    val lastIndexOfSpace = name.lastIndexOf(" ")
                    Log.i("TAG", "onTextChanged: 55 $name")
                    val nextIndexOfSpace = name.indexOf(" ", start)
                    Log.i("TAG", " lastTokenIndex::: $lastTokenIndex")
                    Log.i("TAG", " lastIndexOfSpace::: $lastIndexOfSpace")
                    Log.i("TAG", " nextIndexOfSpace::: $nextIndexOfSpace")
                    Log.i("TAG", "onTextChanged: 0")
                    if (lastIndexOfSpace > 0 && lastTokenIndex < lastIndexOfSpace) {
                        val afterString = s.toString().substring(lastIndexOfSpace, s.length)
                        if (listener != null) listener?.searchEnd()
                        if (afterString.startsWith(" ")) return
                    }
                    Log.i("TAG", "onTextChanged: 1")
                    if (lastTokenIndex < 0) {
                        lastTokenIndex =
                            if (!name.isEmpty() && name.length >= 1 && name.startsWith("@")) {
                                1
                            } else {
                                if (listener != null) listener?.searchEnd()
                                return
                            }
                    }
                    Log.i("TAG", "onTextChanged: 2")
                    var tokenEnd = lastIndexOfSpace
                    if (lastIndexOfSpace <= lastTokenIndex) {
                        tokenEnd = name.length
                        if (nextIndexOfSpace != -1 && nextIndexOfSpace < tokenEnd) {
                            tokenEnd = nextIndexOfSpace
                        }
                    }
                    Log.i("TAG", "onTextChanged: 3")
                    if (lastTokenIndex >= 0) {
                        name = s.toString().substring(lastTokenIndex, tokenEnd).trim { it <= ' ' }
                        Log.i("TAG", "onTextChanged: 44 $name")
                        val pattern = Pattern.compile("^(.+)\\s.+")
                        val matcher = pattern.matcher(name)
                        if (!matcher.find()) {
                            Log.i("TAG", "onTextChanged: 4 $name")
                            name = name.replace("@", "").trim { it <= ' ' }
                            Log.i("TAG", "onTextChanged: 9 $name")
                            if (!name.isEmpty()) {
                                Log.i("TAG", "onTextChanged: 5")
                                if (listener != null) {
                                    Log.i("TAG", "onTextChanged: 6")
                                    listener?.getMentionedSubString(name)
                                }
                                //getUsers(name);
                            } else {
                                if (listener != null) listener?.searchStart()
                            }
                        }
                    }
                    Log.i("TAG", "onTextChanged: 7")
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

    }

    /*AdapterView.OnItemClickListener onItemSelectedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            MentionPerson mentionPerson = (MentionPerson) adapterView.getItemAtPosition(i);
            map.put("@" + mentionPerson.getAlt_id(), mentionPerson);
        }
    };*/
    /***
     * This function returns the contents of the AppCompatMultiAutoCompleteTextView into my desired Format
     * You can write your own function according to your needs
     */
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
    fun setMentioningText(peron: SearchPeopleResponseItem) {
        //map.clear()
        try{
            map.put("@${peron.username}", peron.toMentionPeople())


            var username: String = peron.username ?: ""
            //Pattern p = Pattern.compile("\\[([^]]+)]\\(([^ )]+)\\)");
            val p = Pattern.compile("[@][a-zA-Z0-9-.]+")

            var finalDesc = this.getText().toString()
            Log.i("TAG", " checkincdscsg::: 0 $finalDesc")
            val cusrsorPosition = selectionStart
            Log.i("TAG", " checkincdscsg::: 1 $cusrsorPosition --- ${finalDesc}")
            val lastindescOfAt = finalDesc.lastIndexOf("@", cusrsorPosition)
            Log.i("TAG", " checkincdscsg::: 2 $lastindescOfAt temp-- ${finalDesc.indexOf("@")}")
            val sbd = finalDesc.substring(lastindescOfAt, cusrsorPosition)
            Log.i("TAG", " checkincdscsg::: 3 $sbd")
            finalDesc = finalDesc.substring(0, lastindescOfAt).plus("@$username ")
                .plus(finalDesc.substring(lastindescOfAt.plus(sbd.length)))
//        finalDesc = finalDesc.replace(sbd, " @$username ")
            Log.i("TAG", " checkincdscsg::: 4 $finalDesc")
            setText(finalDesc)
            Log.i(
                "TAG",
                " checkincdscsg::: 5 " + finalDesc.indexOf(
                    username,
                    lastindescOfAt
                ) + username.length
            )
            val selection =
                finalDesc.indexOf(username, lastindescOfAt).plus(username.length).plus(1)
            Log.i("TAG", " checkincdscsg::: 4 $selection")

            setSelection(selection)
            //finalDesc = finalDesc.substring(0,finalDesc.lastIndexOf("@"));

            val m = p.matcher(this.getText().toString())


            temp.clear()
            while (m.find()) {
                //MentionPerson mentionPerson = MentionPerson();
                Log.i("TAG", " nameAndId:::  0   ${m.groupCount()} ")
                var name = m.group(0);
                Log.i("TAG", " nameAndId:::  1   $name ")

                temp.put(name, map.getOrDefault(name, MentionPerson()))

            }

            map.clear()
            map.putAll(temp.filter {
                it.value.alt_id?.isNotEmpty() == true
            })

            val textColor = ResourcesCompat.getColor(
                resources, R.color.primaryDark, null
            )


            val spannable: Spannable = SpannableString(text.toString())
            /*for ((key) in map.entries) {
                val startIndex = finalDesc.indexOf(key)
                val endIndex = startIndex + key.length
                spannable.setSpan(
                    ForegroundColorSpan(textColor),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }*/
            for ((key) in map.entries) {
                var startIndex: Int? = finalDesc.indexOf(key.plus(" "))
                while (startIndex != null && startIndex != -1 && startIndex >= 0) {
                    val endIndex = startIndex.plus(key.length)
                    spannable.setSpan(
                        ForegroundColorSpan(textColor),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    Log.i("TAG", " checkingStartIndesFirst:: $startIndex")
                    startIndex = finalDesc.indexOf(key.plus(" "), startIndex + 1)
                    Log.i("TAG", " checkingStartIndesLast:: $startIndex")
                }
            }
            setText(spannable)
            setSelection(selection)
        }catch (e:Exception){
            Log.e("TAG", "setMentioningText: ${e.message}")
        }
    }


    public fun getAllMentionPerson():ArrayList<MentionPerson>{
        return ArrayList(map.values)
    }

    fun setAllMentioned(mentions: List<MentionPerson>?) {
        map.clear()
        mentions?.forEach {
           // map.put("@${peron.username}",peron.toMentionPeople())
            map.put("@${it.alt_id}",it)
        }
        structuredText()
    }

    fun structuredText() {
        val p = Pattern.compile("[@][a-zA-Z0-9-.]+")
        val m = p.matcher(this.getText().toString())
        temp.clear()
        while (m.find()) {
            //MentionPerson mentionPerson = MentionPerson();
            Log.i("TAG"," nameAndId:::  0   ${m.groupCount()} ")
            var name = m.group(0);
            Log.i("TAG"," nameAndId:::  1   $name ")

            temp.put(name,map.getOrDefault(name,MentionPerson()))
        }
        map.clear()
        map.putAll(temp.filter {
            it.value.alt_id?.isNotEmpty() == true
        })
        val textColor = ResourcesCompat.getColor(
            resources, R.color.primaryDark, null
        )
        var finalDesc = this.getText().toString()
        Log.i("TAG"," OurText:: $finalDesc")
        val spannable: Spannable = SpannableString(text.toString())
        for ((key) in map.entries) {
            var startIndex :Int?= finalDesc.indexOf(key)
            while (startIndex != null && startIndex != -1 && startIndex >= 0  ){
                val endIndex = startIndex.plus(key.length)
                spannable.setSpan(
                    ForegroundColorSpan(textColor),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Log.i("TAG"," checkingStartIndesFirst:: $startIndex")
                startIndex = finalDesc.indexOf(key, startIndex + 1)
                Log.i("TAG"," checkingStartIndesLast:: $startIndex")
            }
        }
        setText(spannable)
    }

    interface MentionText {
        fun getMentionedSubString(subString: String?)
        fun searchStart()
        fun searchEnd()
    }

    fun addMentionListener(listrner: MentionText) {
        if (listener == null) listener = listrner
    }

}
