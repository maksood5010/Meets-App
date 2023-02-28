package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class ReportOtherFragment(): BaseFragment() {

     val TAG = ReportOtherFragment::class.simpleName

    val postViewModel : PostViewModel by viewModels()

    lateinit var reason : EditText
    lateinit var reasonCounter: TextView
    lateinit var report: TextView
    var compositeDisposable = CompositeDisposable()

    var body = JsonObject()



    companion object{
        fun getInstance(content_type:String?, id:String?):ReportOtherFragment{
            return ReportOtherFragment().apply {
                arguments= Bundle().apply {
                    putString("content_type",content_type)
                    putString("id",id)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_other, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView(view)
        setUp()
        setObserver()
    }

    private fun initView(view: View) {

        body.addProperty("content_id",arguments?.getString("id"))
        body.addProperty("content_type",arguments?.getString("content_type"))

        Log.d(TAG, "initView: ")
        reason = view.findViewById(R.id.et_reason)
        if(arguments?.getString("content_type").equals("user")){
            view.findViewById<TextView>(R.id.tv_heading).text = "Report user"
            reason.hint = "why you are reporting this user"
        }

        reasonCounter = view.findViewById(R.id.tv_counter)
        report = view.findViewById(R.id.report)
        view.findViewById<ImageView>(R.id.cancel_button).apply {
            setOnClickListener {
                activity?.onBackPressed()
            }
        }
    }

    private fun setUp() {

        reason.count {
            reasonCounter.text = "$it/1000"
        }
        compositeDisposable.add(
            Utils.onClick(report,500){
                Utils.hideSoftKeyBoard(requireContext(),report)
                if(reason.text.toString().trim().isNullOrBlank()){
                   showToast("Please write something!!!")
                }else{
                    body.addProperty("reason",reason.text.toString().trim())
                    postViewModel.reportContent(body)
                }

            }
        )
    }

    private fun setObserver() {
        postViewModel.observerReportContent().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success ->{
                    Toast.makeText(requireContext(),"Thank you for reporting, ${it.value?.get("content_type")?.asString} is under review",
                        Toast.LENGTH_SHORT).show()
                }
                is ResultHandler.Failure ->{
                    Toast.makeText(requireContext(),"${it.message}",Toast.LENGTH_SHORT).show()
                }
            }
            activity?.onBackPressed()
        })
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}