package com.example.sikka_grphsfixed.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.sikka_grphsfixed.Card
import com.example.sikka_grphsfixed.Database
import com.example.sikka_grphsfixed.R
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class AnalysisFragment : Fragment(R.layout.analysis) {

    private lateinit var monthSelector: LinearLayout
    private lateinit var monthTextView: TextView
    private lateinit var graphsContainer: ConstraintLayout
    private val calendar = Calendar.getInstance()

    private var selectedChartType: String = "Pie chart"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chooseBtn = view.findViewById<Button>(R.id.chooseAnalysisBtn)
        monthSelector = view.findViewById(R.id.monthSelector)
        monthTextView = monthSelector.getChildAt(0) as TextView
        graphsContainer = view.findViewById(R.id.graphsContainer)

        updateMonthDisplay()

        monthSelector.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val width = v.width
                if (event.x < width / 2) moveToPreviousMonth()
                else moveToNextMonth()
            }
            true
        }

        chooseBtn.setOnClickListener { showAnalysisMenu(it) }

        // Add this line to show the pie chart immediately
        filterAndDisplayDataForSelectedMonth()
    }



    private fun updateMonthDisplay() {
        val formatter = SimpleDateFormat("MMMM,yyyy", Locale.getDefault())
        monthTextView.text = formatter.format(calendar.time)
    }

    private fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateMonthDisplay()
        filterAndDisplayDataForSelectedMonth()
    }

    private fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateMonthDisplay()
        filterAndDisplayDataForSelectedMonth()
    }

    private fun showAnalysisMenu(anchor: View) {
        val menu = PopupMenu(requireContext(), anchor)
        menu.menu.add("Pie chart")
        menu.menu.add("Bar chart")
        menu.menu.add("Expense FLow")
        menu.menu.add("Income FLow")

        menu.setOnMenuItemClickListener { item ->
            selectedChartType = item.title.toString()
            filterAndDisplayDataForSelectedMonth()
            true
        }

        menu.show()
    }

    private fun filterAndDisplayDataForSelectedMonth() {
        graphsContainer.removeAllViews()
        val filteredCards = filterCardsByMonth(Database.cardList)

        if (filteredCards.isEmpty()) {
            return
        }

        when (selectedChartType) {
            "Pie chart" -> showPieChart(filteredCards)
            "Bar chart" -> showBarChart(filteredCards)
            "Income FLow" -> showIncomeFlowChart(filteredCards)
            "Expense FLow" -> showExpenseFLowChart(filteredCards)
        }
    }

    private fun filterCardsByMonth(cards: List<Card>): List<Card> {
        val selectedText = monthTextView.text.toString()
        val formatter = SimpleDateFormat("MMMM,yyyy", Locale.getDefault())
        val selectedDate = formatter.parse(selectedText) ?: return emptyList()

        val selectedCalendar = Calendar.getInstance().apply { time = selectedDate }
        val selectedMonth = selectedCalendar.get(Calendar.MONTH)
        val selectedYear = selectedCalendar.get(Calendar.YEAR)

        return cards.filter { card ->
            val cardDate = try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(card.date)
            } catch (e: Exception) {
                null
            }
            cardDate?.let {
                val cardCalendar = Calendar.getInstance().apply { time = it }
                cardCalendar.get(Calendar.MONTH) == selectedMonth && cardCalendar.get(Calendar.YEAR) == selectedYear
            } ?: false
        }
    }


    private fun showPieChart(cards: List<Card>) {
        val pieChart = PieChart(requireContext())

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val categoryTotals = HashMap<String, Float>()
        val random = Random()

        for (card in cards) {
            val amount = card.amount?.replace("$", "")?.toFloatOrNull() ?: continue
            if (amount >= 0f) continue // Only process expenses (negative amounts)
            val label = card.category ?: "Unknown"
            categoryTotals[label] = categoryTotals.getOrDefault(label, 0f) + amount
        }

        for ((label, total) in categoryTotals) {
            entries.add(PieEntry(total, label))
            colors.add(Color.rgb(random.nextInt(200) + 30, random.nextInt(200) + 30, random.nextInt(200) + 30))
        }


        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        pieChart.data = data

        // Transparent settings
        pieChart.setBackgroundColor(Color.BLUE) // Chart background transparent
        pieChart.setHoleColor(Color.TRANSPARENT)       // Center hole transparent
        pieChart.setTransparentCircleColor(Color.TRANSPARENT) // Transparent circle color

        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "Expenses"
        pieChart.setCenterTextSize(20f)
        pieChart.animateY(1000)

        graphsContainer.addView(
            pieChart,
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        )
    }


    private fun showIncomeFlowChart(cards: List<Card>) {
        val lineChart = LineChart(requireContext())

        val entries = ArrayList<Entry>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (card in cards) {
            val date = try {
                dateFormat.parse(card.date)
            } catch (e: Exception) {
                null
            } ?: continue

            val calendar = Calendar.getInstance().apply { time = date }
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toFloat()
            val amount = card.amount?.replace("$", "")?.toFloatOrNull() ?: continue

            if (amount <= 0f) continue // Only include positive (income) amounts

            entries.add(Entry(dayOfMonth, amount))
        }

        if (entries.isEmpty()) return

        entries.sortBy { it.x }

        val dataSet = LineDataSet(entries, "Income Flow")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f

        val data = LineData(dataSet)

        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.animateX(1000)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        graphsContainer.addView(
            lineChart,
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        )
    }


    private fun showExpenseFLowChart(cards: List<Card>) {
        val lineChart = LineChart(requireContext())

        val entries = ArrayList<Entry>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (card in cards) {
            val date = try {
                dateFormat.parse(card.date)
            } catch (e: Exception) {
                null
            } ?: continue

            val calendar = Calendar.getInstance().apply { time = date }
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toFloat()
            val amount = card.amount?.replace("$", "")?.toFloatOrNull() ?: continue

            if (amount >= 0f) continue // Only negative positive (income) amounts

            entries.add(Entry(dayOfMonth, amount))
        }

        if (entries.isEmpty()) return

        entries.sortBy { it.x }

        val dataSet = LineDataSet(entries, "Income Flow")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f

        val data = LineData(dataSet)

        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.animateX(1000)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        graphsContainer.addView(
            lineChart,
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        )
    }


    private fun showBarChart(cards: List<Card>) {
        val barChart = BarChart(requireContext())

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val accTypeTotals = HashMap<String, Float>()

        for (card in cards) {
            val amount = card.amount?.replace("$", "")?.toFloatOrNull() ?: continue
            if (amount <= 0f) continue
            val accType = card.accType ?: "Unknown"
            accTypeTotals[accType] = accTypeTotals.getOrDefault(accType, 0f) + amount
        }

        var index = 0f
        for ((accType, total) in accTypeTotals) {
            entries.add(BarEntry(index, total))
            labels.add(accType)
            index++
        }

        if (entries.isEmpty()) return

        val dataSet = BarDataSet(entries, "Accounts")
        dataSet.color = Color.rgb(60, 130, 255)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f

        graphsContainer.addView(
            barChart,
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        )
    }
}
