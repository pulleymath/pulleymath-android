package com.freewheelin.pulley.lib

import com.freewheelin.pulley.legacy.lib.ObservableHashSet
import com.freewheelin.pulley.legacy.lib.ObservableHashSetListener
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

class ObservableHashSetTest {
    @Test
    fun `add, clear, remove, addAll 등이 불릴때, listener가 호출되어야한다`() {
        val set = ObservableHashSet<Int>()


        val dummyListener = mock(DummySetListener::class.java)
        set.listener = dummyListener

        set.add(0)
        Mockito.verify(dummyListener, times(1)).onItemChanged(set)

        set.remove(0)
        Mockito.verify(dummyListener, times(2)).onItemChanged(set)

        set.remove(999)
        Mockito.verify(dummyListener, times(3)).onItemChanged(set)

        set.clear()
        Mockito.verify(dummyListener, times(4)).onItemChanged(set)

        set.addAll(listOf(0,1,2,3))
        Mockito.verify(dummyListener, times(5)).onItemChanged(set)
    }


}

open class DummySetListener: ObservableHashSetListener<Int> {
    override fun onItemChanged(set: ObservableHashSet<Int>) {}


}
