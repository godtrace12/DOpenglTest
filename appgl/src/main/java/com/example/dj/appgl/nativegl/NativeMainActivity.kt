package com.example.dj.appgl.nativegl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dj.appgl.R

class NativeMainActivity : AppCompatActivity(),View.OnClickListener {
    private var btnBasicTriangle:Button? = null
    private var btnTransformFeedback:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_main)
        findViews()
    }

    private fun findViews() {
        btnBasicTriangle = findViewById(R.id.btnBasicTriangle)
        btnBasicTriangle!!.setOnClickListener(this)
        btnTransformFeedback = findViewById(R.id.btnTransformFeedback)
        btnTransformFeedback!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        var vieId = v!!.id
        if (vieId == R.id.btnBasicTriangle){
            onStart<NativeGLActivity>(Pair<String,Int>(NativeGLDrawType.NativeGLDraw_Type,NativeGLDrawType.NativeGLDraw_BasicTriangle))
        }else if (vieId == R.id.btnTransformFeedback){
            onStart<NativeGLActivity>(Pair<String,Int>(NativeGLDrawType.NativeGLDraw_Type,NativeGLDrawType.NativeGLDraw_TransformFeedback))
        }
    }


    private inline fun <reified T : Activity> Context.onStart(vararg pair: Pair<String, Int>?) {
        val mIntent = Intent(this, T::class.java)
        pair?.let {
            pair.forEach {
                mIntent.putExtra(it!!.first, it!!.second)
            }
        }
        startActivity(mIntent)
    }


}