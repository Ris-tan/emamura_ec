package emamura_ec.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Captures only the delivery-date choice made on the delivery confirmation page.
 * Address data has already been validated and is kept in checkoutDeliveryData.
 */
public class CheckoutDeliveryConfirmForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requestedDeliveryDate;

    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }
}
