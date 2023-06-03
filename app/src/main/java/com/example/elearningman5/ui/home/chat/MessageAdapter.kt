package com.example.elearningman5.ui.home.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.LocalStorage
import com.example.elearningman5.R

private const val TIME_MESSAGE = 0
private const val VIEW_TYPE_MY_MESSAGE = 1
private const val VIEW_TYPE_OTHER_MESSAGE = 2

// untuk onClickListener RecyclerView
interface ItemClickListener {
    fun onItemClick(position: Int)
}

class MessageAdapter (private val context: Context) : RecyclerView.Adapter<MessageViewHolder>() {
    private val messages: ArrayList<Message> = ArrayList()
    private var localStorage: LocalStorage = LocalStorage(context)
    private lateinit var itemClickListener: ItemClickListener

    @SuppressLint("NotifyDataSetChanged")
    fun addMessage(message: Message){
        messages.add(message)
    }

    fun setItemClickListener(listener: ItemClickListener) {
        itemClickListener = listener
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return when (message.email) {
            "timeMessage" -> {
                TIME_MESSAGE
            }
            localStorage.getEmail() -> {
                VIEW_TYPE_MY_MESSAGE
            }
            else -> {
                VIEW_TYPE_OTHER_MESSAGE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            TIME_MESSAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.time_message, parent, false)
                TimeMessage(view).apply {
                    setItemClickListener(itemClickListener) //untuk mengatur itemClickListener pada instance ViewHolder
                }
            }
            VIEW_TYPE_MY_MESSAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.my_message, parent, false)
                MyMessageViewHolder(view).apply {
                    setItemClickListener(itemClickListener) //untuk mengatur itemClickListener pada instance ViewHolder
                }
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.other_message, parent, false)
                OtherMessageViewHolder(view).apply {
                    setItemClickListener(itemClickListener)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    inner class TimeMessage (view: View) : MessageViewHolder(view) {
        private var txtTime: TextView = view.findViewById(R.id.txtTime)

        override fun bind(message: Message) {
            txtTime.text = message.time

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
    }

    inner class MyMessageViewHolder (view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.findViewById(R.id.txtMyMessage)
        private var timeText: TextView = view.findViewById(R.id.txtMyMessageTime)

        override fun bind(message: Message) {
            messageText.text = message.message.trimEnd('\r', '\n')
            timeText.text = message.time

//            onClickListener pada tampilan item
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
    }

    inner class OtherMessageViewHolder (view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.findViewById(R.id.txtOtherMessage)
        private var userText: TextView = view.findViewById(R.id.txtOtherUser)
        private var receiverRole: TextView = view.findViewById(R.id.txtReceiverRole)
        private var timeText: TextView = view.findViewById(R.id.txtOtherMessageTime)

        @SuppressLint("SetTextI18n")
        override fun bind(message: Message) {
            messageText.text = message.message.trimEnd('\r', '\n')
            userText.text = message.name
            receiverRole.text = "(${ message.receiver_role })"
            timeText.text = message.time

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
    }
}

open class MessageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message:Message) {}
}
