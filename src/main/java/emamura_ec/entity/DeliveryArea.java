package emamura_ec.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "delivery_areas")
public class DeliveryArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_area_id")
    private Long deliveryAreaId;

    @Column(name = "prefecture", nullable = false, length = 10)
    private String prefecture;

    @Column(name = "shipping_fee", nullable = false)
    private Integer shippingFee;

    @Column(name = "lead_days", nullable = false)
    private Integer leadDays;

    @Column(name = "available", nullable = false)
    private Boolean available;

    protected DeliveryArea() {
    }

    public Long getDeliveryAreaId() {
        return deliveryAreaId;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public Integer getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Integer shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Integer getLeadDays() {
        return leadDays;
    }

    public void setLeadDays(Integer leadDays) {
        this.leadDays = leadDays;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
