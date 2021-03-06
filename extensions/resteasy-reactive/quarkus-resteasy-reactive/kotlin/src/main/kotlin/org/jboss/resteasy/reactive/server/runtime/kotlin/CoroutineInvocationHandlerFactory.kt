package org.jboss.resteasy.reactive.server.runtime.kotlin

import org.jboss.resteasy.reactive.server.spi.EndpointInvoker
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Factory for the [CoroutineInvocationHandler] that is already part of the CDI container
 */
@ApplicationScoped
class CoroutineInvocationHandlerFactory @Inject constructor(
        private val applicationCoroutineScope: ApplicationCoroutineScope
) {
    fun createHandler(invoker: EndpointInvoker): ServerRestHandler {
        return CoroutineInvocationHandler(invoker, applicationCoroutineScope)
    }
}