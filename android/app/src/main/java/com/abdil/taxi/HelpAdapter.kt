package com.abdil.taxi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HelpAdapter(
    private val onItemClick: (HelpItem) -> Unit
) : RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    private var items = listOf<HelpItem>()

    fun submitList(newItems: List<HelpItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val cardView: View = itemView.findViewById(R.id.cardHelp)

        fun bind(item: HelpItem, onItemClick: (HelpItem) -> Unit) {
            tvIcon.text = item.icon
            tvTitle.text = item.title
            tvCategory.text = item.category
            tvDescription.text = item.description.take(80) + "..."

            cardView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

data class HelpItem(
    val icon: String,
    val title: String,
    val description: String,
    val category: String
)