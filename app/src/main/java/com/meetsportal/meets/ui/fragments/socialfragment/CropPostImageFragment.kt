package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.onClick
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.*


@AndroidEntryPoint
class CropPostImageFragment(val cropImagesParentFragment: CropImagesParentFragment, val uris: ArrayList<Uri>?, var position: Int) : BaseFragment() {

    val TAG = CropPostImageFragment::class.simpleName

    lateinit var crop: CropImageView
    lateinit var done: Button
    fun setPos(position: Int){
        this.position=position
        if(position == uris?.lastIndex) {
            done.setText("Done")
        } else done.setText("Next")
//        done.isEnabled=true
        setUp()
    }

    // val viewModel: PostViewModel by activityViewModels()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crop_image, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
    }

    private fun setUp() {
        Log.i(TAG, "initView: Crop Crash 3")
        val uri = uris?.getOrNull(position) ?: return
        crop.setImageUriAsync(uri)
//        crop.setAspectRatio(1, 1)
//        crop.setFixedAspectRatio(true)
        crop.setOnCropImageCompleteListener(object : CropImageView.OnCropImageCompleteListener {
            override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
                Log.i("TAG", " croppedImage: isnull==${result?.bitmap == null} ")
            }
        })
        compositeDisposable.add(
                done.onClick({ populateView() },2000)
                               )
        Log.i(TAG, "initView: Crop Crash 4")

    }

    fun populateView() {
        try{
            Log.i(TAG, "initView: Crop Crash 5")
//            done.isEnabled=false
            val croppedImage: Bitmap = crop.croppedImage ?: return
            cropImagesParentFragment.onCropFinish(croppedImage, position)
            Log.i(TAG, "initView: Crop Crash 511")
        }catch(e:Exception){
            Log.i(TAG, "initView: Crop Crash 6")
            Log.d(TAG, "populateView: ${e.printStackTrace()}")
            Toast.makeText(requireContext(), "File Format Not supported", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    private fun initView(view: View) {
        Log.i(TAG, "initView: Crop Crash 1")
        crop = view.findViewById(R.id.criv)
        done = view.findViewById(R.id.bt_done)
        if(position == uris?.lastIndex) {
            done.setText("Done")
        } else done.setText("Next")
        Log.i(TAG, "initView: Crop Crash 2")
        //done.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}