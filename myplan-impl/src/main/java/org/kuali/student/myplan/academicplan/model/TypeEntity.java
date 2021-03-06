/**
 * Copyright 2010 The Kuali Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.myplan.academicplan.dto.TypeInfo;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.BaseAttributeEntity;
import org.kuali.student.r2.common.util.RichTextHelper;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@MappedSuperclass
public abstract class TypeEntity<T extends BaseAttributeEntity<?>> extends BaseTypeEntity implements AttributeOwner<T> {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
	private Set<T> attributes;

    @Column(name = "REF_OBJECT_URI")
    private String refObjectURI;

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.student.r2.common.entity.AttributeOwner#setAttributes(java.util.List)
	 */
	@Override
	public void setAttributes(Set<T> attributes) {
		this.attributes = attributes;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.student.r2.common.entity.AttributeOwner#getAttributes()
	 */
	@Override
	public Set<T> getAttributes() {
		return attributes;
	}

    public void setRefObjectURI(String refObjectURI) {
        this.refObjectURI = refObjectURI;
    }

    public String getRefObjectURI() {
        return refObjectURI;
    }

	public TypeInfo toDto() {
		TypeInfo typeInfo = new TypeInfo();
		typeInfo.setName(this.getName());
		typeInfo.setKey(this.getId());
		typeInfo.setRefObjectUri(getRefObjectURI());
                // TODO: handle formatted
		typeInfo.setDescr(new RichTextHelper().fromPlain (this.getDescr()));
		typeInfo.setEffectiveDate(this.getEffectiveDate());
		typeInfo.setExpirationDate(this.getExpirationDate());
		typeInfo.setAttributes(new ArrayList<AttributeInfo>());

		// TODO - refactor this into a central place; probably Igor's Converter
		List<AttributeInfo> atts = new ArrayList<AttributeInfo>();
		for (BaseAttributeEntity<?> att : this.getAttributes()) {
			atts.add(att.toDto());
		}
		// end refactor
		typeInfo.setAttributes(atts);
		
		return typeInfo;
	}
}
