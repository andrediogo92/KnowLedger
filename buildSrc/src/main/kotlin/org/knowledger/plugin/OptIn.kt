package org.knowledger.plugin

interface OptIn {
    val experimentalOptIn: Boolean
    val requiresOptIn: Boolean
}