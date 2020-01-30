package com.openapp.openappsdk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import co.openapp.sdk.data.db.entity.Lock
import com.openapp.openappsdk.LockAdapter.LockViewHolder
import kotlinx.android.synthetic.main.lock_add_list_item.view.*

class LockAdapter(private val lockCallback: ((Lock) -> Unit)) : ListAdapter<Lock, LockViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        return LockViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.lock_add_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    inner class LockViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bindTo(lock: Lock) {
            itemView.tv_lock_name.text = lock.lockName
            itemView.setOnClickListener { lockCallback.invoke(lock) }
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Lock> = object : DiffUtil.ItemCallback<Lock>() {
            override fun areItemsTheSame(
                    oldLock: Lock, newLock: Lock): Boolean { // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldLock.id == newLock.id
            }

            override fun areContentsTheSame(
                    oldUser: Lock, newUser: Lock): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser == newUser
            }
        }
    }
}