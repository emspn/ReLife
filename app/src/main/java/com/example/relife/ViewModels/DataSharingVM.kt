package com.example.relife.ViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.relife.Model.User // Ensure this import is correct

class DataSharingVM : ViewModel() {
    val userLD = MutableLiveData<User>()

    fun setUser (user: User) {
        userLD.value = user
    }
}