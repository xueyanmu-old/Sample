package com.poatek.sample.ui.base

data class AlertDialogContent(
    val title: String,
    val message: String,
    val retryCallback: (() -> Unit)? = null
)