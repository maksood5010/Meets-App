package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable


@AndroidEntryPoint
class CropImageFragment : BaseFragment() {

    val TAG = CropImageFragment::class.simpleName

    lateinit var crop: CropImageView
    lateinit var done: Button
    var list = ArrayList<Bitmap>()
    var position: Int? = null
    var editImage: EditImageFragment? = null

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
        position = Navigation.getFragmentData(this, "data").toInt()

        list.clear()
        editImage = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_EDIT_POST_IMAGE_FRAGMENT) as EditImageFragment?
        list.addAll(editImage?.list as Collection<Bitmap>)
        var mainImageBitmap = list.get(position!!)
        crop.setImageBitmap(mainImageBitmap)
//        crop.setAspectRatio(1, 1)
//        crop.setFixedAspectRatio(true)
        crop.setOnCropImageCompleteListener(object : CropImageView.OnCropImageCompleteListener {
            override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
                Log.i("TAG", " croppedImage: ${result?.bitmap} ")
            }
        })

        compositeDisposable.add(Utils.onClick(done, 1000) {
            populateView()
            //viewModel.populatePickImage(UploadPost(images = poulate))
            activity?.onBackPressed()
        }
                /*RxView.clicks(done).throttleFirst(2,TimeUnit.SECONDS)
               .observeOn(AndroidSchedulers.mainThread()).map { done.getText().toString() }
               .subscribe(Consumer {
                   populateView()
                   //viewModel.populatePickImage(UploadPost(images = poulate))
                   activity?.onBackPressed()
               })*/)

    }

    fun populateView() {
        var poulate = ArrayList<Bitmap>()
        poulate.addAll(list)
        poulate.set(position!!, crop.getCroppedImage())
        editImage?.list = poulate
        editImage?.reSetUp()


    }

    private fun initView(view: View) {
        crop = view.findViewById(R.id.criv)
        done = view.findViewById(R.id.bt_done)
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