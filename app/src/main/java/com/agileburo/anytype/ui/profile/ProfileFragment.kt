package com.agileburo.anytype.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.profile.ProfileViewModel
import com.agileburo.anytype.presentation.profile.ProfileViewModelFactory
import com.agileburo.anytype.ui.base.ViewStateFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayInputStream
import javax.inject.Inject

class ProfileFragment : ViewStateFragment<ViewState<ProfileView>>(R.layout.fragment_profile) {

    @Inject
    lateinit var factory: ProfileViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(ProfileViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(this, this)
        vm.navigation.observe(this, navObserver)
        vm.onViewCreated()

        vm.image.observe(this, Observer { blob ->

            val stream = ByteArrayInputStream(blob)

            Glide
                .with(this)
                .load(BitmapFactory.decodeStream(stream))
                .centerInside()
                .circleCrop()
                .into(pic)

            stream.close()
        })

    }

    override fun render(state: ViewState<ProfileView>) {
        when (state) {
            is ViewState.Init -> {
                logoutButton.setOnClickListener { vm.onLogoutClicked() }
                updateToggle.setOnCheckedChangeListener { _, isChecked ->
                    vm.onUpdateToggled(value = isChecked)
                }
                invitesToggle.setOnCheckedChangeListener { _, isChecked ->
                    vm.onInviteToggled(value = isChecked)
                }
                pinCodeText.setOnClickListener { vm.onPinCodeClicked() }
                keychainPhrase.setOnClickListener { vm.onKeyChainPhraseClicked() }
                backButton.setOnClickListener { vm.onBackButtonClicked() }
                switchProfileButton.setOnClickListener { vm.onAddProfileClicked() }
            }
            is ViewState.Success -> {
                name.text = state.data.name
            }
        }
    }

    override fun injectDependencies() {
        componentManager().profileComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().profileComponent.release()
    }
}