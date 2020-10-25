package org.knowledger.plugin

import org.gradle.kotlin.dsl.DependencyHandlerScope


internal fun DependencyHandlerScope.implementation(dependencyNotation: String) {
    add("implementation", dependencyNotation)
}

internal fun DependencyHandlerScope.testImplementation(dependencyNotation: String) {
    add("testImplementation", dependencyNotation)
}

internal fun DependencyHandlerScope.testRuntimeOnly(dependencyNotation: String) {
    add("testRuntimeOnly", dependencyNotation)
}
