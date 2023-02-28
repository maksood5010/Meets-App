package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.EditPostImageAdapter
import com.meetsportal.meets.adapter.SearchPeopleAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentEditPostBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.overridelayout.mention.SocialMentionAutoComplete
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable


@AndroidEntryPoint
class EditPostFragment : BaseFragment() {

    private val TAG = EditPostFragment::class.java.simpleName

    val viewModel: PostViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()

    var list: ArrayList<Bitmap>? = ArrayList()
    lateinit var adapter: EditPostImageAdapter
    var searchMentionAdapter: SearchPeopleAdapter? = null

    var loader: LoaderDialog? = null

    var compositeDisposable = CompositeDisposable()


    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_edit_post, container, false)
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()

    }

    private fun initView(view: View) {
        binding.cancelButton.setOnClickListener { activity?.onBackPressed() }
        binding.tvCounter.visibility = View.INVISIBLE
        loader = LoaderDialog(requireActivity())
    }

    private fun setUp() {
        var postId = Navigation.getFragmentData(this, "postId")
        Log.i(TAG, "PostId::: $postId")
        binding.rvSearchPeople.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvSelectedImage.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.etCaption.count {
            binding.tvCounter.text = "$it/1000"
        }
        searchMentionAdapter = SearchPeopleAdapter(requireActivity(), SearchPeopleResponse()) {
            binding.etCaption.setMentioningText(it)
            binding.rvSearchPeople.visibility = View.GONE
        }

        binding.rvSearchPeople.adapter = searchMentionAdapter
        compositeDisposable.add(Utils.onClick(binding.btUpdate, 1000) {
            validatePost()?.let {
                MyApplication.showToast(requireActivity(), it)
            } ?: run {
                loader?.showDialog()
                viewModel.updatePost(postId, binding.etCaption.text.toString()
                        .trim(), binding.etCaption.getAllMentionPerson())
            }
        })
        viewModel.fetchSinglePost(postId, null)
        binding.etCaption.addMentionListener(object : SocialMentionAutoComplete.MentionText {
            override fun getMentionedSubString(subString: String?) {
                Log.i(TAG, " chekingMention:: $subString")
                profileViewmodel.searchPeople(subString)
            }

            override fun searchStart() {
                Log.i(TAG, " searchSrated::: ")
            }

            override fun searchEnd() {
                binding.rvSearchPeople.visibility = View.GONE
            }
        })
    }

    private fun validatePost(): String? {
//        if(binding.etCaption.text.toString().trim().equals("") || binding.etCaption.text.toString()
//                    .isBlank()) return "Caption is required"
//        else
        if(adapter.list?.size?.compareTo(0) == 0) {
            return "Please select Image"
        }
        return null
    }

    private fun setObserver() {
        profileViewmodel.observePeopleSearch().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    if(it.value?.isNotEmpty() == true) {
                        binding.rvSearchPeople.visibility = View.VISIBLE
                        Log.i(TAG, " cheking persons:: ${it.value}")
                        searchMentionAdapter?.setItem(it.value)
                    } else {
                        binding.rvSearchPeople.visibility = View.GONE
                    }
                }

                is ResultHandler.Failure -> {
                    binding.rvSearchPeople.visibility = View.GONE
                    Log.e(TAG, " people Search Failed::: ")
                }
            }
        })

        viewModel.observeSinglePost().observe(viewLifecycleOwner, Observer { result ->
            when(result) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " postDetailCame:: ${result.value}")
                    binding.etCaption.setText(result.value?.body)
                    binding.etCaption.setAllMentioned(result.value?.mentions)

                    adapter = EditPostImageAdapter(requireActivity(), result.value?.media)
                    binding.rvSelectedImage.adapter = adapter
                }

                is ResultHandler.Failure -> {
                    //if(result.code == 404){
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                    //}
                }
            }
        })

        viewModel.onPostUpdate().observe(viewLifecycleOwner, Observer {
            loader?.hideDialog()
            when(it) {
                is ResultHandler.Success -> {
                    Toast.makeText(requireContext(), "Post Updated... ", Toast.LENGTH_SHORT).show()
                    var profile = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_PROFILE_FRAGMENT) as ProfileFragment?
                    var detailPost = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_DETAIL_POST_FRAGMENT) as DetailPostFragment?
                    detailPost?.setUp()
                    profile?.postAdapter?.refresh()
//                    (activity as MainActivity?)?.apply {
//                        (this.viewPageAdapter.instantiateItem(this.viewPager,0) as TimeLineFragment).refreshTimeLine()
//                    }
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }

        })
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}