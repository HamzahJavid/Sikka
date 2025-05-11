package com.example.sikka_grphsfixed

import com.example.sikka_grphsfixed.adapters.CardAdapter
import com.google.firebase.database.*

object Database {

    val cardList = mutableListOf<Card>()

    fun loadRecords(uid: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Records")

        // Query Firebase to filter records based on 'user' (uid)
        databaseRef.orderByChild("user").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear the current card list
                    cardList.clear()

                    // Iterate through the snapshot and add matching records to cardList
                    for (recordSnapshot in snapshot.children) {
                        val record = recordSnapshot.getValue(Card::class.java)
                        record?.let {
                            cardList.add(it)  // Add record to cardList

                            val dum = CardAdapter.FinanceCard(amount = it.amount,
                                title = it.category ?: "",
                                icon = it.icon,
                                account = it.accType ?: "")

                            DummyDatabase.cardList.add(dum)
                        }
                    }

                    // Optionally, update the UI (e.g., RecyclerView) after loading the records
                    // updateRecyclerView(cardList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error (e.g., show a Toast message)
                    // Toast.makeText(context, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}


