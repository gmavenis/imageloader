package com.kay.appdemo

import android.os.AsyncTask
import android.util.Log
import com.kay.appdemo.image.ImagesWorker
import org.json.JSONException
import org.json.JSONObject


class MainPresenter(private var view: MainContract.View?, private val jsonString: String) : MainContract.Presenter {

    private val task = ModelParsingTask { view?.display(it) }


    override fun start() {
        task.execute(jsonString)
    }

    override fun destroy() {
        view = null
        task.cancel(true)
    }





    class ModelParsingTask(val callback: (ResultModel) -> Unit) : AsyncTask<String, Void, ResultModel>() {
        override fun doInBackground(vararg params: String?): ResultModel {
            val jsonString = checkNotNull(params[0])
            return try {
                val rootObj = JSONObject(jsonString)
                val title = rootObj.getString("title")
                val imageUrls = rootObj.getJSONArray("image")
                val mutableList = mutableListOf<String>()
                for (i in 0 until imageUrls.length()) {
                    mutableList.add(imageUrls.getString(i))
                }
                ResultModel(title, mutableList.toList())
            } catch (e: JSONException) {
                Log.e("json error",e.message?:"")
                ResultModel("kay", emptyList())
            }
        }

        override fun onPostExecute(result: ResultModel?) {
            callback(result!!)
        }
    }

}