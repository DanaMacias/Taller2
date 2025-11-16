package com.example.taller2.data.firebase

import com.google.firebase.database.FirebaseDatabase

class FirebaseDataSource {

    private val database = FirebaseDatabase.getInstance(
        "https://proycompo2-default-rtdb.firebaseio.com/"
    )

    fun playersRef() = database.getReference("players")
}