package srmcreditengine.entities;

import jakarta.persistence.*;
import srmcreditengine.entities.enums.ReceivableStatus;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_receivable")
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assignor;

    @Enumerated(EnumType.STRING)
    private ReceivableType type;
    private BigDecimal faceValue;
    private Integer termInMonths;

    @Enumerated(EnumType.STRING)
    private ReceivableStatus status;

    @Column(name = "due_date",
            columnDefinition = "TIMESTAMP")
    private LocalDateTime dueDate;


    @OneToOne(mappedBy = "receivable", optional = true)
    private Settlement settlement;

    public Receivable() {
    }

    public Receivable(Long id, String assignor, ReceivableType type, BigDecimal faceValue, Integer termInMonths, ReceivableStatus status, LocalDateTime dueDate, Settlement settlements) {
        this.id = id;
        this.assignor = assignor;
        this.type = type;
        this.faceValue = faceValue;
        this.termInMonths = termInMonths;
        this.status = status;
        this.dueDate = dueDate;
        this.settlement = settlements;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssignor() {
        return assignor;
    }

    public void setAssignor(String assignor) {
        this.assignor = assignor;
    }

    public ReceivableType getType() {
        return type;
    }

    public void setType(ReceivableType type) {
        this.type = type;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(BigDecimal faceValue) {
        this.faceValue = faceValue;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }

    public ReceivableStatus getStatus() {
        return status;
    }

    public void setStatus(ReceivableStatus status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Receivable that = (Receivable) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}