package com.meetsportal.meets.utils

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class PlaceApi(val myContext: Context) {
    private val apiKey: String = myContext.getString(R.string.GOOGLE_API_KEY)

    fun autoComplete(input: String): ArrayList<AddressModel?> {
        val arrayList: ArrayList<AddressModel?> = ArrayList()
        var connection: HttpURLConnection? = null
        val jsonResult = StringBuilder()

        val tm = myContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso
        try {
            val sb = StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
            sb.append("input=$input")
            sb.append("&key=$apiKey")
            sb.append("&components=country:$countryCodeValue")
            val url = URL(sb.toString())
            connection = url.openConnection() as HttpURLConnection
            val inputStreamReader = InputStreamReader(connection.inputStream)
            var read: Int
            val buff = CharArray(1024)
            while (inputStreamReader.read(buff).also { read = it } != -1) {
                jsonResult.append(buff, 0, read)
            }
            Log.d("JSon", jsonResult.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        try {
            val jsonObject = JSONObject(jsonResult.toString())
            val prediction = jsonObject.getJSONArray("predictions")
            for (i in 0 until prediction.length()) {
                val addressModel = AddressModel()
                addressModel._id = prediction.getJSONObject(i).getString("description")
                addressModel.name =
                    prediction.getJSONObject(i).getJSONObject("structured_formatting")
                        .getString("main_text")
                addressModel.address =
                    prediction.getJSONObject(i).getJSONObject("structured_formatting")
                        .getString("secondary_text")
                arrayList.add(addressModel)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return arrayList
    }

    fun getGeoLocation(lat: Double, lon: Double): AddressModel? {
        val model: AddressModel = AddressModel()
        var connection: HttpURLConnection? = null
        val jsonResult = StringBuilder()

        try {
            val sb =
                StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lon&sensor=false&language=en")
            sb.append("&key=$apiKey")
            val url = URL(sb.toString())
            Log.d("TAG", "getGeoLocation: sb.toString() $sb")
            connection = url.openConnection() as HttpURLConnection
            val inputStreamReader = InputStreamReader(connection.inputStream)
            var read: Int
            val buff = CharArray(1024)
            while (inputStreamReader.read(buff).also { read = it } != -1) {
                jsonResult.append(buff, 0, read)
            }
            Log.d("JSon", jsonResult.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        try {
            val jsonObject = JSONObject(jsonResult.toString())
            Log.d("TAG", "getGeoLocation:jsonObject $jsonObject ")
            val jsonArray = jsonObject.getJSONArray("results");
            if (!jsonArray.isNull(0)) {
                val firstItem = jsonArray.getJSONObject(0)
                if (firstItem.has("plus_code") && firstItem.getJSONObject("plus_code")!=null){
                    model.name = firstItem.getJSONObject("plus_code").getString("compound_code")
                }else{
                    model.name = firstItem.getString("formatted_address")
                }


                model._id = firstItem.getString("place_id")
                model.address = firstItem.getString("formatted_address")
                model.setLocation(firstItem.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        firstItem.getJSONObject("geometry").getJSONObject("location").getDouble("lng"))
//                model.lat = firstItem.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
//                model.lon = firstItem.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                return model
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }


}