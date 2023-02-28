package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jsibbold.zoomage.ZoomageView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.Spotlight
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.utils.Constant.TAG_PROFILE_FRAGMENT
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream

@AndroidEntryPoint
class ProfileEditPicPage : BaseFragment(), BottomSheetOptions.BottomSheetListener {

    private val TAG = ProfileEditPicPage::class.java.simpleName

    lateinit var edit: TextView
    lateinit var blurImage: ImageView
    lateinit var image: ZoomageView
    lateinit var toolbar_head: TextView

    var mBottomSheetOptions: BottomSheetOptions? = null
    var image_uri: Uri? = null
    var loader: LoaderDialog? = null
    var typeOfImage: String? = null

    val userAccountViewModel: UserAccountViewModel by viewModels()

    var url: String? = null

    companion object {

        val TYPE = "type"
        val IMAGE_URL = "url"

        val PROFILE_TYPE = ""
        val OWN = "own"
        val OTHER = "others"

        val PROFILE_PIC = "profile_pic"
        val COVER_PIC = "cover_pic"
        val SPOTLIGHT1 = "spotlight1"
        val SPOTLIGHT2 = "spotlight2"
        val SPOTLIGHT3 = "spotlight3"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_eedit_pic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        addObserver()
        //observerInfoChange()
    }


    private fun initView(view: View) {
        edit = view.findViewById<TextView>(R.id.tv_edit).apply {
            setOnClickListener {
                MyApplication.putTrackMP(Constant.AcEditCoverImage, null)
                showBottomSheetDialogFragment(mBottomSheetOptions)
            }
        }
        blurImage = view.findViewById(R.id.iv_blur)
        image = view.findViewById(R.id.iv_image)
        toolbar_head = view.findViewById(R.id.toolbar_head)
        view.findViewById<ImageView>(R.id.iv_back).setOnClickListener { activity?.onBackPressed() }
        populateView()
        var delete: String? = null
        if(typeOfImage != PROFILE_PIC) {
//            delete="Delete"
            toolbar_head.text = "Cover Image"
        } else {
            toolbar_head.text = "Profile photo"
        }
        mBottomSheetOptions = BottomSheetOptions.getInstance("Take Photo", "Choose Photo", delete, null, null)
        mBottomSheetOptions?.setBottomSheetLitener(this)
        loader = LoaderDialog(requireActivity())
    }

    private fun populateView() {
        typeOfImage = Navigation.getFragmentData(this, TYPE)
        url = Navigation.getFragmentData(this, IMAGE_URL)
        url?.let {
            //Utils.stickImage(requireContext(),blurImage,it,null)
            Utils.blurImage(requireContext(), it, blurImage)
            Utils.stickImage(requireContext(), image, it, null)
        }
    }

    private fun setUp() {
        if(Navigation.getFragmentData(this, PROFILE_TYPE) == OTHER) {
            edit.visibility = View.GONE
        }
    }

    //ic_verified_tick

    private fun addObserver() {
        userAccountViewModel.observeProfilePictureChange().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " obsserverCamehereEdit:: ")
//            loader?.hideDialog()
            //Only Success here when percentage reaches 100 and response not null
            when(it) {
                is ResultHandler.Success -> {
                    userAccountViewModel.getFullProfile()
                }

                is ResultHandler.Failure -> {
                    loader?.hideDialog()
                    showToast(getString(R.string.sorry_try_again))
                }

            }
        })

        userAccountViewModel.observeFullProfileChange().observe(viewLifecycleOwner, {
            loader?.hideDialog()
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        var profile = activity?.supportFragmentManager?.findFragmentByTag(TAG_PROFILE_FRAGMENT) as ProfileFragment
                        PreferencesManager.put(it, PREFRENCE_PROFILE)
                        Log.i(TAG, "ProfileGetResponse:: their response")
                        profile.populateView()
                        activity?.onBackPressed()
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }


            /*it?.let {
                var profile = activity?.supportFragmentManager?.findFragmentByTag(TAG_PROFILE_FRAGMENT) as ProfileFragment
                PreferencesManager.put(it, PREFRENCE_PROFILE)
                Log.i(TAG, "ProfileGetResponse:: their response")
                loader?.hideDialog()
                profile.populateView()
                activity?.onBackPressed()

            }*/
        })

        userAccountViewModel.observeSpotlightChange().observe(viewLifecycleOwner, Observer {

            var profile = activity?.supportFragmentManager?.findFragmentByTag(TAG_PROFILE_FRAGMENT) as ProfileFragment?
            loader?.hideDialog()
            it?.let {
                PreferencesManager.put(it, PREFRENCE_PROFILE)
            }
            profile?.populateView()
            activity?.onBackPressed()
        })

        userAccountViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)
    }

    override fun bottomSheetClickedOption(buttonClicked: String) {
        Log.i(TAG, " buttonClivked::  1")
        when(buttonClicked) {
            BottomSheetOptions.BUTTON1 -> {
                Log.i(TAG, " buttonClivked::  2")
                MyApplication.putTrackMP(Constant.AcCoverTakePhoto, null)
                getImageFromCamera()
            }

            BottomSheetOptions.BUTTON2 -> {
                Log.i(TAG, " buttonClivked::  3")
                MyApplication.putTrackMP(Constant.AcCoverChoosePhoto, null)
                getImageFromGallary()
            }

            BottomSheetOptions.CANCEL  -> {
                MyApplication.putTrackMP(Constant.AcCoverCancelPhoto, null)
            }

            BottomSheetOptions.BUTTON3 -> {
                Log.i(TAG, " buttonClivked::  4 ${typeOfImage}")
                typeOfImage?.let {
                    when(it) {
                        SPOTLIGHT1 -> updateSpotLight(1)
                        SPOTLIGHT2 -> updateSpotLight(2)
                        SPOTLIGHT3 -> updateSpotLight(3)
                    }
                }
                //userAccountViewModel.deleteSpotLight()
            }
        }
    }

    private fun updateSpotLight(index: Int) {
        var spotligts = ArrayList<Spotlight?>(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.social?.spotlights)

        var removingSpotLight = ArrayList<Spotlight>()
        spotligts.map { slight ->
            Log.i("TAG", " deletingIndex:: $index")
            when(index) {
                1    -> {
                    slight?.one?.let {
                        removingSpotLight.add(slight)
                    }
                }

                2    -> {
                    slight?.two?.let {
                        removingSpotLight.add(slight)
                    }
                }

                3    -> {
                    slight?.three?.let {
                        removingSpotLight.add(slight)
                    }
                }

                else -> {
                }
            }
            Log.i("TAG", " updatingSpht:: ${spotligts}")
        }
        spotligts.removeAll(removingSpotLight)
        userAccountViewModel.deleteSpotLight(spotligts)

    }

    private fun getImageFromCamera() {
        Log.i(TAG, " buttonClivked::  6")
        if(Utils.checkReadWritePermission(requireContext())) {
            Log.i(TAG, " buttonClivked::  7")
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            image_uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
            startActivityForResult(cameraIntent, Constant.CAMERA_REQUEST)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 1001)

        }
    }

    private fun getImageFromGallary() {
        if(Utils.checkReadWritePermission(requireContext())) {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(Intent.createChooser(photoPickerIntent, "select Camera"), Constant.GALLERY_IMAGE_REQUEST)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 1001)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: 1")
        if(resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d(TAG, "onActivityResult: 2")
            data?.let { it1 ->
                loader?.show()
                val resultUri = UCrop.getOutput(it1) ?: return@let
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(resultUri)
                val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
                typeOfImage?.let {
                    when(it) {
                        PROFILE_PIC -> userAccountViewModel.updateProfilePicture(selectedImage)
                        COVER_PIC   -> userAccountViewModel.uploadCoverPicture(selectedImage)
                    }
                }

            }
        } else if(requestCode == Constant.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: 3")
            loader?.show()
            image_uri?.let {
                val destinationUri = Uri.fromFile(File(requireActivity().cacheDir, "post_${System.currentTimeMillis()}.jpg"))
                val options = UCrop.Options()
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
                options.setCompressionQuality(100)
                options.setMaxBitmapSize(10000)
                UCrop.of(it, destinationUri).withOptions(options).withMaxResultSize(1024, 1024)
                        .start(requireActivity())
            }
            //profile_pic.setImageURI(image_uri)
//            Utils.uriToBitmap(image_uri,requireContext())?.let { bitmap ->
//                typeOfImage?.let {
//                    var spotligh  = ArrayList<Spotlight?>(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.social?.spotlights)
//                    when(it){
//                        PROFILE_PIC ->  userAccountViewModel.updateProfilePicture(bitmap)
//                        SPOTLIGHT1 -> userAccountViewModel.updateSpotLightPicture(bitmap,spotligh,1)
//                        SPOTLIGHT2 -> userAccountViewModel.updateSpotLightPicture(bitmap,spotligh,2)
//                        SPOTLIGHT3 -> userAccountViewModel.updateSpotLightPicture(bitmap,spotligh,3)
//                    }
//                }
//            }?:run {
//                loader?.hideDialog()
//            }


        } else if(requestCode == Constant.GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: 4")
            try {
                val imageUri: Uri? = data?.data
                loader?.show()
                imageUri?.let {
                    val destinationUri = Uri.fromFile(File(requireActivity().cacheDir, "post_${System.currentTimeMillis()}.jpg"))
                    UCrop.of(it, destinationUri).withMaxResultSize(1024, 1024)
                            .start(requireActivity())
                }
//                val imageStream: InputStream? =
//                    requireActivity().contentResolver.openInputStream(imageUri!!)
//                val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
//                typeOfImage?.let {
//                    var spotligh  = ArrayList<Spotlight?>(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.social?.spotlights)
//                    when(it){
//                        PROFILE_PIC ->  userAccountViewModel.updateProfilePicture(selectedImage)
//                        SPOTLIGHT1 -> userAccountViewModel.updateSpotLightPicture(selectedImage,spotligh,1)
//                        SPOTLIGHT2 -> userAccountViewModel.updateSpotLightPicture(selectedImage,spotligh,2)
//                        SPOTLIGHT3 -> userAccountViewModel.updateSpotLightPicture(selectedImage,spotligh,3)
//                    }
//                }

            } catch(e: Exception) {
                Log.d(TAG, "onActivityResult: 5")
                loader?.hideDialog()
                Log.e(TAG, "Something went wrong to pick image ${e}")
            }

        } else {
            loader?.hideDialog()
        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }


}