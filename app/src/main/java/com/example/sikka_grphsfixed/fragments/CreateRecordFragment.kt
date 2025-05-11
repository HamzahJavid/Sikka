package com.example.sikka_grphsfixed.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sikka_grphsfixed.Database
import com.example.sikka_grphsfixed.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CalculatorFragment : Fragment() {

    // UI elements
    private lateinit var time: TextView
    private lateinit var date: TextView
    private lateinit var display: TextView
    private lateinit var notes: TextView
    private lateinit var createBtn: Button
    private lateinit var accountBtn: Button
    private lateinit var categoryBtn: Button

    // State variables
    private var selectedAccountType = "Cash" // default account type
    private var selectedCategory = "Food" // default category
    private var expression = ""
    private var recordType = "Expense" // default type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString("UserID")
        val mydb = FirebaseDatabase.getInstance()

        Log.d("UserID_Debug", "UserID: ${arguments?.getString("UserID")}")

        // Initialize UI components
        initializeUI(view)

        // Set current date and time
        setCurrentDateTime()

        // Set up numeric and operator buttons
        setupNumericButtons(view)
        setupOperatorButtons(view)

        // Create card on 'Create' button click
        createBtn.setOnClickListener {
            createRecord(uid, mydb)
        }

        val toggle = view.findViewById<Switch>(R.id.toggleExpenseIncome)

        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggle.text = "Income"
                recordType = "Income"
            } else {
                toggle.text = "Expense"
                recordType = "Expense"
            }
        }
    }

    private fun initializeUI(view: View) {
        categoryBtn = view.findViewById(R.id.category)
        categoryBtn.setOnClickListener { showCategoryMenu(it) }

        accountBtn = view.findViewById(R.id.account)
        accountBtn.setOnClickListener { showAccountMenu(it) }

        display = view.findViewById(R.id.tvDisplay)
        time = view.findViewById(R.id.date_cr)
        date = view.findViewById(R.id.time_cr)
        notes = view.findViewById(R.id.Notes)
        createBtn = view.findViewById(R.id.create)
    }

    private fun setCurrentDateTime() {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        date.text = currentDate
        time.text = currentTime
    }

    private fun createRecord(uid: String?, mydb: FirebaseDatabase) {
        val keyRef = mydb.getReference("Records").push()

        val record = mapOf(
            "user" to uid,
            "amount" to display.text.toString(),
            "date" to date.text.toString(),
            "notes" to notes.text.toString(),
            "accType" to selectedAccountType,
            "category" to selectedCategory,
            "type" to recordType,
            "icon" to getCategoryIconResource(selectedCategory)
        )

        keyRef.setValue(record)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Record added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add record", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNumericButtons(view: View) {
        val numericButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
            R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
            R.id.btn_8, R.id.btn_9
        )

        for (id in numericButtons) {
            view.findViewById<Button>(id).setOnClickListener {
                appendToExpression((it as Button).text.toString())
            }
        }
    }

    private fun setupOperatorButtons(view: View) {
        val operators = mapOf(
            R.id.btn_add to "+",
            R.id.btn_subtract to "-",
            R.id.btn_multiply to "*",
            R.id.btn_divide to "/"
        )

        for ((id, op) in operators) {
            view.findViewById<Button>(id).setOnClickListener {
                appendToExpression(op)
            }
        }

        view.findViewById<Button>(R.id.btn_dot).setOnClickListener {
            appendToExpression(".")
        }

        view.findViewById<Button>(R.id.btn_equals).setOnClickListener {
            evaluateExpression()
        }
    }

    private fun appendToExpression(str: String) {
        expression += str
        display.text = expression
    }

    private fun evaluateExpression() {
        try {
            val tokens = tokenize(expression)
            val result = evaluateTokens(tokens)
            display.text = result.toString()
            expression = result.toString()
        } catch (e: Exception) {
            display.text = "Error"
            expression = ""
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var number = ""
        for (char in expr) {
            if (char.isDigit() || char == '.') {
                number += char
            } else if (char in "+-*/") {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                tokens.add(char.toString())
            }
        }
        if (number.isNotEmpty()) {
            tokens.add(number)
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<String>): Double {
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token in "+-*/") {
                operators.add(token)
            } else {
                numbers.add(token.toDouble())
            }
            i++
        }

        var index = 0
        while (index < operators.size) {
            if (operators[index] == "*" || operators[index] == "/") {
                val left = numbers[index]
                val right = numbers[index + 1]
                val result = if (operators[index] == "*") left * right else left / right
                numbers[index] = result
                numbers.removeAt(index + 1)
                operators.removeAt(index)
            } else {
                index++
            }
        }

        var result = numbers[0]
        for (j in operators.indices) {
            val op = operators[j]
            val num = numbers[j + 1]
            result = if (op == "+") result + num else result - num
        }

        return result
    }

    private fun showAccountMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.account_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cash_option -> {
                    selectedAccountType = "Cash"
                    accountBtn.text = "Cash"
                    true
                }
                R.id.savings_option -> {
                    selectedAccountType = "Savings"
                    accountBtn.text = "Savings"
                    true
                }
                R.id.card_option -> {
                    selectedAccountType = "Card"
                    accountBtn.text = "Card"
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showCategoryMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.category_menu, popup.menu)

        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if (field.name == "mPopup") {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                    setForceIcons.isAccessible = true
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { item ->
            selectedCategory = item.title.toString()
            categoryBtn.text = item.title
            true
        }

        popup.show()
    }

    private fun getCategoryIconResource(category: String): Int? {
        return when (category) {
            "Food" -> R.drawable.food
            "Transport" -> R.drawable.transport
            "Entertainment" -> R.drawable.entertainment
            "Bills" -> R.drawable.utilities
            else -> R.drawable.food
        }
    }
}
