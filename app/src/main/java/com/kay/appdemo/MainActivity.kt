package com.kay.appdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.kay.appdemo.interfaces.ProgressUpdateListener
import com.kay.appdemo.image.*

class MainActivity : AppCompatActivity(), MainContract.View {


    private lateinit var titleView: TextView
    override lateinit var presenter: MainContract.Presenter
    private lateinit var imageHolder: CustomImageContainer

    var touchCount = 0;
    private var model: ResultModel? = null

    private val JSON_STRING =
        """
            {
                "title" : "Civil War",
                "image" : [
                    "http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg?type=m665_443_2",
                "http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2",
                "http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg?type=m665_443_2"
            ]
        }
        """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleView = findViewById(R.id.titleView)
        imageHolder = findViewById(R.id.imageHolder)
        presenter = MainPresenter(this, JSON_STRING).also {
            it.start()
        }


    }

    override fun display(res: ResultModel) {
        model = res
        titleView.text = model!!.name
        displayImage(model!!.urls[0])


        imageHolder.setOnClickListener {
            if (model != null) {
                touchCount++
                // infinite images
                val url = model!!.urls[touchCount % model!!.urls.size]
                displayImage(url)
            }
        }

    }

    private fun displayImage(url: String) {
        ImagesWorker.display(WeakImageHolder(imageHolder), url, object : ProgressUpdateListener {
            override fun updateProgress(imageUri: String, current: Int, total: Int) {
                imageHolder.updateState(ImageJobState.Downloading(current / 1024))
            }
        })
    }


    override fun onDestroy() {
        presenter.destroy()
        super.onDestroy()
    }

    fun clearCacheAndExit(view: View) {
        ImagesWorker.clearDiskCache()
        finish()
    }
}
