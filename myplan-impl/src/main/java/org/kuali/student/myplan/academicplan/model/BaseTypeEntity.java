package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r1.common.entity.KSEntityConstants;
import org.kuali.student.r2.common.entity.BaseVersionEntity;

import javax.persistence.*;
import java.util.Date;


@MappedSuperclass
@AttributeOverrides({
@AttributeOverride(name="id", column=@Column(name="TYPE_KEY"))})
public class BaseTypeEntity extends BaseVersionEntity {
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "TYPE_DESC",length= KSEntityConstants.LONG_TEXT_LENGTH)
	private String descr;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EFF_DT")
	private Date effectiveDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXPIR_DT")
	private Date expirationDate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
}
