/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.insetsanimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import com.google.android.samples.insetsanimation.databinding.FragmentConversationBinding

class ConversationFragment : Fragment() {
    private var _binding: FragmentConversationBinding? = null
    private val binding: FragmentConversationBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set our conversation adapter on the RecyclerView
        binding.conversationRecyclerview.adapter = ConversationAdapter()

        // There are three steps to WindowInsetsAnimations:

        /**
         * 1) Since our Activity has declared `window.setDecorFitsSystemWindows(false)`, we need to
         * handle any [WindowInsets] as appropriate.
         *
         * Our [RootViewDeferringInsetsCallback] will update our attached view's padding to match the
         * combination of the [WindowInsets.Type.systemBars], and selectively apply the
         * [WindowInsets.Type.ime] insets, depending on any ongoing WindowInsetAnimations
         * (see that class for more information).
         */
        val deferringInsetsListener = RootViewDeferringInsetsCallback(
            persistentInsetTypes = WindowInsets.Type.systemBars(),
            deferredInsetTypes = WindowInsets.Type.ime()
        )
        // RootViewDeferringInsetsCallback is both an WindowInsetsAnimation.Callback and an
        // OnApplyWindowInsetsListener, so needs to be set as so.
        binding.root.setWindowInsetsAnimationCallback(deferringInsetsListener)
        binding.root.setOnApplyWindowInsetsListener(deferringInsetsListener)

        /**
         * 2) The second step is reacting to any animations which run. This can be system driven,
         * such as the user focusing on an EditText and on-screen keyboard (IME) coming on screen,
         * or app driven (more on that in step 3).
         *
         * To react to animations, we set an [android.view.WindowInsetsAnimation.Callback] on any
         * views which we wish to react to inset animations. In this example, we want our
         * EditText holder view, and the conversation RecyclerView to react.
         *
         * We use our [TranslateDeferringInsetsAnimationCallback] class, bundled in this sample,
         * which will automatically move each view as the IME animates.
         *
         * Note about [TranslateDeferringInsetsAnimationCallback], it relies on the behavior of
         * [RootViewDeferringInsetsCallback] on the layout's root view.
         */
        binding.messageHolder.setWindowInsetsAnimationCallback(
            TranslateDeferringInsetsAnimationCallback(
                view = binding.messageHolder,
                persistentInsetTypes = WindowInsets.Type.systemBars(),
                deferredInsetTypes = WindowInsets.Type.ime()
            )
        )
        binding.conversationRecyclerview.setWindowInsetsAnimationCallback(
            TranslateDeferringInsetsAnimationCallback(
                view = binding.conversationRecyclerview,
                persistentInsetTypes = WindowInsets.Type.systemBars(),
                deferredInsetTypes = WindowInsets.Type.ime()
            )
        )

        /**
         * 3) The third step is when the app wants to control and drive an inset animation.
         * This is an optional step, but suits many types of input UIs. The example scenario we
         * use in this sample is that the user can drag open the IME, by over-scrolling the
         * conversation RecyclerView. To enable this, we use our
         * [InsetsAnimationOverscrollingTouchListener] class, which is a
         * [android.view.View.OnTouchListener] which handles this automatically.
         *
         * Internally [InsetsAnimationOverscrollingTouchListener] uses a bundled class
         * called [SimpleImeAnimationController], which simplifies much of the mechanics for
         * controlling the IME.
         */
        binding.conversationRecyclerview
            .setOnTouchListener(InsetsAnimationOverscrollingTouchListener())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
