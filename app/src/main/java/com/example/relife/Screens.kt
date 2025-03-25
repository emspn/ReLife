package com.example.relife

sealed class Screens (val screen: String){


    data object Home:Screens("Home")
    data object Map:Screens("Map")
    data object Chat:Screens("Chat")
    data object Profile:Screens("Profile")

    data object SignIn : Screens("SignIn")

    data object Person : Screens("com.example.relife.Person")
    data object LocationInfo : Screens("LocationInfo")

}