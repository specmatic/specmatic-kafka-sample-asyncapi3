package com.example.order

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration

@Component
class OrderStatusClient(
    private val webClient: WebClient,
    @param:Value($$"${order-status-service.base-url}") private val baseUrl: String,
    @param:Value($$"${order-status-service.timeout-seconds:3}") private val timeoutSeconds: Long,
    @param:Value($$"${order-status-service.retry-attempts:1}") private val retryAttempts: Long
) {
    fun fetchStatus(orderId: Int): OrderStatus {
        val path = "$baseUrl/orders/$orderId/status"
        println("[OrderStatusClient] Fetching order status for orderId: $orderId from $path")

        try {
            val response: OrderStatusResponse = webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono<OrderStatusResponse>()
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retry(retryAttempts)
                .block() ?: throw IllegalStateException("Empty response for order $orderId")

            println("[OrderStatusClient] Successfully fetched status '${response.status}' for orderId: $orderId")
            return OrderStatus.valueOf(response.status)
        } catch (e: IllegalArgumentException) {
            println("[OrderStatusClient] Unknown status value for order $orderId")
            e.printStackTrace()
            throw IllegalStateException("Unknown status for order $orderId", e)
        } catch (e: Exception) {
            println("[OrderStatusClient] Failed to fetch status for order $orderId")
            e.printStackTrace()
            throw e
        }
    }
}

data class OrderStatusResponse(
    val status: String
)
