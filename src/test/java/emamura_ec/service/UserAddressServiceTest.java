package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import emamura_ec.entity.User;
import emamura_ec.entity.UserAddress;
import emamura_ec.exception.UserAddressException;
import emamura_ec.form.UserAddressForm;
import emamura_ec.repository.UserAddressRepository;
import emamura_ec.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    private UserAddressService userAddressService;
    private User user;

    @BeforeEach
    void setUp() {
        userAddressService = new UserAddressService(userRepository, userAddressRepository);
        user = new User("Test User", "user@example.com", "hashed", "09000000000", null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void listReturnsOnlyTheCurrentUsersAddressesAsViews() {
        UserAddress address = address("東京都", "新宿区1-1", true);
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of(address));

        var views = userAddressService.findAll(user.getEmail());

        assertEquals(1, views.size());
        assertEquals("新宿区1-1", views.get(0).getAddressLine());
        assertTrue(views.get(0).isDefaultAddress());
    }

    @Test
    void firstAddressBecomesDefaultEvenWhenTheFormDoesNotRequestIt() {
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of());

        userAddressService.create(user.getEmail(), form(false));

        var captor = org.mockito.ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressRepository).save(captor.capture());
        assertTrue(captor.getValue().isDefaultAddress());
    }

    @Test
    void creatingAnotherDefaultClearsThePreviousDefault() {
        UserAddress first = address("東京都", "新宿区1-1", true);
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of(first));

        userAddressService.create(user.getEmail(), form(true));

        assertFalse(first.isDefaultAddress());
        var captor = org.mockito.ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressRepository).save(captor.capture());
        assertTrue(captor.getValue().isDefaultAddress());
    }

    @Test
    void ownedAddressCanBeUpdated() {
        UserAddress address = address("東京都", "新宿区1-1", true);
        when(userAddressRepository.findByUserAddressIdAndUser(1L, user)).thenReturn(Optional.of(address));
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of(address));

        userAddressService.update(user.getEmail(), 1L, form(false));

        assertEquals("大阪府", address.getPrefecture());
        assertEquals("大阪市2-2", address.getAddressLine());
        assertTrue(address.isDefaultAddress());
    }

    @Test
    void deletingDefaultAddressPromotesTheNextAddress() {
        UserAddress first = address("東京", "新宿区1-1", true);
        UserAddress second = address("大阪", "大阪市1-2", false);
        when(userAddressRepository.findByUserAddressIdAndUser(1L, user)).thenReturn(Optional.of(first));
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of(first, second));

        userAddressService.delete(user.getEmail(), 1L);

        verify(userAddressRepository).delete(first);
        assertTrue(second.isDefaultAddress());
    }

    @Test
    void settingDefaultAddressClearsTheOtherAddress() {
        UserAddress first = address("東京", "新宿区1-1", true);
        UserAddress second = address("大阪", "大阪市1-2", false);
        when(userAddressRepository.findByUserAddressIdAndUser(2L, user)).thenReturn(Optional.of(second));
        when(userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user))
                .thenReturn(List.of(first, second));

        userAddressService.setDefault(user.getEmail(), 2L);

        assertFalse(first.isDefaultAddress());
        assertTrue(second.isDefaultAddress());
    }

    @Test
    void anotherUsersAddressCannotBeReadUpdatedDeletedOrMadeDefault() {
        when(userAddressRepository.findByUserAddressIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(UserAddressException.class, () -> userAddressService.findForm(user.getEmail(), 99L));
        assertThrows(UserAddressException.class, () -> userAddressService.update(user.getEmail(), 99L, form(false)));
        assertThrows(UserAddressException.class, () -> userAddressService.delete(user.getEmail(), 99L));
        assertThrows(UserAddressException.class, () -> userAddressService.setDefault(user.getEmail(), 99L));
    }

    private UserAddress address(String prefecture, String addressLine, boolean defaultAddress) {
        return new UserAddress(
                user,
                "受取人",
                "09011112222",
                "100-0001",
                prefecture,
                addressLine,
                defaultAddress);
    }

    private UserAddressForm form(boolean defaultAddress) {
        UserAddressForm form = new UserAddressForm();
        form.setRecipientName("更新後の受取人");
        form.setPhoneNumber("09022223333");
        form.setPostalCode("530-0001");
        form.setPrefecture("大阪府");
        form.setAddressLine("大阪市2-2");
        form.setDefaultAddress(defaultAddress);
        return form;
    }
}
