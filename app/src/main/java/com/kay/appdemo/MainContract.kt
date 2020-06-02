package com.kay.appdemo

import com.kay.appdemo.base.BasePresenter
import com.kay.appdemo.base.BaseView

interface MainContract {

    interface Presenter : BasePresenter {
    }

    interface View : BaseView<Presenter> {
        fun display(model: ResultModel)
    }
}