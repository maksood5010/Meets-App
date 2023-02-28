package com.meetsportal.meets.ui.fragments.socialfragment


import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ImagesViewAdapter
import com.meetsportal.meets.databinding.FragmentCropImagesBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import java.util.*


class CropImagesParentFragment(val createPost: CreatePost) :BaseFragment() {

    var list : ArrayList<Bitmap> = ArrayList()
    private var fragmentCrop: CropPostImageFragment? = null
    private var parcelableArrayList: ArrayList<Uri>? = null
    private var _binding: FragmentCropImagesBinding? = null
    private val binding get() = _binding!!

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        //return inflater.inflate(R.layout.fragment_detailed_post2,container,false)
        _binding = FragmentCropImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("TAG", "initView: Crop Crash 7")
        parcelableArrayList = arguments?.getParcelableArrayList<Uri>("images")
        val adapter= ImagesViewAdapter(requireActivity(),parcelableArrayList){
        }
        binding.rvImages.adapter=adapter
        parcelableArrayList?.get(0)?.let {
            setupFragment(parcelableArrayList,0)
        }
        Log.i("TAG", "initView: Crop Crash 8")
    }

    fun onCropFinish(bitmap: Bitmap, position: Int){
        Log.i("TAG", "initView: Crop Crash 9")
        Log.d("TAG", "onCropFinish: on position $position")
        list?.add(bitmap)
        if(parcelableArrayList?.getOrNull(position+1)!=null){
            fragmentCrop?.setPos(position+1)
        }else{
            createPost.list?.addAll(list)
            createPost.updateAdapter()
            requireActivity().onBackPressed()
        }
        Log.i("TAG", "initView: Crop Crash 10")
    }
    fun setupFragment(source: ArrayList<Uri>?, position: Int) {
        Log.i("TAG", "initView: Crop Crash 11")
        fragmentCrop = CropPostImageFragment(this,source,position)
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.cropFrame, fragmentCrop!!, "CropPostImageFragment").commitAllowingStateLoss()
        Log.i("TAG", "initView: Crop Crash 12")
    }

    override fun onBackPageCome() {

    }

}