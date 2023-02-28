package com.meetsportal.meets.ui.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.EditingToolsAdapter
import com.meetsportal.meets.adapter.FilterListener
import com.meetsportal.meets.adapter.FilterViewAdapter
import com.meetsportal.meets.models.ToolType
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.*
import com.meetsportal.meets.utils.Constant
import ja.burhanrashid52.photoeditor.*
import java.io.File

class MemeIt : BaseActivity() , View.OnClickListener,
    OnPhotoEditorListener, PropertiesBSFragment.Properties,
    EmojiBSFragment.EmojiListener, StickerBSFragment.StickerListener,
    EditingToolsAdapter.OnItemSelected, FilterListener, BottomSheetOptions.BottomSheetListener
{

    val TAG = MemeIt::class.java.simpleName


    lateinit var mPhotoEditorView: PhotoEditorView
    lateinit var mRvFilters : RecyclerView
    lateinit var mRootView: ConstraintLayout

    var mPropertiesBSFragment : PropertiesBSFragment? = null
    var mEmojiBSFragment : EmojiBSFragment? = null
    var mStickerBSFragment : StickerBSFragment? = null
    var mBottomSheetOptions : BottomSheetOptions? = null
    var mPhotoEditor: PhotoEditor? = null

    //private val mSaveFileHelper: FileSaveHelper? = null


    private var mIsFilterVisible = false
    private val mConstraintSet = ConstraintSet()

    private val mFilterViewAdapter: FilterViewAdapter = FilterViewAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_meme_it)
        supportActionBar?.hide()

        initViews()

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mBottomSheetOptions = BottomSheetOptions.getInstance("Share","Send to Weekend","Send in NewsFeed","Download",null)

        mBottomSheetOptions?.setBottomSheetLitener(this)
        mStickerBSFragment?.setStickerListener(this)
        mEmojiBSFragment?.setEmojiListener(this)
        mPropertiesBSFragment?.setPropertiesChangeListener(this);

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters.layoutManager = llmFilters
        mRvFilters.adapter = mFilterViewAdapter

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .build()



        mPhotoEditor?.setOnPhotoEditorListener(this)




    }

    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun initViews(){

        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_share).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_clear_edit).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_undo).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_redo).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_emoji).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_draw).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_filter).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_text).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_sticker).setOnClickListener(this)

        Glide.with(this).load(intent.getStringExtra(Constant.URL_EXTRA))
            .error(R.drawable.ic_person_placeholder)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(mPhotoEditorView.source)

    }


    override fun onEditTextChangeListener(view: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment = TextEditorDialogFragment.show(
            this,
            text!!, colorCode
        )
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditor {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
//todo:Check kar
                mPhotoEditor!!.editText(view!!, inputText, styleBuilder)
            }

        })
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            "TAG",
            "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            "TAG",
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d("TAG", "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d("TAG", "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.iv_undo -> mPhotoEditor?.undo()
            R.id.iv_redo -> mPhotoEditor?.redo()
            R.id.iv_back -> onBackPressed()
            R.id.iv_draw -> {
                mPhotoEditor?.setBrushDrawingMode(true)
                showBottomSheetDialogFragment(mPropertiesBSFragment)
            }
            R.id.iv_text -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        styleBuilder.withTextSize(80f)
                        mPhotoEditor!!.addText(inputText, styleBuilder)
                        //mTxtCurrentTool.setText(R.string.label_text)
                    }
                })
            }
            R.id.iv_filter -> showFilter(true)
            R.id.iv_emoji -> showBottomSheetDialogFragment(mEmojiBSFragment)
            R.id.iv_sticker -> showBottomSheetDialogFragment(mStickerBSFragment)
            R.id.iv_share -> showBottomSheetDialogFragment(mBottomSheetOptions)
            R.id.iv_clear_edit -> {
                Log.i("tag"," eraseBrussMode:: ")
                mPhotoEditor?.brushEraser()
            }
           /* R.id.iv_share -> {
                Log.i("tag"," openBottomSheet:: ")

            }*/



        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor?.brushColor = colorCode
        //mTxtCurrentTool.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor?.setOpacity(opacity)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor?.brushSize = brushSize.toFloat()
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor?.addEmoji(emojiUnicode)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor?.addImage(bitmap)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType?) {
       // //TODO("Not yet implemented")
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)
        if (isVisible) {
            mConstraintSet.clear(mRvFilters.id, ConstraintSet.START)
            mConstraintSet.connect(
                mRvFilters.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                mRvFilters.id, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                mRvFilters.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(mRvFilters.id, ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(mRootView, changeBounds)
        mConstraintSet.applyTo(mRootView)
    }

    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            //mTxtCurrentTool.setText(R.string.app_name)
        } else if (!mPhotoEditor!!.isCacheEmpty) {
            super.onBackPressed()
           //showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun saveImage() {
        val temp: File = File.createTempFile("edited", "img", this.getCacheDir())

        val saveSettings = SaveSettings.Builder()
            .setClearViewsEnabled(true)
            .setTransparencyEnabled(true)
            .build()

        mPhotoEditor?.saveAsBitmap(saveSettings, object : OnSaveBitmap {
            override fun onBitmapReady(bitmap: Bitmap?) {
                mPhotoEditorView.source.setImageBitmap(bitmap)
            }

            override fun onFailure(p0: Exception?) {
                Log.i("TAG", " failed:: ")
            }
        })

    }

    override fun bottomSheetClickedOption(buttonClicked: String) {
        Log.i(TAG," bottonClicked:: $buttonClicked")
    }

    /*private fun captureView(view: View): Bimap? {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }*/
}