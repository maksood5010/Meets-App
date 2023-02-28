package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.ContactAdapter
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.databinding.FragmentContactListBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


@AndroidEntryPoint
class ContactListFragment : BaseFragment() {

    companion object {

        val TAG = ContactListFragment::class.java.simpleName!!

    }

    var list = ArrayList<Contact>()
    var allList = ArrayList<Contact>()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    var subject: PublishSubject<Contact> = PublishSubject.create()
    var subjectContact: PublishSubject<Contact> = PublishSubject.create()
    var searchContactDisposable: Disposable? = null

    lateinit var adapter: ContactAdapter

    val meetUpViewModel: MeetUpViewModel by viewModels()

    var createMeetPager: MeetUpViewPageFragment? = null
    var addFriendMeetpager: AddFriendToMeetUp? = null
    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        addObserver()
    }

    private fun initView(view: View) {
        createMeetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        addFriendMeetpager = activity?.supportFragmentManager?.findFragmentByTag(AddFriendToMeetUp::class.simpleName) as AddFriendToMeetUp?

    }

    private fun setUp() {
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //meetUpViewModel.getAllContact()
//        getContactFromPhone(false)
        /*compositeDisposable.add(
            subject.subscribe({
                Log.i(TAG, " tryingtoInsertContact::: ")
                meetUpViewModel.addContactInDb(it)
            }, {
                Log.e(TAG, " error while getting Contact::: ${it.printStackTrace()}")
            })
        )*/
        compositeDisposable.add(subjectContact.subscribe({
            Log.i(TAG, " tryingtoInsertContact::: ")
            list.firstOrNull { first -> first.number == it.number?.replace(" ", "") }?.let {

            } ?: run {
                Log.i(TAG, " addedContact::: $it")
                it.number = it.number?.replace(" ", "")
                list.add(it)
                list.sortBy { it.name }
            }
            adapter.notifyDataSetChanged()
            allList.clear()
            allList.addAll(list)
        }, {
            Log.e(TAG, " error while getting Contact::: ${it.printStackTrace()}")
        }))

        adapter = ContactAdapter(requireContext(), list) {
            Log.i(TAG, " listenerCalled:: 1")
            var selectedList = list.filter { it.selected == true }

            poplulateSelectedList(selectedList)

        }
        binding.rvContacts.adapter = adapter
        if(Utils.checkPermission(requireContext(), Manifest.permission.READ_CONTACTS)) {
            getCon()
        }
    }

    private fun poplulateSelectedList(selectedList: List<Contact>) {
        (createMeetPager?.pagerAdapter?.instantiateItem(createMeetPager?.binding?.meetUpViewpager!!, 0) as MeetUpFriendtBottomFragment?)?.poplulateSelectedContactList(selectedList)
        addFriendMeetpager?.poplulateSelectedContactList(selectedList)
    }

    /*fun getContactFromPhone(isItRefresh : Boolean){
        var contactObservable: Observable<ArrayList<Contact>> =
            Observable.fromCallable { Utils.getContactList(requireActivity(), subject) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        if(!isItRefresh)
            binding.progressBar.visibility = View.VISIBLE
        contactObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<Contact>> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(TAG, " onSubscribe:: ")
                    compositeDisposable.add(d)
                }

                override fun onNext(t: ArrayList<Contact>) {
                    Log.i(TAG, " onNext:: ${t.size}")
                    if(!isItRefresh){
                        list.clear()
                        list.addAll(t.sortedWith(compareBy { it.name }))
                        //list = ArrayList<Contact>(t.sortedWith(compareBy { it.name }))
                        adapter.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                    }
                    //meetUpViewModel.getAllContact()

                }

                override fun onError(e: Throwable) {
                    Log.i(TAG, " onError:: ${e.printStackTrace()}")
                }

                override fun onComplete() {
                    Log.i(TAG, " onComplete:: ")
                }

            })
    }*/
    private val PROJECTION: Array<String> = arrayOf<String>(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
    fun getCon() {
        Log.d(TAG, "getCon: called")
        val cr: ContentResolver = requireActivity().contentResolver
        val cursor: Cursor? = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null)
        if(cursor != null) {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val nameIndex: Int = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val idIndex: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                var name: String
                var number: String
                var id: String
                while(cursor.moveToNext()) {
                    name = cursor.getString(nameIndex)
                    number = cursor.getString(numberIndex).replace("+[^0-9]", "")
                    id = cursor.getString(idIndex)
                    val c = Contact(id, name, number)
                    list.add(c)
                }
            } finally {
                binding.progressBar.visibility = View.GONE
                cursor.close()
                list.sortBy { it.name }
                list.filter {
                    it.number != null && it.number.isNullOrEmpty()
                }
                adapter.setSearchedList(list)
            }
        }

    }

    fun getContactFromPhone(isItRefresh: Boolean) {
        Log.i(TAG, " fetchingContact::  0")
        Observable.fromCallable { Utils.getContactList(requireActivity(), subjectContact) }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Contact>> {
                    override fun onNext(t: ArrayList<Contact>) {
                        Log.i(TAG, " cleared::: ")
                        compositeDisposable.clear()
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onError(e: Throwable) {
                        ////TODO("Not yet implemented")
                    }

                    override fun onComplete() {
                        ////TODO("Not yet implemented")
                    }

                })


        binding.progressBar.visibility = View.GONE


        //Utils.getContactList(requireActivity(), subjectContact)

        /*if(!isItRefresh)
            binding.progressBar.visibility = View.VISIBLE
        contactObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<Contact>> {
                override fun onSubscribe(d: Disposable) {
                    Log.i(TAG, " onSubscribe:: ")
                    compositeDisposable.add(d)
                }

                override fun onNext(t: ArrayList<Contact>) {
                    Log.i(TAG, " onNext:: ${t.size}")
                    if(!isItRefresh){
                        list.clear()
                        list.addAll(t.sortedWith(compareBy { it.name }))
                        //list = ArrayList<Contact>(t.sortedWith(compareBy { it.name }))
                        adapter.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                    }
                    //meetUpViewModel.getAllContact()

                }

                override fun onError(e: Throwable) {
                    Log.i(TAG, " onError:: ${e.printStackTrace()}")
                }

                override fun onComplete() {
                    Log.i(TAG, " onComplete:: ")
                }

            })*/
    }

    fun getSearchedResult(list: List<Contact>) {
        adapter.setSearchedList(list)
        adapter.notifyDataSetChanged()
    }

    fun searchContact(query: String?) {
//        Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show()\
        if(query.isNullOrEmpty()) {
            adapter.setSearchedList(list)
        } else {
            val filter = list.filter { it.name?.contains(query, true) == true || it.number?.contains(query, true) == true }
            adapter.setSearchedList(filter)
        }
    }

    private fun addObserver() {
        meetUpViewModel.observeList().observe(viewLifecycleOwner, {
            Log.i(TAG, " searchedList::: ${it?.size}")
            if(it.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                list.clear()
                list.addAll(it.sortedWith(compareBy { it.name }))
                adapter.notifyDataSetChanged()
                getContactFromPhone(true)
            } else {
                getContactFromPhone(false)
            }
        })
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, " onDetachonDetach:: ${this::class.simpleName}")
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ContactList")
    }
}