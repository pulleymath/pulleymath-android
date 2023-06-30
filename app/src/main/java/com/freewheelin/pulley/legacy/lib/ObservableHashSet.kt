package com.freewheelin.pulley.legacy.lib

interface ObservableHashSetListener<E> {
    fun onItemChanged(set: ObservableHashSet<E>)
}


class ObservableHashSet<E>: LinkedHashSet<E>() {


    var listener: ObservableHashSetListener<E>? = null

    override fun add(element: E): Boolean {
        val result = super.add(element)
        listener?.onItemChanged(this)
        return result
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var result = false
        elements.forEach {
            val addResult = super.add(it)
            if(result == false)
                result = addResult
        }
        listener?.onItemChanged(this)
        return result
    }

    override fun clear() {
        super.clear()
        listener?.onItemChanged(this)
    }
    override fun remove(element: E): Boolean {
        val result = super.remove(element)
        listener?.onItemChanged(this)
        return result
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var result = false
        elements.forEach {
            val removeResult = super.remove(it)
            if(result == false)
                result = removeResult
        }

        listener?.onItemChanged(this)
        return result
    }

}
