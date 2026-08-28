package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.entity.DeliveryArea;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.repository.DeliveryAreaRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutDeliveryServiceTest {

    @Mock
    private DeliveryAreaRepository deliveryAreaRepository;

    @Mock
    private DeliveryArea deliveryArea;

    private CheckoutDeliveryService checkoutDeliveryService;

    private void setUpService() {
        checkoutDeliveryService = new CheckoutDeliveryService(deliveryAreaRepository);
    }

    private void stubShippingArea() {
        when(deliveryAreaRepository.findByPrefecture("石川県"))
                .thenReturn(Optional.of(deliveryArea));
        when(deliveryArea.getAvailable()).thenReturn(true);
        when(deliveryArea.getShippingFee()).thenReturn(500);
        when(deliveryArea.getLeadDays()).thenReturn(2);
    }

    @Test
    void leadDaysから最短お届け予定日を計算する() {
        setUpService();
        when(deliveryAreaRepository.findByPrefecture("石川県"))
                .thenReturn(Optional.of(deliveryArea));
        when(deliveryArea.getAvailable()).thenReturn(true);
        when(deliveryArea.getLeadDays()).thenReturn(2);
        assertEquals(
                LocalDate.now().plusDays(2),
                checkoutDeliveryService.calculateEarliestDeliveryDate("DELIVERY", "石川県"));
    }

    @Test
    void 最短日ちょうどのお届け希望日は受け付ける() {
        setUpService();
        stubShippingArea();
        CheckoutDeliveryData deliveryData = checkoutDeliveryService.validateAndCreate(createDeliveryForm(null));

        CheckoutDeliveryData data = checkoutDeliveryService.applyRequestedDeliveryDate(
                deliveryData,
                LocalDate.now().plusDays(2));

        assertEquals(LocalDate.now().plusDays(2), data.getRequestedDeliveryDate());
    }

    @Test
    void 最短日より前のお届け希望日は拒否する() {
        setUpService();
        stubShippingArea();
        CheckoutDeliveryData deliveryData = checkoutDeliveryService.validateAndCreate(createDeliveryForm(null));

        assertThrows(
                CheckoutDeliveryException.class,
                () -> checkoutDeliveryService.applyRequestedDeliveryDate(
                        deliveryData,
                        LocalDate.now().plusDays(1)));
    }

    @Test
    void 配送情報入力ではお届け希望日を保存しない() {
        setUpService();
        stubShippingArea();
        CheckoutDeliveryForm form = createDeliveryForm(LocalDate.now().plusDays(2));

        CheckoutDeliveryData data = checkoutDeliveryService.validateAndCreate(form);

        assertNull(data.getRequestedDeliveryDate());
    }

    @Test
    void お届け希望日を指定しない場合は受け付ける() {
        setUpService();
        stubShippingArea();
        CheckoutDeliveryData data = checkoutDeliveryService.validateAndCreate(createDeliveryForm(null));

        assertNull(data.getRequestedDeliveryDate());
    }

    @Test
    void 店頭受取ではお届け希望日を保存しない() {
        setUpService();
        CheckoutDeliveryForm form = createDeliveryForm(LocalDate.now().plusDays(2));
        form.setDeliveryOption("STORE_PICKUP");
        form.setPostalCode(null);
        form.setPrefecture(null);
        form.setCityAddress(null);
        form.setAddressDetail(null);
        form.setRequestedDeliveryDate(LocalDate.now().plusDays(2));

        CheckoutDeliveryData data = checkoutDeliveryService.validateAndCreate(form);

        assertEquals(DeliveryMethod.STORE_PICKUP, data.getDeliveryMethod());
        assertNull(data.getRequestedDeliveryDate());
    }

    private CheckoutDeliveryForm createDeliveryForm(LocalDate requestedDeliveryDate) {
        CheckoutDeliveryForm form = new CheckoutDeliveryForm();
        form.setDeliveryOption("DELIVERY");
        form.setRecipientName("受取人");
        form.setPhoneNumber("09011112222");
        form.setPostalCode("9200001");
        form.setPrefecture("石川県");
        form.setCityAddress("金沢市本町");
        form.setAddressDetail("1-1-1");
        form.setRequestedDeliveryDate(requestedDeliveryDate);
        return form;
    }
}
