package com.example.sikka_grphsfixed.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sikka_grphsfixed.R

class CardAdapter(
    val items: MutableList<FinanceCard>,
    val onItemClick: (FinanceCard, Int) -> Unit // <-- Pass both card and position
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    data class FinanceCard(
        var title: String,
        var account: String,
        var amount: String,
        var icon: Int? = R.drawable.food // Default icon
    )

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.category)
        val paymentType: TextView = itemView.findViewById(R.id.paymentType)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val icon: ImageView = itemView.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.record_card_default, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.category.text = item.title
        holder.paymentType.text = item.account
        holder.amount.text = item.amount
        //holder.icon.setImageResource(item.icon)
        //holder.icon.setImageResource(item.icon ?: R.drawable.card)

        // Set the click listener to pass both card and its position
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(mutableList: MutableList<FinanceCard>) {
        items.clear()
        items.addAll(mutableList)
        notifyDataSetChanged()
    }

}
