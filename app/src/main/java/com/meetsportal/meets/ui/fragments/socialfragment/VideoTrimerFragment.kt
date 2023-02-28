package com.meetsportal.meets.ui.fragments.socialfragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentVideoTrimerBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VideoTrimerFragment(val path: String) : BaseFragment() {

    val TAG = VideoTrimerFragment::class.simpleName

    private var absPlayerInternal: SimpleExoPlayer? = null
    private var _binding: FragmentVideoTrimerBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        _binding = FragmentVideoTrimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controller = MediaController(requireContext(), true)

//        binding.videoViewer.setMediaController(controller)
//        binding.videoViewer.setVideoURI(Uri.parse(path))
//        binding.videoViewer.setOnPreparedListener {
//            controller.setAnchorView(binding.videoViewer)
//        }
//        binding.videoViewer.start()
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val trackSelectorDef: TrackSelector = DefaultTrackSelector()

        absPlayerInternal = ExoPlayerFactory.newSimpleInstance(requireContext(), trackSelectorDef)

        val userAgent: String = Util.getUserAgent(requireContext(), getString(R.string.app_name))

        val defdataSourceFactory = DefaultDataSourceFactory(requireContext(), userAgent)
        val uriOfContentUrl = Uri.parse(path)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(defdataSourceFactory)
                .createMediaSource(uriOfContentUrl)

        absPlayerInternal?.prepare(mediaSource)
        absPlayerInternal?.playWhenReady = true
        absPlayerInternal?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == ExoPlayer.STATE_BUFFERING) {
                    binding.progressVideoDetails.visibility = View.VISIBLE
                } else {
                    binding.progressVideoDetails.visibility = View.GONE
                }
            }
        })

        binding.videoFullScreenPlayer.setPlayer(absPlayerInternal)

    }

    override fun onResume() {
        super.onResume()
    }

    private fun stopPlayer() {
        absPlayerInternal?.release()
        absPlayerInternal = null
    }

    private fun pausePlayer() {
        absPlayerInternal?.playWhenReady = false
        absPlayerInternal?.getPlaybackState()
    }

    override fun onPause() {
        pausePlayer()
        super.onPause()
    }

    override fun onDestroyView() {
        stopPlayer()
        super.onDestroyView()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }


}