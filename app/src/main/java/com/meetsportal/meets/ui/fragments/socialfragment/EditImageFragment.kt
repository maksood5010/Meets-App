package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ImageFilterOptionAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.TAG_CREATE_POST_FRAGMENT
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.uvstudio.him.photofilterlibrary.PhotoFilter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditImageFragment : BaseFragment(), View.OnClickListener {

    val TAG = EditImageFragment::class.simpleName

    var list: ArrayList<Bitmap>? = ArrayList()

    var processedImageArray: Array<Bitmap?> = arrayOfNulls(17)


    var optionImagebitmap: Bitmap? = null
    var mainImageBitmap: Bitmap? = null
    var listBistmap: ArrayList<Bitmap> = ArrayList()

    lateinit var filteredImage: ImageView
    lateinit var filter: PhotoFilter
    lateinit var filterrecyclerView: RecyclerView
    lateinit var done: TextView
    lateinit var cancel: TextView
    lateinit var crop: TextView
    lateinit var temp: Bitmap
    var createpost: CreatePost? = null

    var position: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_post_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }

    private fun initView(view: View) {

        filteredImage = view.findViewById(R.id.iv_filtered)
        filterrecyclerView = view.findViewById(R.id.rv_filter_option)
        done = view.findViewById(R.id.tv_done)
        cancel = view.findViewById(R.id.tv_cancel)
        crop = view.findViewById(R.id.tv_crop)

        done.setOnClickListener(this)
        cancel.setOnClickListener(this)
        crop.setOnClickListener(this)


        filter = PhotoFilter()
        createpost = activity?.supportFragmentManager?.findFragmentByTag(TAG_CREATE_POST_FRAGMENT) as CreatePost?


    }

    private fun setUp() {


        position = Navigation.getFragmentData(this, "data").toInt()

        list?.clear()
        Log.i("TAG", " createpostlist:: ${createpost?.list!!} ")
        list = createpost?.list


        //createpost.updateAdapter()
        //list?.clear()
        //list?.addAll(list)


        reSetUp()


        var adapter = ImageFilterOptionAdapter(requireContext(), listBistmap) {
            applyFilter(it)
        }
        filterrecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        filterrecyclerView.adapter = adapter

    }

    fun reSetUp() {
        mainImageBitmap = list?.get(position!!)
        temp = mainImageBitmap!!
        optionImagebitmap = mainImageBitmap?.copy(mainImageBitmap?.config, mainImageBitmap?.isMutable!!)
        optionImagebitmap = Utils.getResizedBitmap(optionImagebitmap!!, 200)
//        filteredImage.setImageBitmap(mainImageBitmap)
        Glide.with(requireActivity()).load(mainImageBitmap)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.ic_pin_location).error(R.drawable.bg_spinner)
                .into(filteredImage)
        createImageBitmapArray()

    }

    private fun applyFilter(position: Int) {

        processedImageArray.getOrNull(position)?.let {
            temp = it
            Glide.with(requireActivity()).asBitmap().placeholder(R.drawable.bg_spinner).load(temp)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.bg_spinner)
                    .into(filteredImage)
        } ?: run {
            temp = when(position) {
                0    -> mainImageBitmap!!
                1    -> filter.one(activity?.applicationContext, mainImageBitmap)
                2    -> filter.two(activity?.applicationContext, mainImageBitmap)
                3    -> filter.three(activity?.applicationContext, mainImageBitmap)
                4    -> filter.four(activity?.applicationContext, mainImageBitmap)
                5    -> filter.five(activity?.applicationContext, mainImageBitmap)
                6    -> filter.six(activity?.applicationContext, mainImageBitmap)
                7    -> filter.seven(activity?.applicationContext, mainImageBitmap)
                8    -> filter.eight(activity?.applicationContext, mainImageBitmap)
                9    -> filter.nine(activity?.applicationContext, mainImageBitmap)
                10   -> filter.ten(activity?.applicationContext, mainImageBitmap)
                11   -> filter.eleven(activity?.applicationContext, mainImageBitmap)
                12   -> filter.twelve(activity?.applicationContext, mainImageBitmap)
                13   -> filter.thirteen(activity?.applicationContext, mainImageBitmap)
                14   -> filter.fourteen(activity?.applicationContext, mainImageBitmap)
                15   -> filter.fifteen(activity?.applicationContext, mainImageBitmap)
                16   -> filter.sixteen(activity?.applicationContext, mainImageBitmap)
                else -> mainImageBitmap!!
            }
            if(position <= 16) processedImageArray[position] = temp

            Glide.with(requireActivity()).asBitmap().placeholder(R.drawable.bg_spinner).load(temp)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.bg_spinner)
                    .into(filteredImage)
//        filteredImage.setImageBitmap(temp)
            // list.set(this.position!!,temp!!)
        }
    }


    private fun createImageBitmapArray() {
        Log.i("TAG", " createImageBitmapArray:: ")
        listBistmap.clear()
        listBistmap.add(mainImageBitmap!!)
        listBistmap.add(filter.one(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.two(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.three(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.four(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.five(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.six(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.seven(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.eight(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.nine(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.ten(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.eleven(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.twelve(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.thirteen(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.fourteen(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.fifteen(activity?.applicationContext, optionImagebitmap))
        listBistmap.add(filter.sixteen(activity?.applicationContext, optionImagebitmap))
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tv_done   -> {
                populateCreatePost()
                activity?.onBackPressed()
            }

            R.id.tv_cancel -> activity?.onBackPressed()

            R.id.tv_crop   -> {
                val baseFragment: BaseFragment = Navigation.setFragmentData(CropImageFragment(), "data", position.toString())
                Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_CROP_IMAGE_FRAGMENT, R.id.homeFram, true, false)
            }
        }
    }

    fun populateCreatePost() {
        var populate: ArrayList<Bitmap>? = ArrayList<Bitmap>()
        populate?.addAll(list as Collection<Bitmap>)
        populate?.set(this.position!!, temp)
        createpost?.list = populate
        createpost?.updateAdapter()
    }


    fun releaseAllBitamp() {
        try {
            optionImagebitmap?.recycle()
            optionImagebitmap = null
            processedImageArray.forEach {
                if(createpost?.list?.contains(it) == false) it?.recycle()
            }
            listBistmap.forEach {
                if(createpost?.list?.contains(it) == false) it?.recycle()
            }
            listBistmap = ArrayList()

        } catch(e: Exception) {
            Log.e(TAG, "onDestroyView: Unabke to clear bitmap")
        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun onDestroyView() {
        releaseAllBitamp()
        super.onDestroyView()
    }

}