package org.knowledger.agent.misc

import jade.lang.acl.MessageTemplate

infix fun MessageTemplate.and(
    template: MessageTemplate
): MessageTemplate =
    MessageTemplate.and(this, template)

fun MessageTemplate.not(): MessageTemplate =
    MessageTemplate.not(this)


infix fun MessageTemplate.or(
    template: MessageTemplate
): MessageTemplate =
    MessageTemplate.or(this, template)