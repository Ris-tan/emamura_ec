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

    private final DeliveryAreaRepository deliveryAreaRepository;

    public CheckoutDeliveryService(DeliveryAreaRepository deliveryAreaRepository) {
        this.deliveryAreaRepository = deliveryAreaRepository;
    }

    public CheckoutDeliveryData validateAndCreate(CheckoutDeliveryForm form) {
        DeliveryMethod deliveryMethod = form.getDeliveryMethod();
        if (deliveryMethod == null) {
            throw new CheckoutDeliveryException("受取方法を選択してください。");
        }

        Integer shippingFee = null;
        Integer leadDays = null;

        if (deliveryMethod == DeliveryMethod.SHIPPING) {
            String prefecture = trimToNull(form.getPrefecture());
            DeliveryArea area = findAvailableArea(prefecture);
            shippingFee = area.getShippingFee();
            leadDays = area.getLeadDays();
        }

        return new CheckoutDeliveryData(
                deliveryMethod,
                trimToNull(form.getRecipientName()),
                trimToNull(form.getPhoneNumber()),
                trimToNull(form.getPostalCode()),
                trimToNull(form.getPrefecture()),
                trimToNull(form.getAddressLine()),
                shippingFee,
                leadDays);
    }

    public void saveToSession(HttpSession session, CheckoutDeliveryData data) {
        // 注文確定前の一時情報だけを保存し、JPA Entityや注文テーブルは変更しない。
        session.setAttribute(CHECKOUT_DELIVERY_SESSION_ATTRIBUTE, data);
    }

    public Optional<CheckoutDeliveryData> getFromSession(HttpSession session) {
        Object savedData = session.getAttribute(CHECKOUT_DELIVERY_SESSION_ATTRIBUTE);
        if (savedData instanceof CheckoutDeliveryData data) {
            return Optional.of(data);
        }
        return Optional.empty();
    }

    private DeliveryArea findAvailableArea(String prefecture) {
        if (!StringUtils.hasText(prefecture)) {
            throw new CheckoutDeliveryException("宅配便では都道府県を入力してください。");
        }

        DeliveryArea area = deliveryAreaRepository.findByPrefecture(prefecture)
                .orElseThrow(() -> new CheckoutDeliveryException("指定された都道府県には配送できません。"));

        if (!Boolean.TRUE.equals(area.getAvailable())) {
            throw new CheckoutDeliveryException("指定された都道府県は現在配送対象外です。");
        }

        return area;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
