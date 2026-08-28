package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.entity.DeliveryArea;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.User;
import emamura_ec.entity.UserAddress;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.repository.DeliveryAreaRepository;

@ExtendWith(MockitoExtension.class)
class SavedAddressCheckoutTest {

    @Mock
    private DeliveryAreaRepository deliveryAreaRepository;

    @Mock
    private DeliveryArea deliveryArea;

    @Mock
    private UserAddressService userAddressService;

    @Test
    void selectedSavedAddressIsCopiedToCheckoutDataAfterOwnerLookup() {
        User user = new User("User", "user@example.com", "hashed", "09000000000", null);
        UserAddress address = new UserAddress(
                user,
                "保存先の受取人",
                "09011112222",
                "100-0001",
                "東京都",
                "千代田区1-1",
                true);
        when(userAddressService.findOwnedAddress("user@example.com", 1L)).thenReturn(Optional.of(address));
        when(deliveryAreaRepository.findByPrefecture("東京都")).thenReturn(Optional.of(deliveryArea));
        when(deliveryArea.getAvailable()).thenReturn(true);
        when(deliveryArea.getShippingFee()).thenReturn(500);
        when(deliveryArea.getLeadDays()).thenReturn(2);

        CheckoutDeliveryData data = service().validateAndCreate(formWithSavedAddress(), "user@example.com");

        assertEquals(DeliveryMethod.SHIPPING, data.getDeliveryMethod());
        assertEquals("保存先の受取人", data.getRecipientName());
        assertEquals("09011112222", data.getPhoneNumber());
        assertEquals("100-0001", data.getPostalCode());
        assertEquals("東京都", data.getPrefecture());
        assertEquals("千代田区1-1", data.getAddressLine());
    }

    @Test
    void addressIdThatDoesNotBelongToTheCurrentUserIsRejected() {
        when(userAddressService.findOwnedAddress("user@example.com", 99L)).thenReturn(Optional.empty());

        assertThrows(
                CheckoutDeliveryException.class,
                () -> service().validateAndCreate(formWithSavedAddress(99L), "user@example.com"));
    }

    private CheckoutDeliveryService service() {
        return new CheckoutDeliveryService(deliveryAreaRepository, userAddressService);
    }

    private CheckoutDeliveryForm formWithSavedAddress() {
        return formWithSavedAddress(1L);
    }

    private CheckoutDeliveryForm formWithSavedAddress(Long addressId) {
        CheckoutDeliveryForm form = new CheckoutDeliveryForm();
        form.setDeliveryOption("DELIVERY");
        form.setSavedAddressId(addressId);
        return form;
    }
}
