package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.TempRecyclerViewAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment


class TableBookCardFragment : BaseFragment(){

    lateinit var rvTableStatus: RecyclerView

    private var tab: Int? = null

    companion object{
        val ARG_COUNT = "param1"

        fun newInstance(counter: Int?): TableBookCardFragment? {
            val fragment = TableBookCardFragment()
            val args = Bundle()
            args.putInt(ARG_COUNT, counter!!)
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            tab = requireArguments().getInt(ARG_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_table_book_pages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
    }

    private fun initView(view: View) {
        rvTableStatus = view.findViewById(R.id.rvTableStatus)
    }

    private fun setUP(view: View) {
        rvTableStatus.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        rvTableStatus.adapter = TempRecyclerViewAdapter(requireContext(),R.layout.card_booked_table)

    }

    override fun onBackPageCome(){

    }


}