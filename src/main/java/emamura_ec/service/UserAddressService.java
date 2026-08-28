package emamura_ec.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import emamura_ec.dto.UserAddressView;
import emamura_ec.entity.User;
import emamura_ec.entity.UserAddress;
import emamura_ec.exception.UserAddressException;
import emamura_ec.form.UserAddressForm;
import emamura_ec.repository.UserAddressRepository;
import emamura_ec.repository.UserRepository;

@Service
public class UserAddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public UserAddressService(
            UserRepository userRepository,
            UserAddressRepository userAddressRepository) {
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAddressView> findAll(String loginEmail) {
        User user = findUser(loginEmail);
        return userAddressRepository
                .findByUserOrderByDefaultAddressDescUserAddressIdAsc(user)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAddressForm findForm(String loginEmail, Long addressId) {
        return toForm(findOwnedAddressOrThrow(loginEmail, addressId));
    }

    /**
     * Resolves an address together with its owner. Callers must not use an ID-only
     * lookup for account data, because a guessed ID could otherwise expose or
     * modify another user's address.
     */
    @Transactional(readOnly = true)
    public Optional<UserAddress> findOwnedAddress(String loginEmail, Long addressId) {
        if (!StringUtils.hasText(loginEmail) || addressId == null) {
            return Optional.empty();
        }

        return userRepository.findByEmail(loginEmail)
                .flatMap(user -> userAddressRepository.findByUserAddressIdAndUser(addressId, user));
    }

    @Transactional
    public void create(String loginEmail, UserAddressForm form) {
        User user = findUser(loginEmail);
        List<UserAddress> addresses = findOwnedEntities(user);
        boolean makeDefault = addresses.isEmpty()
                || form.isDefaultAddress()
                || addresses.stream().noneMatch(UserAddress::isDefaultAddress);

        if (makeDefault) {
            clearDefault(addresses);
        }

        UserAddress address = new UserAddress(
                user,
                trim(form.getRecipientName()),
                trim(form.getPhoneNumber()),
                trim(form.getPostalCode()),
                trim(form.getPrefecture()),
                trim(form.getAddressLine()),
                makeDefault);
        userAddressRepository.save(address);
    }

    @Transactional
    public void update(String loginEmail, Long addressId, UserAddressForm form) {
        UserAddress address = findOwnedAddressOrThrow(loginEmail, addressId);
        List<UserAddress> addresses = findOwnedEntities(address.getUser());
        boolean makeDefault = form.isDefaultAddress()
                || address.isDefaultAddress()
                || addresses.stream().noneMatch(UserAddress::isDefaultAddress);

        if (makeDefault) {
            clearDefault(addresses);
        }

        // The entity is managed inside this transaction, so these changes are persisted by dirty checking.
        address.setRecipientName(trim(form.getRecipientName()));
        address.setPhoneNumber(trim(form.getPhoneNumber()));
        address.setPostalCode(trim(form.getPostalCode()));
        address.setPrefecture(trim(form.getPrefecture()));
        address.setAddressLine(trim(form.getAddressLine()));
        address.setDefaultAddress(makeDefault);
    }

    @Transactional
    public void delete(String loginEmail, Long addressId) {
        UserAddress address = findOwnedAddressOrThrow(loginEmail, addressId);
        List<UserAddress> addresses = findOwnedEntities(address.getUser());
        boolean wasDefault = address.isDefaultAddress();

        userAddressRepository.delete(address);

        // Keep a usable default after deleting it. OrderAddress is a separate historical snapshot and is untouched.
        if (wasDefault || addresses.stream().noneMatch(item -> item != address && item.isDefaultAddress())) {
            addresses.stream()
                    .filter(item -> item != address)
                    .findFirst()
                    .ifPresent(item -> item.setDefaultAddress(true));
        }
    }

    @Transactional
    public void setDefault(String loginEmail, Long addressId) {
        UserAddress selectedAddress = findOwnedAddressOrThrow(loginEmail, addressId);
        List<UserAddress> addresses = findOwnedEntities(selectedAddress.getUser());

        // Updating every address in one transaction keeps the one-default-per-user rule consistent.
        clearDefault(addresses);
        selectedAddress.setDefaultAddress(true);
    }

    private UserAddress findOwnedAddressOrThrow(String loginEmail, Long addressId) {
        return findOwnedAddress(loginEmail, addressId)
                .orElseThrow(() -> new UserAddressException("指定されたお届け先が見つかりません。"));
    }

    private User findUser(String loginEmail) {
        return userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new UserAddressException("ログインユーザーが見つかりません。"));
    }

    private List<UserAddress> findOwnedEntities(User user) {
        return userAddressRepository.findByUserOrderByDefaultAddressDescUserAddressIdAsc(user);
    }

    private void clearDefault(List<UserAddress> addresses) {
        addresses.forEach(address -> address.setDefaultAddress(false));
    }

    private UserAddressView toView(UserAddress address) {
        return new UserAddressView(
                address.getUserAddressId(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getPostalCode(),
                address.getPrefecture(),
                address.getAddressLine(),
                address.isDefaultAddress());
    }

    private UserAddressForm toForm(UserAddress address) {
        UserAddressForm form = new UserAddressForm();
        form.setRecipientName(address.getRecipientName());
        form.setPhoneNumber(address.getPhoneNumber());
        form.setPostalCode(address.getPostalCode());
        form.setPrefecture(address.getPrefecture());
        form.setAddressLine(address.getAddressLine());
        form.setDefaultAddress(address.isDefaultAddress());
        return form;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
