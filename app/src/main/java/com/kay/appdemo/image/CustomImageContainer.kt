package com.kay.appdemo.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kay.appdemo.R

class CustomImageContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ImageHolder {

    lateinit var textView: TextView
    lateinit var imageView: ImageView

    private var isInflated = false

    init {
        orientation = VERTICAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        textView = findViewById(R.id.downloadedSize)
        imageView = findViewById(R.id.imagePhoto)
    }

    @SuppressLint("SetTextI18n")
    fun updateState(jobState: ImageJobState) {
        if (jobState is ImageJobState.JobDone) {
            imageView.setImageBitmap(jobState.bitmap)
            textView.visibility = View.INVISIBLE
        } else {
            textView.visibility = View.VISIBLE
        }
        when (jobState) {
            is ImageJobState.DownloadStarted -> textView.text = "Download Started"
            is ImageJobState.Downloading -> textView.text = "${jobState.kb} kb downloading"
            is ImageJobState.DownloadComplete, ImageJobState.Decoding -> textView.text = "Almost there..."
        }
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        updateState(ImageJobState.JobDone(bitmap))
    }

    override fun width(): Int {
        return if (isInflated) {
            imageView.layoutParams.width
        } else {
            300
        }
    }

    override fun height(): Int {
        return if (isInflated) {
            imageView.layoutParams.height
        } else {
            300
        }
    }

    override fun getIdentifier(): Int {
        return this.id
    }
}