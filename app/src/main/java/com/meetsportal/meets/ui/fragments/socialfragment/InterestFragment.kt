package com.meetsportal.meets.ui.fragments.socialfragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.AlphabetAdapter
import com.meetsportal.meets.adapter.InterestAdapter
import com.meetsportal.meets.adapter.SelectedInterestAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentChooseInterestBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterestFragment : BaseFragment() {

    val profileViewmodel: UserAccountViewModel by viewModels()

    private var _binding: FragmentChooseInterestBinding? = null
    private val binding get() = _binding!!

    var myInterest = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.interests


    var alphabetAdapter: AlphabetAdapter? = null


    var listInterestAdapter: InterestAdapter? = null
    var selected: ArrayList<Definition?> = ArrayList()
    var fullInterest = ArrayList<Definition?>()
    var currentChar = 'A'

    var selectedInterestAdapter: SelectedInterestAdapter? = null

    companion object {

        val TAG = InterestFragment::class.simpleName!!
        val NAME = "name"
        val SELECTED_LIST = "selectedist"

        enum class CALLING_INETEREST_FRAG(val callingFame: String) { MEETUP("meetUp"), MAP("map"), PROFILE("profile")
        }

        fun getInstance(callingTAG: String, selectedInterest: ArrayList<String?>): InterestFragment {
            return InterestFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(NAME, callingTAG)
                    putStringArrayList(SELECTED_LIST, selectedInterest)
                }
            }
        }
    }

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
     }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_detailed_post2,container,false)
        _binding = FragmentChooseInterestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        addObserver()
        Log.i(TAG, " checkingArguements::: $arguments")

        //var a = findViewById<MotionLayout>(R.id.motion_base)

    }

    private fun initView(view: View) {
        val tag = arguments?.getString(NAME)
        if(tag == "ProfileFragment") {
            binding.heading.text = "Share you interest"
            binding.tvDesc.text = "Share your interest to connect with others with similar interests"
        } else {
            binding.heading.text = "Select Up to 10 categories"
            binding.tvDesc.text = "Tab the category name to select it"
        }
        binding.etSearch.setRoundedColorBackground(requireActivity())
        binding.viewChosenInterest.setRoundedColorBackground(requireActivity(), corner = 20f)
        binding.rldialog.setRoundedColorBackground(requireActivity(), corner = 20f, enbleDash = true, strokeHeight = 2f, strokeColor = R.color.gray1, Dashgap = 0f, stripSize = 20f)
        binding.tvSave.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
        collapsLetter()
        binding.countSelected.text = (arguments?.getStringArrayList(SELECTED_LIST) ?: ArrayList()).size.toString()
                .plus(" interests selected")
        //binding.rvABC.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.selectedInterest.layoutManager = FlexboxLayoutManager(requireActivity()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.selectedInterest.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f, resources), Utils.dpToPx(10f, resources)))
        binding.rvABC.layoutManager = GridLayoutManager(requireContext(), 8)
        binding.rvInteres.layoutManager = FlexboxLayoutManager(requireActivity()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
        }
        listInterestAdapter = InterestAdapter(requireActivity(), ArrayList<Definition>(), selected, myInterest) {
            binding.countSelected.text = selected.size.toString().plus(" interests selected")
            selectedInterestAdapter?.notifyDataSetChanged()
        }

        selectedInterestAdapter = SelectedInterestAdapter(requireActivity(), selected, showCross = false)
        binding.selectedInterest.adapter = selectedInterestAdapter
        binding.rvInteres.adapter = listInterestAdapter
        binding.rvABC.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f, resources), Utils.dpToPx(10f, resources)))
        binding.seeAll.onClick({
            binding.tvChar.text = "See All"
            listInterestAdapter?.setItem(fullInterest)
            collapsLetter()
        }, 500)

        alphabetAdapter = AlphabetAdapter(requireActivity(), selected, ArrayList()) {
            currentChar = it.toChar()
            binding.tvChar.text = currentChar.toString()
            listInterestAdapter?.setItem(fullInterest.filter {
                it?.en?.get(0)?.equals(currentChar) == true
            })
            collapsLetter()
        }
        binding.rvABC.adapter = alphabetAdapter

    }


    private fun setUp() {

        transition(true)
        binding.ivBack.onClick({ activity?.onBackPressed() }, 500)
        profileViewmodel.getFullGenericList("interests")
        binding.llChangeList.onClick({
            alphabetAdapter?.updateItem()
            expandLetter()

        }, 100)
        binding.downArray.onClick({
            Log.i(TAG, " checkingArrayClick::: ")
            if(binding.downArray.rotation > 250) {
                transition(true)
            } else if(binding.downArray.rotation > 80) {
                transition(false)
            }
        }, binding.motionBase.transitionTimeMs)
        binding.etSearch.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                transition(true)
                collapsLetter()
            }
        }
        binding.etSearch.onClick({
            Log.i(TAG, " clicked:: ")
            transition(true)
            collapsLetter()
        }, 50)


        binding.tvSave.onClick({

            Log.i(TAG, " checking::: 0  ${arguments}")

            var bundle = Bundle().apply {
                putStringArrayList("returnInterestKey", ArrayList(selected.map { it?.key }))
            }
            setFragmentResult(arguments?.getString(NAME) ?: "", bundle)
            parentFragmentManager.popBackStack()

            /*when(arguments?.getSerializable(NAME) as CALLING_INETEREST_FRAG) {
                CALLING_INETEREST_FRAG.MEETUP -> {
                    var bundle = Bundle().apply {
                        putStringArrayList("returnInterestKey", ArrayList(selected.map { it?.key }))
                    }
                    setFragmentResult("interestResult", bundle)
                    parentFragmentManager.popBackStack()
                }
                CALLING_INETEREST_FRAG.PROFILE ->{
                    var bundle = Bundle().apply {
                        putStringArrayList("returnInterestKey", ArrayList(selected.map { it?.key }))
                    }
                    setFragmentResult("interestResult", bundle)
                    profileViewmodel.updateInterest(selected)
                }
                CALLING_INETEREST_FRAG.MAP ->{
                    var bundle = Bundle().apply {
                        putStringArrayList("returnInterestKey", ArrayList(selected.map { it?.key }))
                    }
                    setFragmentResult("interestResult", bundle)
                    parentFragmentManager.popBackStack()
                }
            }*/
        }, 500)

        binding.etSearch.count {
            binding.tvChar.text = "Search"
            listInterestAdapter?.setItem(fullInterest.filter { it?.en?.contains(binding.etSearch.text.toString(), true) == true })
        }

    }

    fun transition(shrink: Boolean) {
        Log.i(TAG, " shrinkshrink:: $shrink")
        if(shrink) {
            binding.motionBase.transitionToEnd()
            //binding.downArray.animate().setDuration(100).rotation(180f).start()
        } else {
            binding.motionBase.transitionToStart()
            // binding.downArray.animate().setDuration(100).rotation(0f).start()
        }
    }

    fun collapsLetter() {
        if(binding.rldialog.scaleX > 0.9) {
            binding.rldialog.animate().scaleX(0.0f).scaleY(0.0f).setDuration(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            binding.rldialog.elevation = 0f
                        }
                    }).start()
        }
    }

    fun expandLetter() {
        binding.rldialog.animate().scaleX(1.0f).scaleY(1.0f)
                .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(100)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        binding.rldialog.elevation = 1f
                    }

                }).start()
    }

    private fun addObserver() {
        profileViewmodel.observeFullInterestChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    selected.clear()
                    var comingList = arguments?.getStringArrayList(SELECTED_LIST) ?: ArrayList()
                    it.value?.let {
                        selected.addAll(it.definition.filter {
                            comingList.contains(it?.key)
                        })
                    }
                    selectedInterestAdapter?.notifyDataSetChanged()
                    /*myInterest?.let{
                        it?.forEach { myint ->
                            selected.add(it?.filter { it?.key == myint?.key }?.getOrNull(0))
                        }
                    }*/
                    alphabetAdapter?.putAllInterest(it.value?.definition ?: ArrayList())
                    fullInterest.clear()
                    fullInterest.addAll(it.value?.definition ?: ArrayList())
                    listInterestAdapter?.setItem(fullInterest.filter {
                        it?.en?.get(0)?.equals(currentChar) == true
                    })
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!")
                }
            }
        })

        profileViewmodel.observeInterestChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Interest saved!!!")
                    Log.i(TAG, " MuProfile:: ${it}")
                    PreferencesManager.put(it.value, Constant.PREFRENCE_PROFILE)
                    parentFragmentManager.popBackStack()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }


}

/*fun animate(shrink:Boolean){
        val animator = ValueAnimator()
        animator.setObjectValues(0.5f, 1.0f)
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setEvaluator(object :TypeEvaluator<Float>{
             override fun evaluate(fraction: Float, startValue: Float?, endValue: Float?): Float {
                 if(shrink)
                    return fraction
                 else{
                     return 1 - fraction
                 }
             }

        })
        animator.addUpdateListener { animation ->
            var fraction = animation.animatedValue as Float
            binding.viewChosenInterest.scaleX = 1 - (0.5 * fraction).toFloat()
            binding.viewChosenInterest.scaleY = 1 - (0.75 * fraction).toFloat()
            binding.viewChosenInterest.translationY = (binding.viewChosenInterest.height-(binding.viewChosenInterest.height.times(0.25))).div(2).times(fraction).toFloat()
            binding.countSelected.translationY = (binding.viewChosenInterest.height-(binding.viewChosenInterest.height.times(0.25))).div(2).times(fraction).toFloat()
            binding.downArray.translationY = ((binding.viewChosenInterest.height-(binding.viewChosenInterest.height.times(0.20))) * fraction).toFloat()
            binding.downArray.translationX = -Math.abs(((binding.viewChosenInterest.width * 0.25)*fraction).toFloat())
            binding.countSelected.alpha = fraction
            binding.downArray.rotation = 180 * fraction
            if(fraction > 0.9){
                (binding.viewChosenInterest.height-(binding.viewChosenInterest.height.times(0.25))).div(2).times(fraction).toFloat()
                binding.viewChosenInterest.setRoundedColorBackground(requireActivity(),corner = 30f)
            }
        }
        animator.duration = 200
        animator.start()
    }*/