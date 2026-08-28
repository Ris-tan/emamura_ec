package emamura_ec.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.entity.DeliveryArea;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.UserAddress;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryConfirmForm;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.repository.DeliveryAreaRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutDeliveryService {

    private static final String CHECKOUT_DELIVERY_SESSION_ATTRIBUTE = "checkoutDeliveryData";
    private static final String DELIVERY_OPTION_DELIVERY = "DELIVERY";
    private static final String DELIVERY_OPTION_STORE_PICKUP = "STORE_PICKUP";
    private static final DateTimeFormatter DELIVERY_DATE_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("M月d日");

    private final DeliveryAreaRepository deliveryAreaRepository;
    private final UserAddressService userAddressService;

    public CheckoutDeliveryService(DeliveryAreaRepository deliveryAreaRepository) {
        this(deliveryAreaRepository, null);
    }

    @Autowired
    public CheckoutDeliveryService(
            DeliveryAreaRepository deliveryAreaRepository,
            UserAddressService userAddressService) {
        this.deliveryAreaRepository = deliveryAreaRepository;
        this.userAddressService = userAddressService;
    }

    public CheckoutDeliveryData validateAndCreate(CheckoutDeliveryForm form) {
        return validateAndCreate(form, null);
    }

    public CheckoutDeliveryData validateAndCreate(CheckoutDeliveryForm form, String loginEmail) {
        DeliveryMethod deliveryMethod = resolveDeliveryMethod(form.getDeliveryOption());
        // Saved addresses are only meaningful for delivery; store pickup still uses the pickup recipient fields.
        UserAddress savedAddress = deliveryMethod == DeliveryMethod.STORE_PICKUP
                ? null
                : resolveSavedAddress(form, loginEmail);
        String recipientName = trimToNull(savedAddress == null
                ? form.getRecipientName()
                : savedAddress.getRecipientName());
        String phoneNumber = trimToNull(savedAddress == null
                ? form.getPhoneNumber()
                : savedAddress.getPhoneNumber());
        String postalCode = trimToNull(savedAddress == null
                ? form.getPostalCode()
                : savedAddress.getPostalCode());
        String prefecture = trimToNull(savedAddress == null
                ? form.getPrefecture()
                : savedAddress.getPrefecture());
        String cityAddress = trimToNull(savedAddress == null
                ? form.getCityAddress()
                : savedAddress.getAddressLine());
        String addressDetail = savedAddress == null ? trimToNull(form.getAddressDetail()) : null;
        String addressLine = combineAddress(cityAddress, addressDetail);

        // Zipcloud only assists the UI; all submitted values remain untrusted and are validated on the server.
        validateRecipientInformation(recipientName, phoneNumber);
        if (deliveryMethod != DeliveryMethod.STORE_PICKUP) {
            if (savedAddress == null) {
                validateAddressInformation(postalCode, prefecture, cityAddress, addressDetail);
            } else {
                validateSavedAddressInformation(postalCode, prefecture, cityAddress);
            }
        }
        validateAddressLineLength(addressLine);

        Integer shippingFee = null;
        Integer leadDays = null;

        if (deliveryMethod == DeliveryMethod.SHIPPING) {
            DeliveryArea area = findAvailableArea(prefecture);
            shippingFee = area.getShippingFee();
            leadDays = area.getLeadDays();
            // leadDays is intentionally a simple calendar-day estimate; holidays and business days are out of scope.
        }

        return new CheckoutDeliveryData(
                deliveryMethod,
                recipientName,
                phoneNumber,
                postalCode,
                prefecture,
                cityAddress,
                addressDetail,
                shippingFee,
                leadDays,
                null,
                savedAddress == null ? null : form.getSavedAddressId());
    }

    private UserAddress resolveSavedAddress(CheckoutDeliveryForm form, String loginEmail) {
        if (form.getSavedAddressId() == null) {
            return null;
        }
        if (userAddressService == null || !StringUtils.hasText(loginEmail)) {
            throw new CheckoutDeliveryException("保存済みのお届け先を利用するにはログインが必要です。");
        }

        return userAddressService.findOwnedAddress(loginEmail, form.getSavedAddressId())
                .orElseThrow(() -> new CheckoutDeliveryException("指定されたお届け先を利用できません。"));
    }

    /**
     * Revalidates the delivery date from Session against the current delivery
     * area data. This is also called while rebuilding the order snapshot so a
     * stale confirmation page cannot bypass the minimum-date rule.
     */
    public LocalDate validateStoredDeliveryData(CheckoutDeliveryData data) {
        if (data == null || data.getDeliveryMethod() == null) {
            throw new CheckoutDeliveryException("受取方法を確認してください。");
        }

        if (data.getDeliveryMethod() != DeliveryMethod.SHIPPING) {
            if (data.getRequestedDeliveryDate() != null) {
                throw new CheckoutDeliveryException("店頭受取ではお届け希望日を指定できません。");
            }
            return null;
        }

        DeliveryArea area = findAvailableArea(data.getPrefecture());
        LocalDate earliestDeliveryDate = calculateEarliestDeliveryDate(area.getLeadDays());
        validateRequestedDeliveryDate(data.getRequestedDeliveryDate(), earliestDeliveryDate);
        return earliestDeliveryDate;
    }

    /**
     * Applies the date selected on the confirmation screen only after checking
     * the current delivery area again. The address step stores no requested
     * date, so this is the single point where the date enters the checkout Session.
     */
    public CheckoutDeliveryData applyRequestedDeliveryDate(
            CheckoutDeliveryData data,
            LocalDate requestedDeliveryDate) {
        if (data == null || data.getDeliveryMethod() == null) {
            throw new CheckoutDeliveryException("受取方法を確認してください。");
        }

        if (data.getDeliveryMethod() != DeliveryMethod.SHIPPING) {
            if (requestedDeliveryDate != null) {
                throw new CheckoutDeliveryException("店頭受取ではお届け希望日を指定できません。");
            }
            return copyWithRequestedDeliveryDate(data, null);
        }

        DeliveryArea area = findAvailableArea(data.getPrefecture());
        LocalDate earliestDeliveryDate = calculateEarliestDeliveryDate(area.getLeadDays());
        validateRequestedDeliveryDate(requestedDeliveryDate, earliestDeliveryDate);
        return copyWithRequestedDeliveryDate(data, requestedDeliveryDate);
    }

    /**
     * Supplies the minimum date to the delivery form after a prefecture has
     * been entered. The repository lookup remains in this service so the
     * controller and Thymeleaf never duplicate delivery-area business rules.
     */
    public LocalDate calculateEarliestDeliveryDate(String deliveryOption, String prefecture) {
        if (!DELIVERY_OPTION_DELIVERY.equals(deliveryOption)) {
            return null;
        }
        return calculateEarliestDeliveryDate(findAvailableArea(prefecture).getLeadDays());
    }

    public CheckoutDeliveryForm toForm(CheckoutDeliveryData data) {
        CheckoutDeliveryForm form = new CheckoutDeliveryForm();
        form.setDeliveryOption(data.getDeliveryMethod() == DeliveryMethod.STORE_PICKUP
                ? DELIVERY_OPTION_STORE_PICKUP
                : DELIVERY_OPTION_DELIVERY);
        form.setRecipientName(data.getRecipientName());
        form.setPhoneNumber(data.getPhoneNumber());
        form.setPostalCode(data.getPostalCode());
        form.setPrefecture(data.getPrefecture());
        form.setCityAddress(data.getCityAddress());
        form.setAddressDetail(data.getAddressDetail());
        form.setSavedAddressId(data.getSavedAddressId());
        form.setRequestedDeliveryDate(data.getRequestedDeliveryDate());
        return form;
    }

    public CheckoutDeliveryConfirmForm toConfirmationForm(CheckoutDeliveryData data) {
        CheckoutDeliveryConfirmForm form = new CheckoutDeliveryConfirmForm();
        form.setRequestedDeliveryDate(data.getRequestedDeliveryDate());
        return form;
    }

    public String formatDeliveryDate(LocalDate date) {
        return date == null ? "指定なし" : DELIVERY_DATE_DISPLAY_FORMATTER.format(date);
    }

    public void saveToSession(HttpSession session, CheckoutDeliveryData data) {
        // Only temporary checkout data is stored until order confirmation; no JPA entity or order table is changed here.
        session.setAttribute(CHECKOUT_DELIVERY_SESSION_ATTRIBUTE, data);
    }

    public Optional<CheckoutDeliveryData> getFromSession(HttpSession session) {
        Object savedData = session.getAttribute(CHECKOUT_DELIVERY_SESSION_ATTRIBUTE);
        if (savedData instanceof CheckoutDeliveryData data) {
            return Optional.of(data);
        }
        return Optional.empty();
    }

    private DeliveryMethod resolveDeliveryMethod(String deliveryOption) {
        /*
         * 購入者には配送手段を選ばせず、内部的な配送方式はシステム側で判断する設計としている。
         * 現時点ではお届けをSHIPPINGとして扱い、LOCAL_DELIVERYの自動判定は将来拡張とする。
         */
        if (DELIVERY_OPTION_DELIVERY.equals(deliveryOption)) {
            return DeliveryMethod.SHIPPING;
        }
        if (DELIVERY_OPTION_STORE_PICKUP.equals(deliveryOption)) {
            return DeliveryMethod.STORE_PICKUP;
        }
        throw new CheckoutDeliveryException("受取方法を選択してください。");
    }

    private void validateRecipientInformation(String recipientName, String phoneNumber) {
        if (!StringUtils.hasText(recipientName) || !StringUtils.hasText(phoneNumber)) {
            throw new CheckoutDeliveryException("受取人氏名と電話番号を入力してください。");
        }
    }

    private void validateAddressInformation(
            String postalCode,
            String prefecture,
            String cityAddress,
            String addressDetail) {
        if (!StringUtils.hasText(postalCode)
                || !StringUtils.hasText(prefecture)
                || !StringUtils.hasText(cityAddress)
                || !StringUtils.hasText(addressDetail)) {
            throw new CheckoutDeliveryException("郵便番号、都道府県、市区町村・町域、番地・建物名を入力してください。");
        }
    }

    private void validateAddressLineLength(String addressLine) {
        if (addressLine != null && addressLine.length() > 255) {
            throw new CheckoutDeliveryException("市区町村・町域と番地・建物名は合わせて255文字以内で入力してください。");
        }
    }

    private DeliveryArea findAvailableArea(String prefecture) {
        if (!StringUtils.hasText(prefecture)) {
            throw new CheckoutDeliveryException("お届けでは都道府県を入力してください。");
        }

        DeliveryArea area = deliveryAreaRepository.findByPrefecture(prefecture)
                .orElseThrow(() -> new CheckoutDeliveryException("指定された都道府県には配送できません。"));

        if (!Boolean.TRUE.equals(area.getAvailable())) {
            throw new CheckoutDeliveryException("指定された都道府県は現在配送対象外です。");
        }

        return area;
    }

    private LocalDate calculateEarliestDeliveryDate(Integer leadDays) {
        if (leadDays == null || leadDays < 0) {
            throw new CheckoutDeliveryException("配送地域のリード日数が不正です。");
        }
        return LocalDate.now().plusDays(leadDays);
    }

    private CheckoutDeliveryData copyWithRequestedDeliveryDate(
            CheckoutDeliveryData data,
            LocalDate requestedDeliveryDate) {
        return new CheckoutDeliveryData(
                data.getDeliveryMethod(),
                data.getRecipientName(),
                data.getPhoneNumber(),
                data.getPostalCode(),
                data.getPrefecture(),
                data.getCityAddress(),
                data.getAddressDetail(),
                data.getShippingFee(),
                data.getLeadDays(),
                requestedDeliveryDate,
                data.getSavedAddressId());
    }

    private void validateRequestedDeliveryDate(
            LocalDate requestedDeliveryDate,
            LocalDate earliestDeliveryDate) {
        if (requestedDeliveryDate != null && requestedDeliveryDate.isBefore(earliestDeliveryDate)) {
            throw new CheckoutDeliveryException(
                    "お届け希望日は最短お届け予定日以降の日付を選択してください。");
        }
    }

    private void validateSavedAddressInformation(
            String postalCode,
            String prefecture,
            String addressLine) {
        if (!StringUtils.hasText(postalCode)
                || !StringUtils.hasText(prefecture)
                || !StringUtils.hasText(addressLine)) {
            throw new CheckoutDeliveryException("保存済みのお届け先に必要な住所情報がありません。");
        }
    }

    private String combineAddress(String cityAddress, String addressDetail) {
        if (cityAddress == null) {
            return addressDetail;
        }
        if (addressDetail == null) {
            return cityAddress;
        }
        return cityAddress + addressDetail;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
