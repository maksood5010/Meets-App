package com.meetsportal.meets.ui.fragments.socialfragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.TAG_PROFILE_FRAGMENT
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class UploadVaccineFragment : BaseFragment(){

    private val TAG = UploadVaccineFragment::class.java.simpleName

    private val ACTIVITY_CHOOSE_FILE: Int = 1005
    lateinit var progressLayout : LinearLayout
    lateinit var progressBar: ProgressBar
    lateinit var upload : RelativeLayout
    lateinit var tvPercent : TextView
    lateinit var filename : TextView
    lateinit var uploadbutton : TextView

    var uri:Uri?= null


    val userAccountViewModel : UserAccountViewModel by viewModels()
    //lateinit var loader: LoaderDialog


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_vaccine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }

    private fun initView(view: View) {
        progressLayout = view.findViewById(R.id.ll_progress)
        progressBar = view.findViewById(R.id.progress)
        upload = view.findViewById(R.id.rv_uploadImage)
        //loader = LoaderDialog(requireActivity())
        tvPercent = view.findViewById(R.id.tv_percent)
        tvPercent = view.findViewById(R.id.tv_percent)
        filename = view.findViewById(R.id.filename)
        uploadbutton = view.findViewById(R.id.upload)
        view.findViewById<ImageView>(R.id.back).setOnClickListener{activity?.onBackPressed()}

    }

    private fun setUp() {
        setListener()
        setObserver()
    }


    private fun setListener() {
        Utils.onClick(upload,2000){
            chooseFile()
        }
        /*RxView.clicks(upload).throttleFirst(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                chooseFile()
            })*/

        uploadbutton.setOnClickListener {
            uri?.let {
                //loader.showDialog()
                //loader.showPercent()
                userAccountViewModel.uploadVaccineCard(uri!!,"vaccination_card")
            }?:run{
                Toast.makeText(requireContext(),"Please select file first...",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setObserver() {
        userAccountViewModel.observeUploadDocument().observe(viewLifecycleOwner, Observer {
            it?.let {
                it.percent?.let {
                    Log.i(TAG," percent:: $it")
                    progressBar.setProgress(it.toInt())
                    tvPercent.setText(it.plus("%"))
                    //loader.setPercent(it.plus("%"))
                    //if(it.equals("100"))
                        //loader.hideDialog()
                }
                it.response?.let {
                    Log.i(TAG," sucessfully Uploaded:: ")
                    Toast.makeText(requireContext(),"Document uploded...",Toast.LENGTH_SHORT).show()
                    PreferencesManager.put(it.body(), Constant.PREFRENCE_PROFILE)
                    var profile = activity?.supportFragmentManager?.findFragmentByTag(TAG_PROFILE_FRAGMENT) as ProfileFragment
                    profile.populateView()
//                    (activity as MainActivity?)?.populateData()
                    //loader.hideDialog()
                    activity?.onBackPressed()
                }
            }
        })

        userAccountViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity,it)
        })
    }

    private fun chooseFile() {
        val chooseFile: Intent
        val intent: Intent
        chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFile.type = "*/*"
        intent = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, " onActivityResult:: 1")
        if(resultCode == RESULT_OK){
            Log.i(TAG, " onActivityResult:: 2")
            if(requestCode == ACTIVITY_CHOOSE_FILE){
                val path: String? = File(data?.data?.path).getAbsolutePath()
                Log.i(TAG, " paath:: $path")
                path?.let {
                    uri = data?.getData();
                    var filename: String? = null
                    val cursor: Cursor? = activity?.contentResolver?.query(
                        uri!!,
                        null,
                        null,
                        null,
                        null
                    )
                    cursor?.let {
                        Log.i(TAG, " cursor not null")
                        Log.i(TAG, " cursor null")
                        cursor?.moveToFirst()
                        val idx = cursor?.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        filename = cursor?.getString(idx!!)
                        cursor?.close()
                    }?:run {
                        filename=uri?.getPath()
                    }


                    val fileDescriptor: AssetFileDescriptor? =
                        activity?.getApplicationContext()?.getContentResolver()
                            ?.openAssetFileDescriptor(uri!!, "r")
                    val fileSize = fileDescriptor?.length


                    Log.i(TAG, " ${fileSize}")
                    Log.i(TAG, " filename:: $filename  extension:: $filename ")

                    when(fileSize?.compareTo(8000000)){
                        -1 -> {
                            showFile(filename,fileSize)
                            //userAccountViewModel.uploadVaccineCard(uri!!,"vaccination_card")
                        }else -> Toast.makeText(activity,"file size exceed limit !",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showFile(file: String?, fileSizeinByte: Long) {
        Log.i(TAG," fileSize:: ${fileSizeinByte}")
        var fileSize = ""
        if((fileSizeinByte/1024)>1024){
            fileSize = ((fileSizeinByte/1024)/1024).toString().plus("MB")
        }else{
            fileSize = (fileSizeinByte/1024).toString().plus("KB")
        }
        filename.text = file.plus(" ").plus(fileSize).plus("")
    }


    /*fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor =
            activity?.getContentResolver()?.query(contentUri!!, proj, null, null, null) ?: return null
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }*/

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}