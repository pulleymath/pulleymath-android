package com.freewheelin.pulley.core

import com.freewheelin.pulley.model.Problem
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

object PieceEditEvents {

    val problemListChanged : BehaviorSubject<List<Problem>> = BehaviorSubject.create()

    val problemItemUp : PublishSubject<Int> = PublishSubject.create()
    val problemItemDown : PublishSubject<Int> = PublishSubject.create()
    val problemItemDelete : PublishSubject<Int> = PublishSubject.create()
    val similarButtonClicked : PublishSubject<Int> = PublishSubject.create()
    val problemListAdd : PublishSubject<Problem> = PublishSubject.create()
    val problemListReplace : PublishSubject<Problem> = PublishSubject.create()

}

object MainTabEvents{
    val mainTabIndexChanged : PublishSubject<Int> = PublishSubject.create()
}