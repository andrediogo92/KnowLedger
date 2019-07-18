package org.knowledger.example.logger

import org.koin.core.KoinApplication

fun KoinApplication.tinyLogger() {
    logger(KoinTinyLog())
}