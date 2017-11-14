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
package com.pacoworks.dereference.architecture.reactive.buddies

import com.jakewharton.rxrelay2.BehaviorRelay
import com.pacoworks.dereference.architecture.reactive.ActivityLifecycle
import com.pacoworks.dereference.architecture.reactive.ActivityResult
import com.pacoworks.dereference.architecture.reactive.PermissionResult
import io.reactivex.BackpressureStrategy

/**
 * Delegate class for Android lifecycle responsibilities in an Activity to transform them on reactive streams.
 *
 * It wraps the lifecycle in a more comprehensible approach for this app.
 */
class ReactiveActivity {
  val lifecycleRelay: BehaviorRelay<ActivityLifecycle> = BehaviorRelay.create<ActivityLifecycle>()
  val activityResultRelay: BehaviorRelay<ActivityResult> = BehaviorRelay.create<ActivityResult>()
  val permissionResultRelay: BehaviorRelay<PermissionResult> = BehaviorRelay.create<PermissionResult>()
  val onBackRelay: BehaviorRelay<Unit> = BehaviorRelay.create<Unit>()

  /**
   * To be called on the first time an Activity is created
   */
  fun onEnter() =
      call(ActivityLifecycle.ENTER)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onCreate() =
      call(ActivityLifecycle.CREATE)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onStart() =
      call(ActivityLifecycle.START)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onResume() =
      call(ActivityLifecycle.RESUME)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onPause() =
      call(ActivityLifecycle.PAUSE)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onStop() =
      call(ActivityLifecycle.STOP)

  /**
   * To be called on the lifecycle method of the same name
   */
  fun onDestroy() =
      call(ActivityLifecycle.DESTROY)

  /**
   * To be called when an Activity is finished by a business request
   */
  fun onExit() =
      call(ActivityLifecycle.EXIT)

  /**
   * To be called after receiving a result from another Activity
   */
  fun onActivityResult(result: ActivityResult) =
      activityResultRelay.accept(result)

  /**
   * To be called after receiving a result of a permission request
   */
  fun onPermissionResult(result: PermissionResult) =
      permissionResultRelay.accept(result)

  /**
   * To be called when the user presses the back key
   */
  fun onBackPressed() =
      onBackRelay.accept(Unit)

  /**
   * Creates a proxy object [ActivityReactiveBuddy] to access framework events, like lifecycle.
   *
   * @return a new [ActivityReactiveBuddy]
   */
  fun createBuddy() = object : ActivityReactiveBuddy {
    override fun lifecycle() =
        lifecycleRelay.toFlowable(BackpressureStrategy.LATEST).toObservable()

    override fun activityResult() =
        activityResultRelay.toFlowable(BackpressureStrategy.LATEST).toObservable()

    override fun permissionResult() =
        permissionResultRelay.toFlowable(BackpressureStrategy.LATEST).toObservable()

    override fun back() =
        onBackRelay.toFlowable(BackpressureStrategy.LATEST).toObservable()
  }

  private fun call(lifecycle: ActivityLifecycle) =
      lifecycleRelay.accept(lifecycle)
}
