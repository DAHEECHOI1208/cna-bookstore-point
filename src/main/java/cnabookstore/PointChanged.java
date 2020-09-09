package cnabookstore;

public class PointChanged extends AbstractEvent {

    private Long id;
    private String customerName;
    private Integer pointValue;
    private String pointUseYn;
    private Long pointId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    private Long orderId;

    public PointChanged(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Integer getPointValue() {
        return pointValue;
    }

    public void setPointValue(Integer pointValue) {
        this.pointValue = pointValue;
    }
    public String getPointUseYn() {
        return pointUseYn;
    }

    public void setPointUseYn(String pointUseYn) {
        this.pointUseYn = pointUseYn;
    }
    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }
}
