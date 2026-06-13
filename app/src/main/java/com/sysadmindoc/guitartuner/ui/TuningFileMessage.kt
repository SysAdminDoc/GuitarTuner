package com.sysadmindoc.guitartuner.ui

sealed interface TuningFileMessage {
    data class Imported(val count: Int) : TuningFileMessage
    data class Exported(val count: Int) : TuningFileMessage
    data object NoCustomTunings : TuningFileMessage
    data class Error(val text: String) : TuningFileMessage
    data object ReadError : TuningFileMessage
    data object WriteError : TuningFileMessage
}
