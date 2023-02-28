package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetPeopleListAdapter
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetFireData
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.networking.meetup.Place

class VotedPlaceAlert(var clicked: () -> Unit) {

    val TAG = VotedPlaceAlert::class.simpleName

    var dialog: Dialog? = null
    var logo: ImageView? = null
    var name: TextView? = null
    var address: TextView? = null
    var reasonDesc: TextView? = null
    var rvVotedPerson: RecyclerView? = null

    var meetVoteDocumemt: MeetFireData? = null
    var placeShares: ArrayList<Triple<String?, Int, Double>>? = null
    var participants: List<MeetPerson>? = null
    var places: List<Place>? = null
    var customPlaces: List<AddressModel>? = null


    fun showDialog(activity: Activity, arcIndex: Int, openProfile: (String?) -> Unit): Dialog? {

        meetVoteDocumemt?.let {
            dialog = Dialog(activity, R.style.BottomSheetDialog)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            //...set cancelable false so that it's never get hidden
            dialog?.setCancelable(true)
            //...that's the layout i told you will inflate later
            dialog?.setContentView(R.layout.custom_vote_place_layout)

            dialog?.findViewById<ImageView>(R.id.cancel)?.apply {
                setOnClickListener { hideDialog() }

            }
            logo = dialog?.findViewById(R.id.logo)
            name = dialog?.findViewById<TextView>(R.id.name)?.apply { text = " nhebhjde" }
            address = dialog?.findViewById(R.id.address)
            reasonDesc = dialog?.findViewById(R.id.reason_desc)
            rvVotedPerson = dialog?.findViewById(R.id.rvVotedPerson)
            rvVotedPerson?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)


            var sidList = meetVoteDocumemt?.votes?.values?.filter { it.id == placeShares?.get(arcIndex)?.first }
            var place = places?.firstOrNull { it._id == placeShares?.get(arcIndex)?.first }
            var customPlace = customPlaces?.firstOrNull { it._id == placeShares?.get(arcIndex)?.first }
            place?.let {
                name?.text = it.name.en
                address?.visibility = View.GONE
            }
            customPlace?.let {
                name?.text = it.name
                address?.text = it.address
                address?.visibility = View.VISIBLE
            }
            var list = participants?.filter { participant ->
                sidList?.firstOrNull { it.sid.equals(participant.sid) }
                        ?.let { return@filter true } ?: run { return@filter false }
            }
            if(list == null || list?.isEmpty()) {
                reasonDesc?.text = "No votes yet for ".plus(place?.name?.en ?: "")
                        .plus(customPlace?.name ?: "")
            } else {
                reasonDesc?.text = "These people voted for ".plus(place?.name?.en ?: "")
                        .plus(customPlace?.name ?: "")
            }
            rvVotedPerson?.adapter = MeetPeopleListAdapter(activity, ArrayList(list), {

            }, { sid, isFollow ->

            }, {
                openProfile(it)
            }, {
                hideDialog()
            }).apply { hideFollowunFollw() }

            //rvVotedPerson?.adapter = TempRecyclerViewAdapter(activity,R.layout.card_like)


            dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog?.show()
            return dialog
        }
        return null

    }

    fun setData(meetVoteDocumemt: MeetFireData?, placeShares: ArrayList<Triple<String?, Int, Double>>, participants: List<MeetPerson>?, places: List<Place>?, customPlaces: List<AddressModel>?) {
        this.meetVoteDocumemt = meetVoteDocumemt
        this.placeShares = placeShares
        this.participants = participants
        this.places = places
        this.customPlaces = customPlaces
    }


    fun hideDialog() {
        Log.i(TAG, " hideDialog::  0")
        dialog?.let {
            Log.i(TAG, " hideDialog::  1")
            if(it.isShowing) it.dismiss()
        }
    }

}