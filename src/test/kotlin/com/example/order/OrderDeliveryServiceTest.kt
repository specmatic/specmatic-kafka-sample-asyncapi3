package com.example.order

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.kafka.support.Acknowledgment

class OrderDeliveryServiceTest {
    private val orderRepository = OrderRepository()
    private val taxServiceClient = mock(TaxServiceClient::class.java)
    private val acknowledgment = mock(Acknowledgment::class.java)
    private val service = OrderDeliveryService(orderRepository, taxServiceClient)

    @Test
    fun `raises invoice for consecutive delivery events with same orderId`() {
        `when`(taxServiceClient.raiseInvoice(eq(456), anyString())).thenReturn(
            TaxInvoiceResponse(invoiceId = "inv-456", status = "RAISED", orderId = 456)
        )

        val firstRecord = deliveryRecord(orderId = 456, deliveryDate = "2025-01-01")
        val secondRecord = deliveryRecord(orderId = 456, deliveryDate = "2025-01-02")

        service.orderDeliveryUpdates(firstRecord, acknowledgment)
        service.orderDeliveryUpdates(secondRecord, acknowledgment)

        verify(taxServiceClient, times(2)).raiseInvoice(eq(456), anyString())
        verify(acknowledgment, times(2)).acknowledge()
    }

    private fun deliveryRecord(orderId: Int, deliveryDate: String): ConsumerRecord<String, String> {
        return ConsumerRecord(
            "out-for-delivery-orders",
            0,
            0L,
            orderId.toString(),
            """{"orderId":$orderId,"deliveryAddress":"221B Baker Street","deliveryDate":"$deliveryDate"}"""
        )
    }
}
