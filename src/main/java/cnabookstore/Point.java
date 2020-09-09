package cnabookstore;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Point_table")
public class Point {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerName;
    private Integer pointValue;
    private String pointUseYn;
    private Long pointId;

    @PostPersist
    public void onPostPersist(){
        PointRegisterd pointRegisterd = new PointRegisterd();
        BeanUtils.copyProperties(this, pointRegisterd);
        pointRegisterd.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        PointChanged pointChanged = new PointChanged();
        BeanUtils.copyProperties(this, pointChanged);
        pointChanged.publishAfterCommit();


    }

    @PostRemove
    public void onPostRemove(){
        PointDeleted pointDeleted = new PointDeleted();
        BeanUtils.copyProperties(this, pointDeleted);
        pointDeleted.publishAfterCommit();


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
