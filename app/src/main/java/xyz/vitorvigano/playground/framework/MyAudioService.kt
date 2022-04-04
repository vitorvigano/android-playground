package xyz.vitorvigano.playground.framework

import android.app.*
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import xyz.vitorvigano.playground.R
import xyz.vitorvigano.playground.domain.AudioPlayer


class MyAudioService : MediaBrowserService() {

    private val audioPlayer: AudioPlayer = BitmovinPlayer()
    private val mediaSession by lazy {
        MediaSession(this, "MediaBrowserService")
    }

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() {
            super.onPlay()
            audioPlayer.play()
            mediaSession.setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, 0, 1F)
                    .setActions(PlaybackState.ACTION_PAUSE)
                    .build()
            )
        }

        override fun onPause() {
            super.onPause()
            audioPlayer.pause()
            mediaSession.setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PAUSED, 0, 1F)
                    .setActions(PlaybackState.ACTION_PLAY)
                    .build()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession.apply {
            setMetadata(getMediaMetadata())
            setPlaybackState(getInitialPlaybackState())
            setCallback(mediaSessionCallback)
        }

        sessionToken = mediaSession.sessionToken

        val playAction: Notification.Action =
            Notification.Action.Builder(
                R.drawable.ic_play,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY
                )
            ).build()

        val pauseAction: Notification.Action =
            Notification.Action.Builder(
                R.drawable.ic_pause,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PAUSE
                )
            ).build()

        val notification =
            getNotification(Notification.MediaStyle().setMediaSession(mediaSession.sessionToken))
        notification.actions = arrayOf(playAction, pauseAction)

        startForeground(1, notification)
    }

    private fun getInitialPlaybackState() = PlaybackState.Builder()
        .setState(PlaybackState.STATE_PAUSED, 0, 1F)
        // We need to define what actions we accept in this state.
        .setActions(PlaybackState.ACTION_PLAY)
        .build()

    private fun getMediaMetadata() = MediaMetadata.Builder()
        .putString(MediaMetadata.METADATA_KEY_TITLE, "Learn to Fly")
        .putString(MediaMetadata.METADATA_KEY_ARTIST, "Foo Fighters")
        .putLong(MediaMetadata.METADATA_KEY_DURATION, 50000L)
        .build()

    private fun getNotification(mediaStyle: Notification.MediaStyle) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            Notification.Builder(this, "xyz.vitorvigano.playground")
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_airplane)
                .build()
        } else {
            Notification.Builder(this)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_airplane)
                .setContentTitle("Learn to Fly")
                .setContentText("Foo Fighters")
                .build()
        }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(
                    "xyz.vitorvigano.playground",
                    "xyz.vitorvigano.playground",
                    importance
                ).apply {
                    description = "desc"
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession.release()
        // Release other dependencies
        stopSelf()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {

    }
}