package com.meetsportal.meets.networking.post

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.overridelayout.mention.MentionPerson

data class PostRequest(
    @SerializedName("body")
    val body: String?,
    @SerializedName("media")
    val media: List<String>?,
    @SerializedName("type")
    val type : String? = "default",
    @SerializedName("body_obj")
    val body_obj : BodyObj? = null,
    @SerializedName("bodmentionsy")
    val mentions : List<MentionPerson>? = null
){

}

data class BodyObj(
     val gradient_type : String? =  null
){
    companion object{

        /*enum class GradLable(val label : String,val gradient : Int){
            GRAD1("grad1",R.drawable.grad1),
            GRAD2("grad2",R.drawable.grad2),
            GRAD3("grad3",R.drawable.grad3),
            GRAD4("grad4",R.drawable.grad4),
            GRAD5("grad5",R.drawable.grad5),
            GRAD6("grad6",R.drawable.grad6),
            GRAD7("grad7",R.drawable.grad7),
            GRAD8("grad8",R.drawable.grad8),
            GRAD9("grad9",R.drawable.grad9),
            GRAD10("grad10",R.drawable.grad10),
            GRAD11("grad11",R.drawable.grad11),
        }*/


       /* val gradientTypeArray = arrayOf(GradLable.GRAD1,
            GradLable.GRAD2, GradLable.GRAD3, GradLable.GRAD4,
            GradLable.GRAD5, GradLable.GRAD6, GradLable.GRAD7,
            GradLable.GRAD8, GradLable.GRAD9,GradLable.GRAD10,
            GradLable.GRAD11
        )*/


        /*enum class GradLable(val label : String,val gradient : Int){
            GRAD1("grad1",R.drawable.grad1),
            GRAD2("grad2",R.drawable.grad2),
            GRAD3("grad3",R.drawable.grad3),
            GRAD4("grad4",R.drawable.grad4),
            GRAD5("grad5",R.drawable.grad5),
            GRAD6("grad6",R.drawable.grad6),
            GRAD7("grad7",R.drawable.grad7),
            GRAD8("grad8",R.drawable.grad8),
            GRAD9("grad9",R.drawable.grad9),
            GRAD10("grad10",R.drawable.grad10),
            GRAD11("grad11",R.drawable.grad11),
        }*/

    }


}


