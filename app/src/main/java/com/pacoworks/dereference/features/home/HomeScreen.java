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

package com.pacoworks.dereference.features.home;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxrelay.PublishRelay;
import com.pacoworks.dereference.features.global.BaseController;
import com.pacoworks.dereference.core.reactive.None;

import org.jetbrains.annotations.NotNull;

import rx.Observable;

public class HomeScreen extends BaseController implements HomeView {

    private final PublishRelay<None> mClicksPRelay = PublishRelay.create();

    public HomeScreen() {
        super();
        final HomeState state = new HomeState();
        HomeInteractorKt.bindHomeInteractor(this, state);
        HomeInteractorKt.subscribeHomeInteractor(this, state);
    }

    @NonNull
    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        final TextView textView = new TextView(container.getContext());
        textView.setText("slsgsagasfgasfgk\nfjghlkasjfhgkljashgaakljhskljghsk\nljghksjhg");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClicksPRelay.call(None.VOID);
            }
        });
        return textView;
    }

    @Override
    public void setTitle(@NonNull String title) {
        getActivity().setTitle(title);
    }

    @NotNull
    @Override
    public Observable<None> clicks() {
        return mClicksPRelay.asObservable();
    }
}
