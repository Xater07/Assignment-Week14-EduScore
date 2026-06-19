package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.data.AppData

class StudentChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4A90D9.toInt() // Accent blue
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0F172A.toInt() // Dark navy text
        textSize = 36f
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4B5563.toInt() // Light text color for names
        textSize = 32f
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE5E7EB.toInt() // Light grey for grid lines
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Get Top 5 students based on average weighted marks
        val studentAverages = AppData.students.map { student ->
            val weightedMarks = AppData.getStudentCourseWeightedMarks(student.studentId)
            val avg = if (weightedMarks.isNotEmpty()) {
                weightedMarks.map { it.second }.average().toFloat()
            } else {
                0.0f
            }
            student to avg
        }.sortedByDescending { it.second }.take(5)

        if (studentAverages.isEmpty()) {
            canvas.drawText("No grade data available", width / 2f - 150f, height / 2f, textPaint)
            return
        }

        val paddingLeft = 180f
        val paddingRight = 100f
        val paddingTop = 60f
        val paddingBottom = 60f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw grid lines (0%, 25%, 50%, 75%, 100%)
        val gridLines = floatArrayOf(0f, 25f, 50f, 75f, 100f)
        for (line in gridLines) {
            val ratio = line / 100f
            val x = paddingLeft + (chartWidth * ratio)
            canvas.drawLine(x, paddingTop, x, height - paddingBottom, gridPaint)
            
            // Draw scale values
            canvas.drawText("${line.toInt()}%", x - 30f, height - paddingBottom + 40f, labelPaint)
        }

        // Draw horizontal bars for top 5 students
        val barCount = studentAverages.size
        val barSpacingHeight = chartHeight / barCount
        val barMaxHeight = barSpacingHeight * 0.6f

        studentAverages.forEachIndexed { index, (student, averageMark) ->
            val yOffset = paddingTop + (index * barSpacingHeight)
            val barTop = yOffset + (barSpacingHeight - barMaxHeight) / 2f
            val barBottom = barTop + barMaxHeight

            // Calculate width based on average score ratio
            val averageRatio = (averageMark.coerceIn(0f, 100f)) / 100f
            val barRight = paddingLeft + (chartWidth * averageRatio)

            // Draw student name on the left axis
            val displayName = if (student.studentName.length > 10) {
                student.studentName.substring(0, 8) + ".."
            } else {
                student.studentName
            }
            canvas.drawText(displayName, 20f, barTop + barMaxHeight / 2f + 12f, labelPaint)

            // Draw bar
            val rect = RectF(paddingLeft, barTop, barRight, barBottom)
            canvas.drawRoundRect(rect, dpToPx(), dpToPx(), barPaint)

            // Draw score value on the right or inside
            val scoreText = String.format("%.1f%%", averageMark)
            val textX = if (barRight > paddingLeft + 120f) barRight - 110f else barRight + 10f
            val scorePaint = if (barRight > paddingLeft + 120f) {
                Paint(textPaint).apply { color = 0xFFFFFFFF.toInt(); textSize = 28f }
            } else {
                Paint(textPaint).apply { color = 0xFF0F172A.toInt(); textSize = 28f }
            }
            canvas.drawText(scoreText, textX, barTop + barMaxHeight / 2f + 10f, scorePaint)
        }
    }

    private fun dpToPx(): Float {
        return 8f * resources.displayMetrics.density
    }
}
