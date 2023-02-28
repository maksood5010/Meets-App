package com.meetsportal.meets.ui.fragments.socialfragment

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.ui.dialog.AddNameAlert
import com.meetsportal.meets.ui.dialog.IAmHereAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel

open abstract class BaseMeetFragment : BaseFragment() {

    open val TAG = "BaseMeetFragment"
    abstract val meetUpViewModel: MeetUpViewModel

    lateinit var addNameAlert: AddNameAlert

    fun changeName(meetUpdata: GetMeetUpResponseItem?) {
        Log.i(TAG, " changename:: ${meetUpdata}")
        addNameAlert = AddNameAlert(requireActivity()) {
            if(meetUpdata?.name?.equals(it.trim()) == false && it.trim()
                        .isNotEmpty()) meetUpViewModel.changeMeetName(it, meetUpdata._id)
        }
        Log.i(TAG, " checkingMeetname:: ${meetUpdata?.name}")
        addNameAlert.showDialog(meetUpdata?.name)
    }

    var iAmHereAlert: IAmHereAlert? = null
    var locationFinder: LocationFinder? = null
    fun iamHere(_id: String?) {
        Log.i("TAG", " I am Here:: ")
        iAmHereAlert = IAmHereAlert(requireActivity())
        iAmHereAlert?.showDialog()
        locationFinder = LocationFinder(requireActivity(), object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    it.locations.map {
                        it?.let {
                            Log.i("TAG", " Lat:: ${it.latitude} -- ${it.longitude}---- $_id")
                            meetUpViewModel.markAttendance(_id, it)
                            iAmHereAlert?.changeStatus(0)
                            locationFinder?.mFusedLocationClient?.removeLocationUpdates(this)
                        }
                    }
                }
            }
        })
        meetUpViewModel.observeMarkAttandance().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.d(TAG, "iamHere: success ${it.value}")
                    if(it.value?.get("marked")?.asBoolean == true) {
                        iAmHereAlert?.changeStatus(1)
                        Log.d(TAG, "iamHere: geting hghgj")
                        meetUpViewModel.getMeetUpDetail(_id)
                    } else if(it.value?.get("marked")?.asBoolean == false) {
                        if(it.value.get("distance_to_place") != null) {
                            iAmHereAlert?.changeStatus(2, it.value.get("distance_to_place").asInt)
                        } else {
                            iAmHereAlert?.changeStatus(2)
                        }
                    } else {
                        iAmHereAlert?.changeStatus(3)

                    }
                }

                is ResultHandler.Failure -> {
                    iAmHereAlert?.hideDialog()
                    showToast(it.message ?: "Something Went Wrong")
                }
            }
        })
    }


    fun addToCalendar(meetUpdata: GetMeetUpResponseItem?) {
        if(Utils.checkPermission(activity, android.Manifest.permission.WRITE_CALENDAR)) {
            try {
                pushAppointmentsToCalender(meetUpdata)?.let {
                    MyApplication.showToast(requireActivity(), " Added to Calendar...")
                }
            } catch(e: Exception) {
                FirebaseCrashlytics.getInstance().log("exception while adding to calendar ")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(e))
                MyApplication.showToast(requireActivity(), " Something went Wrong...")
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_CALENDAR), 1255)
        }
    }

    fun pushAppointmentsToCalender(

        meetUpdata: GetMeetUpResponseItem?

                                  ): Long? {
        /***************** Event: note(without alert)  */
        Log.i(TAG, " checking:::  0 ")
        val eventUriString = "content://com.android.calendar/events"
        val eventValues = ContentValues()
        Log.i(TAG, " checking:::  1 ")
        eventValues.put("calendar_id", 1) // id, We need to choose from
        // our mobile for primary
        // its 1
        eventValues.put("title", meetUpdata?.name)
        //eventValues.put("description", addInfo)
        //eventValues.put("eventLocation", place)
        //val endDate = startDate + 1000 * 60 * 60 // For next 1hr
        eventValues.put("dtstart", meetUpdata?.date?.toDate()?.toCalendar()?.getTimeInMillis())
        eventValues.put("dtend", meetUpdata?.date?.toDate()?.toCalendar()?.getTimeInMillis()
                ?.plus(60 * 60 * 5000))

        Log.i(TAG, " checking:::  2 ")
        eventValues.put("allDay", 1); //If it is bithday alarm or such
        // kind (which should remind me for whole day) 0 for false, 1
        // for true
        eventValues.put("eventStatus", 1) // This information is
        // sufficient for most
        // entries tentative (0),
        // confirmed (1) or canceled
        // (2):
        eventValues.put("eventTimezone", "UTC/GMT +2:00")
        /*Comment below visibility and transparency  column to avoid java.lang.IllegalArgumentException column visibility is invalid error */

        //eventValues.put("visibility", 3); // visibility to default (0),
        // confidential (1), private
        // (2), or public (3):
        //eventValues.put("transparency", 0); // You can control whether
        // an event consumes time
        // opaque (0) or transparent
        // (1).
        eventValues.put("hasAlarm", 1) // 0 for fa
        Log.i(TAG, " checking:::  4 ")// lse, 1 for true
        val eventUri: Uri? = requireActivity().applicationContext.contentResolver.insert(Uri.parse(eventUriString), eventValues)
        Log.i(TAG, " checking:::  5 ")
        val eventID: Long? = eventUri?.getLastPathSegment()?.toLong()
        //if (needReminder) {
        Log.i(TAG, " checking:::  6 ")
        if(true) {
            /***************** Event: Reminder(with alert) Adding reminder to event  */
            val reminderUriString = "content://com.android.calendar/reminders"
            val reminderValues = ContentValues()
            reminderValues.put("event_id", eventID)
            reminderValues.put("minutes", 5)
            Log.i(TAG, " checking:::  7 ")// Default value of the
            // system. Minutes is a
            // integer
            reminderValues.put("method", 1) // Alert Methods: Default(0),
            // Alert(1), Email(2),
            // SMS(3)
            Log.i(TAG, " checking:::  8 ")
            val reminderUri: Uri? = requireActivity().applicationContext.contentResolver.insert(Uri.parse(reminderUriString), reminderValues)

            Log.i(TAG, " checking:::  9 ")
        }
        /***************** Event: Meeting(without alert) Adding Attendies to the meeting  */
        /*if (needMailService) {
            val attendeuesesUriString = "content://com.android.calendar/attendees"

            */
        /********
         * To add multiple attendees need to insert ContentValues multiple
         * times
         *//*
            val attendeesValues = ContentValues()
            attendeesValues.put("event_id", eventID)
            attendeesValues.put("attendeeName", "xxxxx") // Attendees name
            attendeesValues.put("attendeeEmail", "yyyy@gmail.com") // Attendee
            // E
            // mail
            // id
            attendeesValues.put("attendeeRelationship", 0) // Relationship_Attendee(1),
            // Relationship_None(0),
            // Organizer(2),
            // Performer(3),
            // Speaker(4)
            attendeesValues.put("attendeeType", 0) // None(0), Optional(1),
            // Required(2), Resource(3)
            attendeesValues.put("attendeeStatus", 0) // NOne(0), Accepted(1),
            // Decline(2),
            // Invited(3),
            // Tentative(4)
            val attendeuesesUri: Uri? = curActivity.applicationContext.contentResolver.insert(
                Uri.parse(attendeuesesUriString), attendeesValues
            )
        }*/
        return eventID
    }

    fun addfriend(meetUpdata: GetMeetUpResponseItem?, TAG: String) {
        var fragment = AddFriendToMeetUp.getInstance(meetUpdata, TAG)
        Navigation.addFragment(requireActivity(), fragment, AddFriendToMeetUp::class.simpleName!!, R.id.homeFram, true, true)
    }

    fun optOut(meetUpdata: GetMeetUpResponseItem?) {
        meetUpViewModel.optOut(meetUpdata?._id)
    }

    fun closeMeetUp(meetId: String?) {
        meetUpViewModel.closeOpenMeet(meetId)
    }

    fun cancleMeetUp(meetUpdata: GetMeetUpResponseItem?) {
        Log.i(TAG, " MeetCancelled ")
        meetUpViewModel.cancleMeetUp(meetUpdata?._id)
    }

    fun deleteMeetUp(meetId: String?, scope: String) {
        meetUpViewModel.deleteMeetUp(meetId, scope)
    }

    open fun setIamHere() {

    }

    open fun addObserver() {
        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.d(TAG, "populateView: GetMeetUpResponseItem 1")
                    populateView(it.value)
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, "  error Comes in fetching Meet detail")
                }
            }
        })
    }


    abstract fun populateView(value: GetMeetUpResponseItem?)


}