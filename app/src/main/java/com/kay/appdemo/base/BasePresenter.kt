package com.kay.appdemo.base

interface BasePresenter{
    /**
     * Start an initial action
     */
    fun start()

    /**
     * destroy this presenter's resources
     */
    fun destroy()
}