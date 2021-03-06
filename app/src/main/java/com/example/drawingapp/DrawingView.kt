package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0F
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    /**
     * This method initializes the attributes of the ViewForDrawing class.
     */
    private fun setUpDrawing() {
        mPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mPaint!!.color = color
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /**
     * This method is called when a stroke is drawn on the canvas as a part of the painting.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mPaint!!.strokeWidth = path.brushThickness
            mPaint!!.color = path.color
            canvas.drawPath(path, mPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mPaint!!)
        }
    }

    /**
     * This method takes off last stroke from input
     */
    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    /**
     * This method acts as an event listener when a touch event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    /**
     * This method is called when user wants to change size for brush
     */
    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mPaint!!.strokeWidth = mBrushSize
    }

    /**
     * This method is called when user wants to change color for brush
     */
    fun setColor(newColor: Int) {
        color = newColor
        mPaint!!.color = color
    }

    /**
     * This method is called when user wants to get color of current brush
     */
    fun getColor(): Int {
        return mPaint!!.color
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}