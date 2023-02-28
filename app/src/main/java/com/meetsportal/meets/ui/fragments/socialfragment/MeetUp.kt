package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.*

class MeetUp : BaseFragment(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private val TAG = MeetUp::class.java.simpleName

    lateinit var back: ImageView
    lateinit var chooseRestaurant: CardView
    lateinit var datePick: LinearLayout
    lateinit var timePick: LinearLayout
    lateinit var pickedDate: TextView
    lateinit var pickedTime: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_meet_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
    }

    private fun initView(view: View) {
        back = view.findViewById(R.id.iv_back)
        chooseRestaurant = view.findViewById(R.id.cv_rest)
        datePick = view.findViewById(R.id.ll_date_picker)
        timePick = view.findViewById(R.id.ll_time_picker)
        pickedDate = view.findViewById(R.id.tv_date_pick_text)
        pickedTime = view.findViewById(R.id.tv_time_pick_text)


    }

    private fun setUp() {
        back.setOnClickListener { activity?.onBackPressed() }
        datePick.setOnClickListener { pickDate() }
        pickedTime.setOnClickListener { pickTime() }


    }


    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val time = "" + hourOfDay.toString() + "h" + minute.toString() + "m" + second
        pickedTime.text = time
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = "" + dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year
        pickedDate.text = date
    }


    private fun pickTime() {

        val now: Calendar = Calendar.getInstance()
        val tpd = TimePickerDialog.newInstance(this, now.get(Calendar.HOUR), now.get(Calendar.MINUTE), false)
        tpd.title = "Choose Booking Time"
        tpd.accentColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
        tpd.setOkColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
        tpd.setCancelColor(ContextCompat.getColor(requireContext(), R.color.gray))
        tpd.show(parentFragmentManager, "Datepickerdialog");
    }

    private fun pickDate() {
        val now: Calendar = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(this, now.get(Calendar.YEAR),  // Initial year selection
                now.get(Calendar.MONTH),  // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                                              )
        dpd.setTitle("Choose MeetUp Date")
        dpd.accentColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
        dpd.setOkColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
        dpd.setCancelColor(ContextCompat.getColor(requireContext(), R.color.gray))

        dpd.show(parentFragmentManager, "Datepickerdialog");
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}