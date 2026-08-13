package com.dnoel.checklistv1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * `viewModelScope` dispatches to `Dispatchers.Main`, which only exists on a real
 * Android device — on a plain JVM it throws. This rule swaps in a test dispatcher
 * for the duration of each test and restores the original afterwards.
 *
 * `UnconfinedTestDispatcher` runs coroutines eagerly, so a call like
 * `viewModel.addList("x")` has already finished by the time the next line runs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
