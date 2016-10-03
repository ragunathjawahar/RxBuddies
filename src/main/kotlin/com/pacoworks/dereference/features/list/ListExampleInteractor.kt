/*
 * Copyright (c) pakoito 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pacoworks.dereference.features.list

import com.pacoworks.dereference.architecture.ui.StateHolder
import com.pacoworks.dereference.core.functional.None
import com.pacoworks.dereference.features.list.model.EditMode
import com.pacoworks.dereference.features.list.model.createEditModeDelete
import com.pacoworks.dereference.features.list.model.createEditModeNormal
import com.pacoworks.rxcomprehensions.RxComprehensions.doFM
import com.pacoworks.rxcomprehensions.RxComprehensions.doSM
import com.pacoworks.rxtuples.RxTuples
import org.javatuples.Pair
import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*

fun bindListExample(viewInput: ListExampleInputView, state: ListExampleState) {
    viewInput.createBinder<List<String>>().call(state.elements, viewInput::updateElements)
    viewInput.createBinder<Set<String>>().call(state.selected, viewInput::updateSelected)
    viewInput.createBinder<EditMode>().call(state.editMode, viewInput::updateEditMode)
}

fun subscribeListExampleInteractor(viewOutput: ListExampleOutputView, state: ListExampleState): Subscription =
        CompositeSubscription(
                handleDragAndDrop(state, viewOutput),
                handleAdd(state.elements, viewOutput.addClick()),
                handleEditModes(state, viewOutput.listLongClicks(), viewOutput.deleteClick(), viewOutput.listClicks()))

private fun handleDragAndDrop(state: ListExampleState, viewOutput: ListExampleOutputView): Subscription =
        doFM(
                { viewOutput.dragAndDropMoves() },
                { swap ->
                    state.elements
                            .first()
                            .map {
                                it.toMutableList()
                                        .apply { Collections.swap(this, swap.value0, swap.value1) }
                                        .toList()
                            }
                }
        ).subscribe(state.elements)

fun handleAdd(elementsState: StateHolder<List<String>>, addClick: Observable<None>): Subscription =
        doSM(
                { elementsState },
                { addClick.first() },
                { elements, click ->
                    /* Insert random number at end of the list */
                    Observable.just(elements.plus((Random().nextInt() + elements.size).toString()))
                }
        )
                .subscribe(elementsState)

fun handleEditModes(state: ListExampleState, listLongClicks: Observable<Pair<Int, String>>, deleteClick: Observable<None>, listClicks: Observable<Pair<Int, String>>): Subscription =
        CompositeSubscription(
                handleEnterEditState(listLongClicks, state.editMode),
                handleExitEditState(deleteClick, state.editMode),
                handleOnCommitDelete(state.editMode, state.elements, state.selected),
                handleOnSwitchEditState(state.editMode, state.selected),
                handleSelect(state.editMode, state.selected, listClicks))

fun handleEnterEditState(listLongClicks: Observable<Pair<Int, String>>, editMode: StateHolder<EditMode>): Subscription =
        listLongClicks
                .flatMap { click ->
                    editMode.first()
                            .filter { it.join({ normal -> true }, { delete -> false }) }
                            .map { createEditModeDelete(click.value1) }
                }.subscribe(editMode)

fun handleExitEditState(deleteClick: Observable<None>, editMode: StateHolder<EditMode>): Subscription =
        deleteClick
                .flatMap {
                    editMode.first()
                            .filter { it.join({ normal -> false }, { delete -> true }) }
                            .map { createEditModeNormal() }
                }.subscribe(editMode)

fun handleOnSwitchEditState(editMode: StateHolder<EditMode>, selected: StateHolder<Set<String>>): Subscription =
        editMode
                .map { it.join({ setOf<String>() }, { setOf(it.id) }) }
                .subscribe(selected)

fun handleOnCommitDelete(editMode: StateHolder<EditMode>, elementsState: StateHolder<List<String>>, selected: StateHolder<Set<String>>): Subscription =
        doSM(
                { editMode.filter { it.join({ normal -> true }, { delete -> false }) } },
                { Observable.zip(elementsState, selected, RxTuples.toPair<List<String>, Set<String>>()).first() },
                { exitEditMode, statePair ->
                    Observable.from(statePair.value0).filter { !statePair.value1.contains(it) }.toList()
                }
        )
                .subscribe(elementsState)

fun handleSelect(editMode: StateHolder<EditMode>, selected: StateHolder<Set<String>>, listClicks: Observable<Pair<Int, String>>): Subscription =
        editMode.
                switchMap {
                    if (it.join({ normal -> false }, { delete -> true })) {
                        listClicks.map { it.value1 }
                    } else {
                        Observable.empty()
                    }
                }.flatMap { value ->
            selected.first().map {
                if (it.contains(value)) {
                    it.minus(value)
                } else {
                    it.plus(value)
                }
            }
        }.subscribe(selected)