package com.anwesh.uiprojects.trianglesidemoverview

/**
 * Created by anweshmishra on 07/08/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.*

val nodes : Int = 5

fun Float.triBisect() : PointF {
    return PointF(Math.min(0.5f, this) * 2, Math.min(0.5f, Math.max(0f, this - 0.5f)) * 2)
}

fun Canvas.drawTSMNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val hGap : Float = h / nodes
    val size : Float = hGap/3
    val point : PointF = scale.triBisect()
    val factor = 1 - 2 * (i % 2)
    save()
    translate((w/2) + (w / 2 + size) * factor * point.y, hGap * i + hGap/2)
    rotate(90f * factor * point.x)
    paint.color = Color.parseColor("#1565C0")
    val path : Path = Path()
    path.moveTo(-size/2, size/2)
    path.lineTo(size/2, size/2)
    path.lineTo(0f, -size/2)
    drawPath(path, paint)
    restore()
}

class TriangleSideMoverView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * this.dir
            if (Math.abs(this.scale - this.prevScale) > 1) {
                this.scale = this.prevScale + this.dir
                this.dir = 0f
                this.prevScale = this.scale
                cb(this.prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (this.dir == 0f) {
                this.dir = 1 - 2 * this.prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }
    }

    data class TSMNode(var i : Int, val state : State = State()) {

        private var prev : TSMNode? = null
        private var next : TSMNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TSMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTSMNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TSMNode {
            var curr : TSMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedTSM(var i : Int) {

        private var curr : TSMNode = TSMNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriangleSideMoverView) {

        val animator : Animator = Animator(view)
        val ltsm : LinkedTSM = LinkedTSM(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            ltsm.draw(canvas, paint)
            animator.animate {
                ltsm.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ltsm.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : TriangleSideMoverView {
            val view : TriangleSideMoverView = TriangleSideMoverView(activity)
            activity.setContentView(view)
            return view
        }
    }
}