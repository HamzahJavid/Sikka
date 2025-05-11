package com.example.sikka_grphsfixed.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sikka_grphsfixed.Database
import com.example.sikka_grphsfixed.Database.loadRecords
import com.example.sikka_grphsfixed.DummyDatabase
import com.example.sikka_grphsfixed.R
import com.example.sikka_grphsfixed.adapters.CardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter

    private lateinit var income: TextView
    private lateinit var expense: TextView
    private lateinit var total: TextView

    private var currentMonth: Int = 0
    private var currentYear: Int = 0
    private lateinit var monthTextView: TextView
    private lateinit var monthSelector: LinearLayout

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.records, container, false)

        // Initialize UI components
        income = view.findViewById(R.id.income)
        expense = view.findViewById(R.id.expense)
        total = view.findViewById(R.id.total)

        recyclerView = view.findViewById(R.id.cardRecyclerView)
        monthTextView = view.findViewById(R.id.month)
        monthSelector = view.findViewById(R.id.monthSelector)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize date-related variables
        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)

        updateMonthDisplay()

        // Set the CardAdapter with item click handling
        cardAdapter = CardAdapter(loadCurrentMonthRecords()) { card, position ->
            showCardDetailsPopup(card, position) // Pass both card and position
        }

        recyclerView.adapter = cardAdapter
        cardAdapter.updateData(loadCurrentMonthRecords())

        // Handle month selection
        monthSelector.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val width = v.width
                val clickedX = event.x

                if (clickedX < width / 2) {
                    goToPreviousMonth()
                } else {
                    goToNextMonth()
                }
                cardAdapter.updateData(loadCurrentMonthRecords())
            }
            true
        }

        // Set up the Add button functionality
        val addBtn = view.findViewById<FloatingActionButton>(R.id.addButton)
        addBtn.setOnClickListener {
            val uid = arguments?.getString("UserID")
            navigateToCalculatorFragment(uid)
        }

        return view
    }

    private fun updateIncomeExpenseTotals(records: List<CardAdapter.FinanceCard>) {
        var i = 0.0
        var e = 0.0

        for (card in records) {
            val amount = card.amount?.toDoubleOrNull() ?: 0.0
            if (amount < 0) {
                e += amount
            } else {
                i += amount
            }
        }

        income.text = "Rs${i.toString()}"
        expense.text = "Rs${e.toString()}"
        total.text =  "Rs${(i + e).toString()}"
    }

    @SuppressLint("SetTextI18n")
    private fun updateMonthDisplay() {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        monthTextView.text = "${monthNames[currentMonth]}, ${currentYear}"
    }

    private fun goToNextMonth() {
        if (currentMonth == 11) {
            currentMonth = 0
            currentYear++
        } else {
            currentMonth++
        }
        updateMonthDisplay()
    }

    private fun goToPreviousMonth() {
        if (currentMonth == 0) {
            currentMonth = 11
            currentYear--
        } else {
            currentMonth--
        }
        updateMonthDisplay()
    }

    private fun loadCurrentMonthRecords(): MutableList<CardAdapter.FinanceCard> {
        loadRecords(arguments?.getString("UserID").toString())
        val currentMonthRecords = mutableListOf<CardAdapter.FinanceCard>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for ((i, card) in Database.cardList.withIndex()) {
            val date = sdf.parse(card?.date)
            val cardCalendar = Calendar.getInstance()
            cardCalendar.time = date!!

            if (cardCalendar.get(Calendar.MONTH) == currentMonth && cardCalendar.get(Calendar.YEAR) == currentYear) {
                currentMonthRecords.add(DummyDatabase.cardList[i])
            }
        }

        updateIncomeExpenseTotals(currentMonthRecords)
        return currentMonthRecords
    }

    private fun showCardDetailsPopup(card: CardAdapter.FinanceCard, position: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_records, null)

        val amountText = dialogView.findViewById<TextView>(R.id.amountText)
        val dateText = dialogView.findViewById<TextView>(R.id.dateText)
        val accountText = dialogView.findViewById<TextView>(R.id.accountText)
        val categoryText = dialogView.findViewById<TextView>(R.id.categoryText)
        val notesText = dialogView.findViewById<TextView>(R.id.notesText)
        val accountIcon = dialogView.findViewById<ImageView>(R.id.accountIcon)
        val categoryIcon = dialogView.findViewById<ImageView>(R.id.categoryIcon)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        // Update details based on clicked card
        amountText.text = Database.cardList[position].amount
        dateText.text = Database.cardList[position].date
        accountText.text = Database.cardList[position].accType
        categoryText.text = Database.cardList[position].category
        notesText.text = Database.cardList[position].notes

        accountIcon.setImageResource(getAccount(Database.cardList[position].accType.toString()) ?: R.drawable.card)
        categoryIcon.setImageResource(card.icon ?: R.drawable.utilities)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()

        deleteButton.setOnClickListener {
            if (position in Database.cardList.indices) {
                Database.cardList.removeAt(position)
            }
            dialog.dismiss()
            // Reload view
            refreshCardList()
        }
    }

    private fun refreshCardList() {
        cardAdapter.updateData(loadCurrentMonthRecords())
    }

    private fun navigateToCalculatorFragment(uid: String?) {
        val calculatorFragment = CalculatorFragment()
        val bundle = Bundle().apply {
            putString("UserID", uid)
        }
        calculatorFragment.arguments = bundle

        requireActivity().findViewById<View>(R.id.bottom_nav).visibility = View.GONE
        requireActivity().findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, calculatorFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().supportFragmentManager.addOnBackStackChangedListener {
            val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
            val fragmentContainer = requireActivity().findViewById<View>(R.id.fragment_container)
            val viewPager = requireActivity().findViewById<View>(R.id.view_pager)

            if (requireActivity().supportFragmentManager.backStackEntryCount == 0) {
                bottomNav.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE
                fragmentContainer.visibility = View.GONE
            }
        }
    }

    private fun getAccount(account: String): Int? {
        return when (account) {
            "Cash" -> R.drawable.cash
            "Savings" -> R.drawable.savings
            "Card" -> R.drawable.card
            else -> R.drawable.card
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().supportFragmentManager.removeOnBackStackChangedListener {}
    }
}
