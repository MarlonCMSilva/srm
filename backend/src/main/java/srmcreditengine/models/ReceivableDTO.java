package srmcreditengine.models;

import srmcreditengine.entities.Receivable;
import srmcreditengine.entities.Settlement;
import srmcreditengine.entities.enums.ReceivableStatus;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReceivableDTO {

    private Long id;
    private String assignor;
    private ReceivableType type;
    private BigDecimal faceValue;
    private Integer termInMonths;
    private ReceivableStatus status;
    private LocalDate dueDate;

    private Settlement settlements;

    public ReceivableDTO() {
    }

    public ReceivableDTO(Long id, String assignor, ReceivableType type, BigDecimal faceValue, Integer termInMonths, ReceivableStatus status, LocalDate dueDate) {
        this.id = id;
        this.assignor = assignor;
        this.type = type;
        this.faceValue = faceValue;
        this.termInMonths = termInMonths;
        this.status = status;
        this.dueDate = dueDate;
    }


    public ReceivableDTO(Receivable entity) {
        id = entity.getId();
        assignor = entity.getAssignor();
        type = entity.getType();
        faceValue = entity.getFaceValue();
        termInMonths = entity.getTermInMonths();
        status = ReceivableStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public String getAssignor() {
        return assignor;
    }

    public ReceivableType getType() {
        return type;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public ReceivableStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Settlement getSettlements() {
        return settlements;
    }
}
