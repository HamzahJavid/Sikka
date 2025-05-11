package com.example.sikka_grphsfixed.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sikka_grphsfixed.Database
import com.example.sikka_grphsfixed.R
import java.io.File
import java.io.FileWriter
import java.util.Calendar

class ExportRecordFragment : Fragment() {

    private lateinit var fromButton: Button
    private lateinit var toButton: Button
    private lateinit var exportButton: Button
    private var fromDate: String? = null
    private var toDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        fromButton = view.findViewById(R.id.from)
        toButton = view.findViewById(R.id.to)
        exportButton = view.findViewById(R.id.exportBtn)

        // Setup listeners
        setupListeners()
    }

    private fun setupListeners() {
        fromButton.setOnClickListener {
            showDatePicker { date ->
                fromDate = date
                fromButton.text = "From: $date"
            }
        }

        toButton.setOnClickListener {
            showDatePicker { date ->
                toDate = date
                toButton.text = "To: $date"
            }
        }

        exportButton.setOnClickListener {
            exportToCSV()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun exportToCSV() {
        try {
            val exportDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (exportDir != null && !exportDir.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, "records.csv")
            val writer = FileWriter(file)

            // Write CSV header
            writer.append("Amount,Date,Notes,Account Type,Category,Type,Icon\n")

            // Write records
            for (card in Database.cardList) {
                writer.append("${card.amount},${card.date},${card.notes},${card.accType},${card.category},${card.type},${card.icon}\n")
            }

            writer.flush()
            writer.close()

            Toast.makeText(requireContext(), "Records exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
