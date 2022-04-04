package xyz.vitorvigano.playground.ui

import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import xyz.vitorvigano.playground.framework.MyAudioService
import xyz.vitorvigano.playground.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var mediaBrowser: MediaBrowser
    private lateinit var mediaController: MediaController

    private val mediaBrowserCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            buildMediaController()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            print("Fail to connect")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            print("Connection suspended")
        }
    }

    private fun buildMediaController() {
        mediaController = MediaController(requireContext(), mediaBrowser.sessionToken)
        mediaController.registerCallback(object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                super.onPlaybackStateChanged(state)
                print("Updated UI")
                print("Delegate to view model to call analytics")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowser(
            requireContext(),
            ComponentName(requireContext(), MyAudioService::class.java),
            mediaBrowserCallback,
            bundleOf()
        )

        mediaBrowser.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            play.setOnClickListener {
                mediaController.transportControls.play()
            }
            pause.setOnClickListener {
                mediaController.transportControls.pause()
            }
        }
    }

    override fun onDestroy() {
        mediaBrowser.disconnect()
        super.onDestroy()
    }
}