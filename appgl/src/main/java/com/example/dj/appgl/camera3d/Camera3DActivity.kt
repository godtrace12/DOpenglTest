package com.example.dj.appgl.camera3d

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.dj.appgl.R
import com.example.dj.appgl.camera.CameraGLSurface
import com.example.dj.appgl.camera.`object`.TrianCamColorRender

class Camera3DActivity: AppCompatActivity() {
    private val TAG = "CameraActivity"
    private var glSurface: Camera3DGLSurface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera3d)
        glSurface = findViewById(R.id.glSurface)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(1, 1, 1, "颜色三角形")
        menu.add(1, 2, 2, "纹理三角形")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e(TAG, "onOptionsItemSelected: itemId=" + item.itemId)
        when (item.itemId) {
            1 -> glSurface!!.setObjectRender(TrianCamColorRender())
//            2 -> glSurface!!.setObjectRender(TrianCamTextureRender())
        }
        return super.onOptionsItemSelected(item)
    }

}