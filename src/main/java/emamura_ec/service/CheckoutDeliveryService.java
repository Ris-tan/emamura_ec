package emamura_ec.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.entity.DeliveryArea;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.repository.DeliveryAreaRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutDeliveryService {

    private static final String CHECKOUT_DELIVERY_SESSION_ATTRIBUTE = "checkoutDeliveryData";
    private static final String DELIVERY_OPTION_DELIVERY = "DELIVERY";
    private static final String DELIVERY_OPTION_STORE_PICKUP = "STORE_PICKUP";

    private final DeliveryAreaRepository deliveryAreaRepository;

    public CheckoutDeliveryService(DeliveryAreaRepository deliveryAreaRepository) {
        this.deliveryAreaRepository = deliveryAreaRepository;
    }

    public CheckoutDeliveryData validateAndCreate(CheckoutDeliveryForm form) {
        DeliveryMethod deliveryMethod = resolveDeliveryMethod(form.getDeliveryOption());
        String recipientName = trimToNull(form.getRecipientName());
        String phoneNumber = trimToNull(form.getPhoneNumber());
        String postalCode = trimToNull(form.getPostalCode());
        String prefecture = trimToNull(form.getPrefecture());
        String cityAddress = trimToNull(form.getCityAddress());
        String addressDetail = trimToNull(form.getAddressDetail());
        String addressLine = combineAddress(cityAddress, addressDetail);

        // Zipcloud only assists the UI; all submitted values remain untrusted and are validated on the server.
        validateRecipientInformation(recipientName, phoneNumber);
        if (deliveryMethod != DeliveryMethod.STORE_PICKUP) {
            validateAddressInformation(postalCode, prefecture, cityAddress, addressDetail);
        }
        validateAddressLineLength(addressLine);

        Integer shippingFee = null;
        Integer leadDays = null;

        if (deliveryMethod == DeliveryMethod.SHIPPING) {
            DeliveryArea area = findAvailableArea(prefecture);
            shippingFee = area.getShippingFee();
            leadDays = area.getLeadDays();
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
                leadDays);
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
