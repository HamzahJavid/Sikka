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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

class AnalysisFragment : Fragment(R.layout.analysis) {

    private lateinit var monthSelector: LinearLayout
    private lateinit var monthTextView: TextView
    private lateinit var graphsContainer: ConstraintLayout
    private val calendar = Calendar.getInstance()

    private var selectedChartType: String = "Pie chart" // New variable

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
                val touchX = event.x
                if (touchX < width / 2) moveToPreviousMonth()
                else moveToNextMonth()
            }
            true
        }

        chooseBtn.setOnClickListener { v ->
            showAnalysisMenu(v)
        }
    }

    private fun updateMonthDisplay() {
        val dateFormat = SimpleDateFormat("MMMM,yyyy", Locale.getDefault())
        monthTextView.text = dateFormat.format(calendar.time)
    }

    private fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateMonthDisplay()
        filterAndDisplayDataForSelectedMonth() // Filter data when month changes
    }

    private fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateMonthDisplay()
        filterAndDisplayDataForSelectedMonth() // Filter data when month changes
    }

    private fun showAnalysisMenu(anchor: View) {
        val menu = PopupMenu(requireContext(), anchor)

        menu.menu.add("Pie chart")
        menu.menu.add("Bar chart")
        menu.menu.add("Income / Expense table")

        menu.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Pie chart" -> {
                    selectedChartType = "Pie chart"
                    showPieChart()
                }
                "Bar chart" -> {
                    selectedChartType = "Bar chart"
                    showBarChart()
                }
                "Income / Expense table" -> {
                    selectedChartType = "Income / Expense table"
                    showSummaryTable()
                }
            }
            true
        }

        menu.show()
    }

    private fun showPieChart() {
        graphsContainer.removeAllViews()

        val accountTypes = Database.cardList.mapNotNull { it.accType }.distinct()

        if (accountTypes.isEmpty()) {
            // If no account types available, don't display anything
            return
        }

        val typeSelector = Spinner(requireContext())
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, accountTypes)
        typeSelector.adapter = adapter

        typeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filteredCards = filterCardsByMonth(Database.cardList)
                if (filteredCards.isEmpty()) {
                    return // Don't display anything if no data available for the selected month
                }
                displayPieChart(filteredCards)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        graphsContainer.addView(typeSelector)
    }

    private fun filterCardsByMonth(cards: List<Card>): List<Card> {
        // Extract month and year from the selected monthTextView (format: "MMMM, yyyy")
        val selectedMonthText = monthTextView.text.toString()
        val selectedMonthDate = try {
            SimpleDateFormat("MMMM,yyyy", Locale.getDefault()).parse(selectedMonthText)
        } catch (e: Exception) {
            null
        }

        // If the selected month format is invalid, return empty list
        if (selectedMonthDate == null) return emptyList()

        val selectedMonth = Calendar.getInstance().apply { time = selectedMonthDate }.get(Calendar.MONTH)
        val selectedYear = Calendar.getInstance().apply { time = selectedMonthDate }.get(Calendar.YEAR)

        // Filter the cards based on the selected month and year
        return cards.filter { card ->
            val cardDate = try {
                // Parse card date in "dd/MM/yyyy" format
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(card.date)
            } catch (e: Exception) {
                null
            }

            // If card date is null, skip it
            cardDate?.let {
                val cardCalendar = Calendar.getInstance().apply { time = cardDate }
                val cardMonth = cardCalendar.get(Calendar.MONTH)
                val cardYear = cardCalendar.get(Calendar.YEAR)

                // Check if the card's month and year match the selected month and year
                cardMonth == selectedMonth && cardYear == selectedYear
            } ?: false
        }
    }


    private fun displayPieChart(cards: List<Card>) {
        if (cards.isEmpty()) {
            return // If no valid data, don't display anything
        }

        val pieChart = PieChart(requireContext())
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val random = Random()

        cards.forEach { card ->
            val rawAmount = card.amount?.replace("$", "")?.trim() ?: "0"
            val amount = rawAmount.toFloatOrNull() ?: 0f
            if (amount <= 0f) return@forEach

            val label = card.accType ?: "Unknown"
            entries.add(PieEntry(amount, label))

            val color = Color.rgb(
                random.nextInt(200) + 30,
                random.nextInt(200) + 30,
                random.nextInt(200) + 30
            )
            colors.add(color)
        }

        if (entries.isEmpty()) {
            return // If no valid data to display, don't show anything
        }

        val dataSet = PieDataSet(entries, "Savings")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "Accounts"
        pieChart.setCenterTextSize(20f)
        pieChart.animateY(1000)

        graphsContainer.addView(
            pieChart, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showBarChart() {
        graphsContainer.removeAllViews()
        // Do nothing if bar chart is not implemented
    }

    private fun showSummaryTable() {
        graphsContainer.removeAllViews()
        // Do nothing if summary table is not implemented
    }

    private fun filterAndDisplayDataForSelectedMonth() {
        // This method filters the cards based on the current selected month and displays them
        val filteredCards = filterCardsByMonth(Database.cardList)
        if (filteredCards.isEmpty()) {
            graphsContainer.removeAllViews()
        }

        when (selectedChartType) {
            "Pie chart" -> displayPieChart(filteredCards)
            "Bar chart" -> showBarChart()
            "Income / Expense table" -> showSummaryTable()
        }
    }
}
