package com.abcengineering.invoiceapp.service;


import com.abcengineering.invoiceapp.dto.CancelPaymentRequest;
import com.abcengineering.invoiceapp.dto.CreatePaymentRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.PaymentNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.repository.InvoiceRepository;
import com.abcengineering.invoiceapp.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(InvoiceRepository invoiceRepository,
                          PaymentRepository paymentRepository) {

        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }


    /**
     * Create a payment.
     *
     * @param paymentRequest the details of payment to be created
     * @return the created {@link Payment} entity
     * @throws InvoiceNotFoundException if no invoice exists for the invoice ID supplied in paymentRequest
     */
    public Payment createPayment(CreatePaymentRequest paymentRequest) {

        Invoice invoice = invoiceRepository.findById(paymentRequest.getInvoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(paymentRequest.getInvoiceId()));

        logger.info("Attempting to create a payment for invoice {}", invoice.getId());

        Payment payment = new Payment(invoice, paymentRequest.getPaymentDate(),
                paymentRequest.getPaymentAmount(), paymentRequest.getPaymentMethod(),
                paymentRequest.getPaymentReference());

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment successfully created with ID {}", savedPayment.getId());
        return savedPayment;
    }


    /**
     * Gets payment details.
     *
     * @param paymentId the ID of the payment to get
     * @return the {@link Payment} entity associated with the payment ID
     * @throws PaymentNotFoundException if no payment exists with the supplied ID
     */
    public Payment getPayment(Integer paymentId) {

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }


    /**
     * Cancels a payment.
     *
     * @param paymentId the ID of the payment to cancel
     * @return {@code true} if the payment is successfully cancelled in this operation;
     *         {@code false} if the payment was already cancelled
     * @throws PaymentNotFoundException if no payment exists with the supplied ID
     */
    public boolean cancelPayment(Integer paymentId, CancelPaymentRequest cancellationRequest) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        logger.info("Attempting to cancel payment {}", paymentId);

        if (payment.getCancellationDateTime() != null) {
            logger.warn("Payment {} already cancelled", paymentId);
            return false;
        }

        payment.setCancellationDateTime(LocalDateTime.now());
        payment.setCancellationReason(cancellationRequest.getCancellationReason());
        paymentRepository.save(payment);

        logger.info("Successfully cancelled payment {}", paymentId);

        return true;
    }


    /**
     * Gets details of all non-cancelled payments associated with the invoice.
     *
     * @param invoiceId the ID of the invoice for which payments are to be retrieved
     * @return a list of all non-cancelled payments, or an empty list if none exist
     */
    public List<Payment> getNonCancelledPaymentsForInvoice(Integer invoiceId) {
        return paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoiceId);
    }
}
