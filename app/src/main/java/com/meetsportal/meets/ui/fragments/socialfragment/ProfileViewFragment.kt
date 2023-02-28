package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.Person4Adapter
import com.meetsportal.meets.adapter.ProfileViewStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentProfileViewBinding
import com.meetsportal.meets.models.ProfileView
import com.meetsportal.meets.models.WeeklyMetric
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcDpOnProfileView
import com.meetsportal.meets.utils.Constant.AcSeeThemProfileView
import com.meetsportal.meets.utils.Constant.AcUnlockProfileView
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import lecho.lib.hellocharts.model.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ProfileViewFragment : BaseFragment() {


    private var isSubscribed: Boolean = false
    private var profileViewsCount: Int? = null
    val userviewModel: UserAccountViewModel by viewModels()

    private var _binding: FragmentProfileViewBinding? = null
    private val binding get() = _binding!!


    companion object {

        val TAG = ProfileViewFragment::class.simpleName!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileViewBinding.inflate(inflater, container, false)
        return binding.root

        //return inflater.inflate(R.layout.fragment_meet_up_restaurant_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        setObserver()
        setFragmentResultListener("value") { key, result ->
            initView(view)
            setUp()
        }
    }

    private fun initView(view: View) {
        binding.tvUnlock.onClick({
            MyApplication.putTrackMP(AcUnlockProfileView, null)
            Navigation.addFragment(requireActivity(), ViewStatsFragment(), "ViewStatsFragment", R.id.homeFram, true, null)
        })
        binding.ivBack.onClick({ popBackStack() })
        binding.child1.rvCountryStat.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.child1.rvCountryStat.addItemDecoration(SpaceItemDecoration(0, Utils.dpToPx(20f, resources)))
        binding.tvUnlock.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#E120FF"), Color.parseColor("#482EC5")), 20f)

        initChart()
        getProfileInsight()
    }

    private fun setUp() {
        binding.child1.seeThem.setOnClickListener {
            MyApplication.putTrackMP(AcSeeThemProfileView, null)
            if(isSubscribed) {
                profileViewsCount?.let { w ->
                    if(w != 0) {
                        val profileViewPersonList = ProfileViewPersonList()
                        Navigation.setFragmentData(profileViewPersonList, "count", w.toString())
                        Navigation.addFragment(requireActivity(), profileViewPersonList, ProfileViewPersonList.TAG, R.id.homeFram, true, false)
                    }
                }
            } else {
                Navigation.addFragment(requireActivity(), ViewStatsFragment(), "ViewStatsFragment", R.id.homeFram, true, null)
            }
        }
    }


    fun getProfileInsight() {

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val current = Calendar.getInstance();
        val timeList = ArrayList<String?>()
        timeList.add(sdf.format(current.time))
        for(i in 0 until 6) {
            current.add(Calendar.DATE, -1)
            timeList.add(sdf.format(current.time))
        }
        Log.i(TAG, " Timeline:::-  $timeList")
        userviewModel.getProfileInsight(timeList.joinToString(","))

    }

    fun setLineChart(weeklyMetrics: List<WeeklyMetric>?) {

        Log.i(TAG, "setLineChart: 0")
        val normalView: MutableList<PointValue> = ArrayList()
        val boostedView: MutableList<PointValue> = ArrayList()

        Log.i(TAG, "setLineChart: 1")

        weeklyMetrics?.reversed()?.forEachIndexed { index, value ->
            normalView.add(PointValue(index.toFloat(), value.normal_views.toFloat()))
            boostedView.add(PointValue(index.toFloat(), value.boosted_views.toFloat()))
        }
        Log.i(TAG, "setLineChart: 2")
        val line = Line(normalView).setColor(Color.parseColor("#5385F5")).setCubic(true)
                .setSquare(true).setFilled(true).setShape(ValueShape.DIAMOND).setPointRadius(3)
                .setStrokeWidth(2).setHasLabels(true)
        /*line.setFormatter(object : LineChartValueFormatter{
            override fun formatChartValue(formattedValue: CharArray?, value: PointValue?): Int {

            }
        })*/
        val line2 = Line(boostedView).setColor(Color.parseColor("#8232C9")).setCubic(true)
                .setSquare(true).setPointRadius(3).setHasLabels(true)
        val lines: MutableList<Line> = ArrayList()
        lines.add(line)
        lines.add(line2)

        Log.i(TAG, "setLineChart: 3")

        val data = LineChartData()
        data.lines = lines


        var today = Calendar.getInstance()
        val sdf = SimpleDateFormat("ddMMM")
        var xAxisValueList = ArrayList<AxisValue>()
        xAxisValueList.add(AxisValue(6f).setLabel("today"))
        for(i in 5 downTo 0) {
            Log.i(TAG, " Checking unitll:::: $i")
            today.add(Calendar.DATE, -1)
            xAxisValueList.add(AxisValue(i.toFloat()).setLabel(sdf.format(today.time)))

        }

        Log.i(TAG, "setLineChart: 5")

        var xAxis = Axis().setHasLines(true).setName("Dates").setValues(xAxisValueList)
        var yAxis = Axis().setHasLines(true).setName("View")
        data.axisXBottom = xAxis
        data.axisYRight = yAxis
        var totalBoostView = 0
        var totalNormalView = 0
        weeklyMetrics?.forEach {
            totalBoostView += it.boosted_views
            totalNormalView += it.normal_views
        }
        Log.d(TAG, "setLineChart:$totalBoostView+$totalNormalView ")
        val total = totalBoostView + totalNormalView

        var chartSlices: ArrayList<SliceValue> = ArrayList()
        val sliceValue1 = SliceValue(totalBoostView.toFloat(), Color.parseColor("#8232C9"))
        val p1 = ((totalBoostView.toDouble() / total.toDouble()) * 100).toInt()
        sliceValue1.setLabel(("${p1}%").toCharArray())

        if(p1 != 0) chartSlices.add(sliceValue1)
        val sliceValue2 = SliceValue(totalNormalView.toFloat(), Color.parseColor("#5385F5"))
        val p2 = ((totalNormalView.toDouble() / total.toDouble()) * 100).toInt()
        sliceValue2.setLabel(("${p2}%").toCharArray())
        chartSlices.add(sliceValue2)
        var pieData = PieChartData(chartSlices)
        pieData.isValueLabelBackgroundEnabled = false
//        pieData.valueLabelTextSize
        val bolder = ResourcesCompat.getFont(requireContext(), R.font.poppins_bold)
        pieData.setHasLabels(true)
//        pieData.setHasLabelsOnlyForSelected(true);
        pieData.setHasLabelsOutside(false);
        pieData.setHasCenterCircle(false);
//        pieData.centerCircleScale=0.75f

        pieData.slicesSpacing = 0
//        pieData.centerText1Typeface= bolder
//        pieData.centerText1FontSize = 20
//        pieData.centerText1Color = ContextCompat.getColor(requireActivity(), R.color.primary)

//        pieData.centerText2Typeface= bolder
//        pieData.centerText2FontSize = 15
//        pieData.centerText2Color = ContextCompat.getColor(requireActivity(), R.color.primary)
//        pieData.centerText1 = "0%"

        Log.i(TAG, " populatViewOnen:::   5")
        binding.child1.chart.pieChartData = pieData
        binding.child1.chart.startDataAnimation()
        binding.child1.tvOrganicView.text = "$totalNormalView"
        binding.child1.tvBoostedView.text = "$totalBoostView"
//        binding.child1.lineChart.lineChartData = data
    }

    fun initChart() {
        //binding.flipper.child1.lineChart.isInteractive = true
//        binding.child1.lineChart.isInteractive = true
//        binding.child1.lineChart.zoomType = ZoomType.VERTICAL
        //binding.lineChart.isZoomEnabled = false
//        binding.child1.lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
        // setLineChart(it.value.weekly_metrics)
        //binding.child1.barChart.isInteractive = true
        //binding.child1.barChart.isZoomEnabled = false
    }


    /*private fun generatestackeddata(regionViews: List<RegionView>?) {
        var list = ArrayList<Column> ();
        var values = ArrayList<SubcolumnValue> ();
        regionViews?.forEach {
            values.clear()
            values.add(SubcolumnValue(it.stats.boosted_views.toFloat(),Color.parseColor("#8232C9")))
            values.add(SubcolumnValue(it.stats.normal_views.toFloat(),Color.parseColor("#5385F5")))
            var coulumn = Column(values)
            coulumn.setHasLabels(true);
            coulumn.setHasLabelsOnlyForSelected(true)
            list.add(coulumn)
        }
        var data = ColumnChartData(list)

        data.setStacked(true)

        if(true){
            var xAxisValueList  = ArrayList<AxisValue>()
            regionViews?.forEachIndexed{ index,value->
                xAxisValueList.add(AxisValue(index.toFloat()).setLabel(value.region))
            }

            var xAxis = Axis().setHasLines(true).setValues(xAxisValueList)
            var yAxis = Axis().setHasLines(true)
            //hasAxisName
            if(true){
                xAxis.setName("Country")
                yAxis.setName("Views")
            }
            data.axisXBottom= xAxis
            data.axisYLeft = yAxis
        }else {
            data.axisXBottom = null
            data.axisYLeft = null
        }
        binding.child1.barChart.columnChartData  = data
       // return data

    }*/

    private fun setObserver() {
        userviewModel.observerProfileInsight().observe(viewLifecycleOwner, {
            Log.i(TAG, " insightDataAlaaa:: $it")
            when(it) {
                is ResultHandler.Success -> {
                    isSubscribed = it?.value?.is_subscribed ?: false
                    profileViewsCount = it.value?.profile_views_count
                    binding.child1.count1.text = profileViewsCount.toString()
                            .plus(" people viewed your profile")
                    var total = 0
                    it.value?.weekly_metrics?.forEach { w ->
                        total += w.total_views
                    }
                    binding.child1.count2.text = total.toString()
                    it.value?.profile_views?.let { it1 -> setFirstItem(it1) }
                    Log.i(TAG, "setObserver: 1 isSubscribed isSubscribed")
                    if(isSubscribed) {
//                        binding.child1.viewFlipper1.displayedChild=1
                        binding.child1.viewFlipper2.displayedChild = 1
                        binding.child1.viewFlipper3.displayedChild = 1
                        binding.child1.rvCountryStat.adapter = ProfileViewStateAdapter(requireActivity(), it.value?.region_views)
                        binding.tvUnlock.visibility = View.GONE
                        setLineChart(it.value?.weekly_metrics)
                        //generatestackeddata(it.value?.region_views)
                    } else {
//                        binding.child1.viewFlipper1.displayedChild=2
                        Log.i(TAG, "setObserver: isSubscribed false")
                        binding.child1.viewFlipper2.displayedChild = 2
                        binding.child1.viewFlipper3.displayedChild = 2
                        binding.tvUnlock.visibility = View.VISIBLE
                    }

                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "Something went wrong!!", Toast.LENGTH_SHORT)
                            .show()
                }
            }
        })
    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            userviewModel.followUser(id)
        } else {
            userviewModel.unfollowUser(id)
        }
    }

    private fun setFirstItem(profileViews: List<ProfileView>) {
        if(profileViewsCount == 0) {
            binding.child1.viewFlipper1.visibility = View.GONE
        } else {
            binding.child1.viewFlipper1.visibility = View.VISIBLE
            val adapter = Person4Adapter(requireActivity(), profileViews.take(4), { sid, follow ->
                MyApplication.putTrackMP(Constant.AcFollowUnFollowProfileView, JSONObject(mapOf("sid" to sid)))
                followuser(sid, follow)
            }, { sid ->
                MyApplication.putTrackMP(AcDpOnProfileView, JSONObject(mapOf("sid" to sid)))
                openProfile(sid, Constant.Source.PROFILEINSIGHT.sorce.plus(MyApplication.SID))
            })
            binding.child1.rv4People.adapter = adapter
//            profileViews.getOrNull(0)?.let {
//                binding.child1.dp1.loadImage(requireActivity(),it.user_meta.profile_image_url,showSimmer = false)
//                binding.child1.tvName.text=it.user_meta.getName()
//                binding.child1.tvUserName.text="@".plus(it.user_meta.username)
//                if(it.user_meta.sid.equals(MyApplication.SID)){
//                    binding.child1.followUnfollow.visibility=View.GONE
//                }
//                if(it.followed_by_you){
//                    binding.child1.followUnfollow.text = "Unfollow"
//                    binding.child1.followUnfollow.setTextColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
//                    binding.child1.followUnfollow.setBackgroundResource(R.drawable.horizontal_round_shape)
//                }else{
//                    binding.child1.followUnfollow.text = "Follow"
//                    binding.child1.followUnfollow.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
//                    binding.child1.followUnfollow.setBackgroundResource(R.drawable.round_border_primary_bg)
//                }
//
//                if(it.user_meta.verified_user==true) {
//                    binding.child1.ivFirstVerify.visibility=View.VISIBLE
//                }else{
//                    binding.child1.ivFirstVerify.visibility=View.GONE
//                }
//            }?:run{
//                binding.child1.viewFlipper1.visibility=View.GONE
//            }
        }
    }


    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
        getProfileInsight()
    }
}