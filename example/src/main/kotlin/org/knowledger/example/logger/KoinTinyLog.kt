package org.knowledger.example.logger

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class KoinTinyLog : Logger() {
    override fun log(level: Level, msg: MESSAGE) =
        when (level) {
            Level.DEBUG -> org.tinylog.kotlin.Logger.debug(msg)
            Level.INFO -> org.tinylog.kotlin.Logger.info(msg)
            Level.ERROR -> org.tinylog.kotlin.Logger.error(msg)
        }
}