package com.agileburo.anytype.presentation.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.common.EventWrapper

interface SupportCommand<Command> {
    val commands: MutableLiveData<EventWrapper<Command>>
    fun receive(): LiveData<EventWrapper<Command>> = commands
    fun dispatch(command: Command) {
        commands.postValue(EventWrapper(command))
    }
}