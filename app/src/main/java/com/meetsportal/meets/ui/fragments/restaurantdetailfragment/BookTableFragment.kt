package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.TimeAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class BookTableFragment : BaseFragment(),DatePickerDialog.OnDateSetListener {

    private val TAG = BookTableFragment::class.java.simpleName

    lateinit var rvTime : RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    lateinit var date : EditText
    private lateinit var spOccasion: Spinner
    lateinit var next :Button


    val sdf = SimpleDateFormat("YYYY-MM-dd", Locale.US)


    var timesList = arrayOf(
        "17:00",
        "17:30",
        "18:00",
        "18:30",
        "19:00",
        "19:30",
        "20:00",
        "20:30",
        "21:00",
        "21:30",
        "22:00",
        "22:30",
        "23:00",
        "23:30"
    )

    var occasions = arrayOf(
        "Anniversary",
        "Birthday",
        "First Time",
        "Regular"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_table,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP()
    }

    private fun initView(view: View) {
        rvTime = view.findViewById(R.id.rv_time)
        date = view.findViewById(R.id.et_date)
        spOccasion = view.findViewById(R.id.sp_occasion)
        next = view.findViewById(R.id.btn_next_table_information)
    }

    private fun setUP() {

        gridLayoutManager = GridLayoutManager(requireContext(), 6)
        rvTime.layoutManager = gridLayoutManager
        rvTime.adapter = TimeAdapter(requireContext(),timesList){_,_ ->  }
        date.setOnClickListener{
            pickDate()
        }

        //val adapter = ArrayAdapter(this, R.layout.item_spinner, occasions)
        val adapter = ArrayAdapter(requireContext(),R.layout.item_spinner,occasions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spOccasion.setAdapter(adapter)
        spOccasion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        next.setOnClickListener {
            Navigation.addFragment(requireActivity(),TableInformationFragment(),"tableInformation",R.id.restrauntFrame,true,false)
        }

    }

    private fun pickDate() {
        val now: Calendar = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),  // Initial year selection
            now.get(Calendar.MONTH),  // Initial month selection
            now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        )
        dpd.setTitle("Choose MeetUp Date")
        dpd.accentColor = ContextCompat.getColor(requireContext(),R.color.primaryDark)
        dpd.setOkColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
        dpd.setCancelColor(ContextCompat.getColor(requireContext(),R.color.gray))

        dpd.show(parentFragmentManager, "Datepickerdialog");
    }

    fun updateDate(calendar: Calendar) {
        Log.i(TAG,sdf.format(calendar.time))
        var sdfShow = SimpleDateFormat("E, dd MMM yyyy", Locale.US)
        date.setText(sdfShow.format(calendar.time))
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        var dateCalendar = Calendar.getInstance()
        dateCalendar.set(Calendar.YEAR, year)
        dateCalendar.set(Calendar.MONTH, monthOfYear)
        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        var sdfShow = SimpleDateFormat("E, dd MMM yyyy", Locale.US)
        date.setText(sdfShow.format(dateCalendar.getTime()))

    }

    override fun onBackPageCome(){

    }
}