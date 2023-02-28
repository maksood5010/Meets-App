package com.meetsportal.meets.ui.fragments.verification

import android.Manifest
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentVerificationCameraBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.fragments.verification.VerificationPagerFragment.Companion.IMAGE_TYPE
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setGradient
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class VerificationCamera : BaseVerificationFragment() {


    val pagerFragment: VerificationPagerFragment? by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(VerificationPagerFragment.TAG) as VerificationPagerFragment?
    }
    val profileViewmodel: UserAccountViewModel by viewModels()

    private var imageCapture: ImageCapture? = null

    private lateinit var opDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    companion object {

        val TAG = VerificationCamera::class.simpleName!!
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


        fun getInstance(verificationNumber: Int): VerificationCamera {
            return VerificationCamera().apply {
                arguments = Bundle().apply {
                    putInt(IMAGE_TYPE, verificationNumber)
                }
            }
        }
    }


    private var _binding: FragmentVerificationCameraBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentVerificationCameraBinding.inflate(inflater, container, false)
        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }

    private fun initView() {
        profileViewmodel.getPreVerificationData()
        if(arguments?.getInt(IMAGE_TYPE) == 1) {
            binding.tvPressCount.text = "Pose Two"
            binding.ivImage.setImageResource(R.drawable.head_tilt_to_shoulder)
        } else {
            binding.tvPressCount.text = "Pose One"
            binding.ivImage.setImageResource(R.drawable.close_eye)
        }

        binding.tvPressCount.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#C95A32"), Color.parseColor("#32BFC9")), 30f)
    }

    private fun setUp() {
        if(Utils.allPermissionsForCamera(requireActivity(), REQUIRED_PERMISSIONS)) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        binding.capture.onClick({
            takePhoto()
        })
        opDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()


    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(opDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(Companion.TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                if(arguments?.getInt(IMAGE_TYPE) == 1) setDataInSecondResultPage(savedUri)
                else setDataInFirstResultPage(savedUri)

                Log.i(TAG, " ImageUri:: $savedUri")

                /*val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.d(Companion.TAG, msg)*/
            }
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            //val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(Companion.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    fun setDataInFirstResultPage(uri: Uri?) {
        Log.i(TAG, " uriCame::: $uri")
        image1Uri = uri
        (pagerFragment?.pagerAdapter?.instantiateItem(pagerFragment!!.binding.verifyPager, 2) as VerificationStepResult?)?.putImageResult(uri)
        pagerFragment?.binding?.verifyPager?.currentItem = 2
        activity?.onBackPressed()
    }

    fun setDataInSecondResultPage(uri: Uri?) {
        Log.i(TAG, " uriCame::: $uri")
        image2Uri = uri
        (pagerFragment?.pagerAdapter?.instantiateItem(pagerFragment!!.binding.verifyPager, 4) as VerificationStepResult?)?.putImageResult(uri)
        pagerFragment?.binding?.verifyPager?.currentItem = 4
        activity?.onBackPressed()
    }

    private fun setObserver() {
        /**
         * this is observing preVerification data
         */
        profileViewmodel.observePreVerifyChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    when(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)) {
                        0 -> {
                            binding.ivImage.loadImage(requireContext(), it.value?.definition?.pose_1)
                        }

                        1 -> {
                            binding.ivImage.loadImage(requireContext(), it.value?.definition?.pose_2)
                        }
                    }
                }

                is ResultHandler.Failure -> {

                }
            }
        })

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(Utils.allPermissionsForCamera(requireActivity(), REQUIRED_PERMISSIONS)) {
                startCamera()
            } else {
                MyApplication.showToast(requireActivity(), "Permissions not granted!!!")
                activity?.onBackPressed()
            }
        }
    }

    override fun image1UriCame(uri: Uri?) {
        Log.i(TAG, " image1UriCame:: $uri ")


    }

    override fun image2UriCame(uri: Uri?) {

        Log.i(TAG, " image1UriCame:: $uri ")

    }


    override fun onBackPageCome() {

    }
}