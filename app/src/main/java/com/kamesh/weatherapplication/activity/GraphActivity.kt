package com.kamesh.weatherapplication.activity

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidplot.ui.HorizontalPositioning
import com.androidplot.ui.VerticalPositioning
import com.androidplot.util.PixelUtils
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.CatmullRomInterpolator
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import com.kamesh.weatherapplication.R
import java.text.DecimalFormat

class GraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val temperatureData = intent.getDoubleArrayExtra("temperatureData")
        if (temperatureData != null) {
            displayGraph(temperatureData)
        }
    }

    private fun displayGraph(temperatureData: DoubleArray) {
        val plot = findViewById<XYPlot>(R.id.plot)
        // Create a series and add data points
        val series = SimpleXYSeries("Temperature Data")
        val timeInterval = 1 // Assuming time interval between readings is 1 hour
        for (i in temperatureData.indices) {
            val time = i * timeInterval
            series.addLast(time.toDouble(), temperatureData[i])
        }

        // Smooth the curve using Catmull-Rom interpolation
        val lineAndPointFormatter = LineAndPointFormatter()
        lineAndPointFormatter.interpolationParams = CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal)
        plot.addSeries(series, lineAndPointFormatter)

        // Customize plot appearance
        plot.setDomainLabel("Time (hours)")
        plot.setRangeLabel("Temperature (°C)")

        // Set range boundaries to ensure proper scaling
        plot.setRangeBoundaries(temperatureData.min()!!, temperatureData.max()!!, BoundaryMode.FIXED)

        // Set some padding around the plot area
        plot.graph.setMargins(50f, 50f, 50f, 50f)

        // Set label formatting for the axes with increased font size
        val labelTextSizePx = PixelUtils.dpToPix(2400f) // Change the font size as desired
        val textStyle = Paint()
        textStyle.textSize = labelTextSizePx
        textStyle.typeface = Typeface.DEFAULT_BOLD // Optional: Change to another typeface if needed


        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("#.#")
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("#.#")

        val legend = plot.legend
        legend.isVisible = true
        legend.position(
            PixelUtils.dpToPix(5000f),
            HorizontalPositioning.ABSOLUTE_FROM_CENTER,
            PixelUtils.dpToPix(2000f),
            VerticalPositioning.ABSOLUTE_FROM_CENTER
        )
        // Set click listener to finish activity when plot is clicked
        plot.setOnClickListener {
            finish()
        }
    }


}


