package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.PostSelectedAdapter
import com.meetsportal.meets.adapter.SearchPeopleAdapter
import com.meetsportal.meets.adapter.SearchPeopleTPPostAdapter
import com.meetsportal.meets.adapter.TextPostBGAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentCreatePostBinding
import com.meetsportal.meets.models.UploadPost
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.overridelayout.mention.SocialMentionAutoComplete
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.GALLERY_IMAGE_REQUEST
import com.meetsportal.meets.utils.Constant.GradientTypeArray
import com.meetsportal.meets.utils.Constant.TAG_EDIT_POST_IMAGE_FRAGMENT
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.io.InputStream
import java.lang.Exception


@AndroidEntryPoint
class CreatePost : BaseFragment(), View.OnClickListener {

    private var destinationUri: Uri?= null
    private var image_uri: Uri? = null
    private  val TAG = CreatePost::class.java.simpleName

    val viewModel : PostViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()

    var CAMERA = "camera"
    var GALLARY = "gallery"
    var IMAGE_FROM = CAMERA


    var list : ArrayList<Bitmap>? = ArrayList()
    lateinit var adapter :PostSelectedAdapter
    var searchMentionAdapter : SearchPeopleAdapter? = null
    var searchMentionTpPostAdapter : SearchPeopleTPPostAdapter? = null

    var loader : LoaderDialog? = null

    var compositeDisposable = CompositeDisposable()


    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!


    var gradientPosition = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()


    }


    fun initView(view: View){
        binding.noImage.onClick({
            binding.etCaption.requestFocus()
            showKeyboard(binding.etCaption)
        })
        binding.rlCamera.onClick({
           /* if(binding.llnormalPost.visibility == View.GONE){
                binding.llnormalPost.visibility = View.VISIBLE
                binding.rlTextPost.visibility = View.GONE
            }*/
//            else{
//                IMAGE_FROM = CAMERA
//                chooseImage()
//            }
            IMAGE_FROM = CAMERA
            chooseImage()
        })
        binding.rlGallary.onClick({
            /*if(binding.llnormalPost.visibility == View.GONE){
                binding.llnormalPost.visibility = View.VISIBLE
                binding.rlTextPost.visibility = View.GONE
            }*/
//            else{
//                IMAGE_FROM = GALLARY
//                chooseImage()
//            }
            IMAGE_FROM = GALLARY
            chooseImage()
        })
        /*binding.textPost.setOnClickListener {
            binding.llnormalPost.visibility = View.GONE
            binding.rlTextPost.visibility = View.GONE
        }*/
        loader = LoaderDialog(requireActivity())
        /*image = view.findViewById(R.id.civ_image)
        name = view.findViewById(R.id.tv_name)*/

    }

    fun setUp(){
        binding.rvGrad.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.rvSearchPeople.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.rvTvSuggestion.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        searchMentionAdapter = SearchPeopleAdapter(requireActivity(), SearchPeopleResponse()){
            Log.i(TAG, "setUp: checkignMentions:::c ")
            binding.etCaption.setMentioningText(it)
            binding.rvSearchPeople.visibility = View.GONE
        }

        searchMentionTpPostAdapter = SearchPeopleTPPostAdapter(requireActivity(),SearchPeopleResponse()){
            binding.tpText.setMentioningText(it)
            binding.rvTvSuggestion.visibility = View.GONE
        }

        binding.rvTvSuggestion.adapter = searchMentionTpPostAdapter
        binding.rvSearchPeople.adapter = searchMentionAdapter
        binding.rvGrad.adapter = TextPostBGAdapter(requireActivity(), GradientTypeArray){
            gradientPosition = it
            binding.rlTextPost.background = Utils.gradientFromColor(GradientTypeArray[gradientPosition].gradient)
            binding.tpText.setTextColor(Color.parseColor(GradientTypeArray[gradientPosition].textColor))
        }
        binding.cancelButton.setOnClickListener(this)
        /*UserProfile.currentUserProile?.photoUrl?.let {
            Glide.with(requireContext())
                .load(it)
                .error(R.drawable.profile_pic)
                .placeholder(R.drawable.profile_pic)
                .into(image)
        }
        name.text = UserProfile.currentUserProile?.displayName*/

       /* binding.etCaption.count {
            binding.tvCounter.text = "$it/1000"
        }*/



        binding.etCaption.addMentionListener(object : SocialMentionAutoComplete.MentionText{
            override fun getMentionedSubString(subString: String?) {
                Log.i(TAG," chekingMention:: $subString")
                profileViewmodel.searchPeople(subString)
            }
            override fun searchStart() {
                Log.i(TAG," searchSrated::: ")
            }
            override fun searchEnd() {
                binding.rvSearchPeople.visibility = View.GONE
            }
        })

        binding.tpText.addMentionListener(object :SocialMentionAutoComplete.MentionText{
            override fun getMentionedSubString(subString: String?) {
                Log.i(TAG," chekingMention:: $subString")
                profileViewmodel.searchPeople(subString)
            }
            override fun searchStart() {
                Log.i(TAG," searchSrated::: ")
            }
            override fun searchEnd() {
                binding.rvTvSuggestion.visibility = View.GONE
            }
        })

        adapter = PostSelectedAdapter(requireContext(),this, UploadPost().images) {
            val baseFragment:BaseFragment = Navigation.setFragmentData(
                EditImageFragment(), "data",
                it.toString()
            )
            Log.i(TAG," listSize:: ${list?.size}")
            Navigation.addFragment(
                requireActivity(),
                baseFragment,
                TAG_EDIT_POST_IMAGE_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )
        }
//        binding.rvSelectedImage.layoutManager = LinearLayoutManager(activity)
//        binding.rvSelectedImage.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f,resources),Utils.dpToPx(20f,resources)))
        binding.rvSelectedImage.adapter = adapter

        viewModel.pickedImage.observe(viewLifecycleOwner, Observer {
            updateAdapter()
            //adapter.updatelist(it.images!!)

        })

        compositeDisposable.add(
            Utils.onClick(binding.btPost,1000){
                //if(binding.llnormalPost.visibility ==  View.VISIBLE){
                Log.i(TAG, "bindingbtPost:: ${adapter.list?.size} ")
                    if(adapter.list?.size?.compareTo(0) == 0){
                        if(binding.etCaption.text.trim().toString().isNullOrBlank()){
                            showToast("Noting to post!!")
                        }else{
                            loader?.showDialog()
                            viewModel.uploadTextPost(binding.etCaption.text.trim().toString(),gradientPosition,binding.etCaption.getAllMentionPerson())

                        }
                    }else{
                        Log.i(TAG, "bindingbtPost:: 2 ")
                        loader?.showDialog()
                        loader?.showPercent()
                        viewModel.uploadPostImages(binding.etCaption.text.toString(), adapter.list!!, binding.etCaption.getAllMentionPerson())
                    }
                    /*validatePost()?.let {
                        MyApplication.showToast(requireActivity(),it)
                    }?:run{
                            loader?.showDialog()
                            loader?.showPercent()
                            viewModel.uploadPostImages(binding.etCaption.text.toString(), adapter.list!!, binding.etCaption.getAllMentionPerson())
                    }*/
                /*}else{
                    validateTextPost()?.let {
                        MyApplication.showToast(requireActivity(),it)
                    }?:run{
                        loader?.showDialog()
                        viewModel.uploadTextPost( binding.tpText.text.trim().toString(),gradientPosition,binding.tpText.getAllMentionPerson())
                    }
                }*/

                //parentFragmentManager.popBackStack()

            }
        )
    }


    fun checkMention(captionText: String) {
        captionText.lastIndexOf("@",ignoreCase = true)
    }

    private fun validatePost() :String?{
       /* if(binding.etCaption.text.toString().trim().equals("") || binding.etCaption.text.toString().isBlank())
            return "Caption is required"
        else*/ if(adapter.list?.size?.compareTo(0) == 0){
            return "Please select Image"
        }
            return null
    }

    private fun validateTextPost():String?{
        if(binding.tpText.text.trim().isNotEmpty()){
            return null
        }else
            return "Cannot post blank text!!!"
    }


    fun updateAdapter(){
        adapter.updatelist(list)
        showNoImage()
    }

    fun showNoImage() {
        if(list?.isEmpty() == true) {
            binding.noImage.visibility = View.VISIBLE
        } else {
            binding.noImage.visibility = View.GONE
        }
    }

    /**
     * if viewmodel send 100 in observer its means post created on aws server
     */
    private fun setObserver() {
        viewModel.observeUploadedImageCount().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, "setObserver: ImagePost ${it.value}")
                    if(it.value?.matches("-?\\d+(\\.\\d+)?".toRegex()) != true) {
                        loader?.hideDialog()
                        popBackStack()
                        var baseFragment: BaseFragment = DetailPostFragment()
                        Navigation.setFragmentData(baseFragment, "post_id", it.value)
                        Navigation.addFragment(requireActivity(), baseFragment, DetailPostFragment.TAG, R.id.homeFram, true, false)

                    }
                    loader?.setPercent(it.value.toString().plus(" of ").plus(list?.size)
                            .plus(" uploaded"))
                }

                is ResultHandler.Failure -> {
                    //showToast(it.message?:"")
                    loader?.hideDialog()
//                    showToast("Something  went wrong!!!")
                }
            }
            Log.i(TAG, " numbers:: $it")
        })

        viewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        viewModel.observeTextPost().observe(viewLifecycleOwner,{
            loader?.hideDialog()
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, "setObserver: textPost ${it.value}")
                    Log.i(TAG, "setObserver: textPost2 ${it.value?.get("_id")?.asString}")
                    popBackStack()
                    var baseFragment: BaseFragment = DetailPostFragment()
                    Navigation.setFragmentData(baseFragment, "post_id", it.value?.get("_id")?.asString)
                    Navigation.addFragment(requireActivity(), baseFragment, DetailPostFragment.TAG, R.id.homeFram, true, false)

                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Couldn't post! Please check Internet…")
                }
            }
        })
        profileViewmodel.observePeopleSearch().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    if(it.value?.isNotEmpty() == true){
                        //if(binding.llnormalPost.visibility ==  View.VISIBLE){
                            binding.rvSearchPeople.visibility = View.VISIBLE
                            searchMentionAdapter?.setItem(it.value)
                        /*}else{
                            binding.rvTvSuggestion.visibility = View.VISIBLE
                            searchMentionTpPostAdapter?.setItem(it.value)
                        }*/
                    }else{
                        binding.rvSearchPeople.visibility = View.GONE
                        //binding.rvTvSuggestion.visibility = View.GONE
                    }
                }
                is ResultHandler.Failure ->{
                    binding.rvSearchPeople.visibility = View.GONE
                    //binding.rvTvSuggestion.visibility = View.GONE
                    Log.e(TAG," people Search Failed::: ")
                }
            }
        })
    }

    fun selectImage(){

        if(Utils.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                100
            )
        }

    }

    fun pickImages() {

        if(IMAGE_FROM == CAMERA){
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            image_uri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
            startActivityForResult(cameraIntent, Constant.CAMERA_REQUEST)
        }else{
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            showToast("Select a maximum of 5 images")
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                GALLERY_IMAGE_REQUEST
            )
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.cancel_button -> parentFragmentManager.popBackStack()
        }
    }

    private fun chooseImage() {
        if (Utils.checkReadWritePermission(requireContext())) {
            //selectImage()

         pickImages()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), 1001
            )

        }
    }

    //Request Permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG," onRequestPermissionsResult::: $requestCode  ")
        when (requestCode) {
            1001 -> {
                Log.i(TAG," loging::: 1")
                var read = grantResults[0] == PackageManager.PERMISSION_GRANTED
                Log.i(TAG," loging::: 2")
                var write = grantResults[1] == PackageManager.PERMISSION_GRANTED
                Log.i(TAG," loging::: 3")
                var camera = grantResults[1] == PackageManager.PERMISSION_GRANTED
                Log.i(TAG," loging::: 4")
                if (grantResults.isNotEmpty() && read && write && camera) {
                    //selectImage()
                    Log.i(TAG," loging::: 5")
                    pickImages()
                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ) {
                    Log.i(TAG," loging::: 5")
                    rationale()
                } else {
                    /*requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), 1001
                    )*/
                }
            }


        }
    }

    //Force fully Open Permissions
    /*private fun rationale() {
        val builder: AlertDialog.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlertDialog.Builder(
                    requireContext(),
                    android.R.style.Theme_Material_Light_Dialog_Alert
                )
            } else {
                AlertDialog.Builder(requireContext())
            }
        builder.setTitle("Mandatory Permissions")
            .setMessage("Manually allow permissions in App settings")
            .setPositiveButton("Proceed") { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity?.packageName, null)
                intent.data = uri
                startActivityForResult(intent, 1)
            }
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }*/



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == Constant.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            image_uri?.let {
//                destinationUri = Uri.fromFile(File(requireActivity().cacheDir, "post_${System.currentTimeMillis()}.jpg"))
//                val options = UCrop.Options()
//                options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
//                options.setCompressionQuality(100)
//                options.setMaxBitmapSize(10000)
//                UCrop.of(it, destinationUri!!).withOptions(options)
////                        .withAspectRatio(1f,1f)
//                        .withMaxResultSize(1024, 1024)
//                        .start(requireActivity())

                openCrop(arrayListOf(it))
            }
//            Utils.uriToBitmap(image_uri, requireContext())?.let { bitmap ->
//                if(list?.size?.compareTo(10) == -1) {
//                    list?.add(bitmap)
//
//                    //viewModel.populatePickImage(UploadPost(images = list))
//                }
//                else{
//                    Toast.makeText(activity," can select maximum 10 images ",Toast.LENGTH_LONG).show()
//                    return
//                }
//            }
        }else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            data?.let { it1 ->

                val resultUri =  UCrop.getOutput(it1)?:return@let
                val imageStream: InputStream? =
                        requireActivity().contentResolver.openInputStream(resultUri)
                val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
                if(list?.size?.compareTo(5) == -1) {
                    list?.add(selectedImage)
                    //viewModel.populatePickImage(UploadPost(images = list))
                }
                else{
                    Toast.makeText(activity," can select maximum 5 images ",Toast.LENGTH_LONG).show()
                    return
                }
                destinationUri?.let {
                    val fDelete: File = File(it.path?:"")
                    if(fDelete.exists()) {
                        if(fDelete.delete()) {
                            Log.i(TAG, "onActivityResult: file Deleted")
                        } else {
                            Log.e(TAG, "onActivityResult: file Deleting some error occured")
                        }
                    }else{
                        Log.e(TAG, "onActivityResult: file Delete No such File")
                    }
                }

            }
        }else if(requestCode == GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            // Get the Image from data
            if (data?.getData() != null) {
                val mImageUri: Uri? = data.getData()
                mImageUri?.let {
//                    destinationUri = Uri.fromFile(File(requireActivity().cacheDir, "post_${System.currentTimeMillis()}.jpg"))
//                    val options = UCrop.Options()
//                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
//                    options.setCompressionQuality(100)
//                    options.setMaxBitmapSize(10000)
//                    UCrop.of(it, destinationUri!!).withOptions(options)
////                            .withAspectRatio(1f,1f)
//                            .withMaxResultSize(1024, 1024)
//                            .start(requireActivity())
                    openCrop(arrayListOf(it))
                }
            }else{
                if (data?.getClipData() != null){
                    val mClipData: ClipData? = data.getClipData()
                    val temp : ArrayList<Uri> = ArrayList()
                    var boolean=false
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData!!.getItemAt(i)
                        val uri = item.uri
                        val siz=list?.size?.plus(temp.size)
                        if(siz?.compareTo(5) == -1) {
//                            list?.add(Utils.uriToBitmap(uri, requireContext())!!)
                            temp?.add(uri)
                        } else{
//                            updateAdapter()
                            boolean=true
                        }
                    }
                    if(boolean){
                        showToast("can select maximum 5 images")
                    }
                    Log.i(TAG," tempCount:: ${temp?.size}")
                    if(temp.isNotEmpty()){ openCrop(temp) }
                }
            }
        } else {
//            Toast.makeText(activity, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
        updateAdapter()
    }

    private fun openCrop(images: ArrayList<Uri>?) {
        val b=Bundle()
        b.putParcelableArrayList("images",images)
        navigate(CropImagesParentFragment(this),b, )
        Log.i("TAG", "initView: Crop Crash 16")
    }


    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun onBackPressed(): Boolean {
        val showProceed = showProceed {
            parentFragmentManager.popBackStack()
        }
        showProceed.setMessage("Discard","Are you sure you want to discard this post?")
        return false
    }

    fun releaseAllBitamp(){
        list?.forEach {
            it?.recycle()
        }
        list = ArrayList()
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onDestroyView() {
        releaseAllBitamp()
        super.onDestroyView()
    }
}