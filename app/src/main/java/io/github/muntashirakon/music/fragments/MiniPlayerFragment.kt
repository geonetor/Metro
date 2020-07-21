package io.github.muntashirakon.music.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.animation.DecelerateInterpolator
import code.name.monkey.appthemehelper.ThemeStore
import io.github.muntashirakon.music.R
import io.github.muntashirakon.music.extensions.show
import io.github.muntashirakon.music.extensions.textColorPrimary
import io.github.muntashirakon.music.extensions.textColorSecondary
import io.github.muntashirakon.music.fragments.base.AbsMusicServiceFragment
import io.github.muntashirakon.music.helper.MusicPlayerRemote
import io.github.muntashirakon.music.helper.MusicProgressViewUpdateHelper
import io.github.muntashirakon.music.helper.PlayPauseButtonOnClickHandler
import io.github.muntashirakon.music.util.PreferenceUtil
import io.github.muntashirakon.music.util.RetroUtil
import io.github.muntashirakon.music.util.ViewUtil
import kotlinx.android.synthetic.main.fragment_mini_player.*
import kotlin.math.abs

open class MiniPlayerFragment : AbsMusicServiceFragment(), MusicProgressViewUpdateHelper.Callback,
    View.OnClickListener {

    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionNext -> MusicPlayerRemote.playNextSong()
            R.id.actionPrevious -> MusicPlayerRemote.back()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(FlingPlayBackController(requireContext()))
        setUpMiniPlayer()

        if (RetroUtil.isTablet()) {
            actionNext.show()
            actionPrevious.show()
            actionNext?.show()
            actionPrevious?.show()

        } else {
            actionNext.visibility = if (PreferenceUtil.isExtraControls) View.VISIBLE else View.GONE
            actionPrevious.visibility =
                if (PreferenceUtil.isExtraControls) View.VISIBLE else View.GONE
        }
        actionNext.setOnClickListener(this)
        actionPrevious.setOnClickListener(this)
        actionNext?.setOnClickListener(this)
        actionPrevious?.setOnClickListener(this)
    }

    private fun setUpMiniPlayer() {
        setUpPlayPauseButton()
        ViewUtil.setProgressDrawable(progressBar, ThemeStore.accentColor(requireContext()))
    }

    private fun setUpPlayPauseButton() {
        miniPlayerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updateSongTitle() {
        val builder = SpannableStringBuilder()

        val song = MusicPlayerRemote.currentSong
        val title = SpannableString(song.title)
        title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)

        val text = SpannableString(song.artistName)
        text.setSpan(ForegroundColorSpan(textColorSecondary()), 0, text.length, 0)

        builder.append(title).append(" • ").append(text)

        miniPlayerTitle.isSelected = true
        miniPlayerTitle.text = builder
    }

    override fun onServiceConnected() {
        updateSongTitle()
        updatePlayPauseDrawableState()
    }

    override fun onPlayingMetaChanged() {
        updateSongTitle()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        progressBar.max = total
        val animator = ObjectAnimator.ofInt(progressBar, "progress", progress)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    protected fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    class FlingPlayBackController(context: Context) : View.OnTouchListener {

        private var flingPlayBackController: GestureDetector

        init {
            flingPlayBackController = GestureDetector(context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(
                        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        if (abs(velocityX) > abs(velocityY)) {
                            if (velocityX < 0) {
                                MusicPlayerRemote.playNextSong()
                                return true
                            } else if (velocityX > 0) {
                                MusicPlayerRemote.playPreviousSong()
                                return true
                            }
                        }
                        return false
                    }
                })
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return flingPlayBackController.onTouchEvent(event)
        }
    }
}
