package org.knowledger.plugin

interface OptIn {
    var experimentalContracts: Boolean
    var experimentalOptIn: Boolean
    var requiresOptIn: Boolean
}