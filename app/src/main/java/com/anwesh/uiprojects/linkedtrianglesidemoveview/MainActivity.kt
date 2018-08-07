package com.anwesh.uiprojects.linkedtrianglesidemoveview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.trianglesidemoverview.TriangleSideMoverView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TriangleSideMoverView.create(this)
    }
}
