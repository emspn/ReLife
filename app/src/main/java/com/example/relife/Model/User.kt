package com.example.relife.Model

data class User (
    val userId: String = "",
    val name: String = ""
) {
    companion object {
        fun also(function: () -> Unit) {

        }
    }
}