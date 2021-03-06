/*
 * This is a common dao with basic CRUD operations and is not limited to any 
 * persistent layer implementation
 * 
 * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.smartitengineering.dao.common.queryparam.impl;

import com.smartitengineering.dao.common.queryparam.OperatorType;
import com.smartitengineering.dao.common.queryparam.ParameterType;
import com.smartitengineering.dao.common.queryparam.SimpleNameValueQueryParameter;

/**
 *
 * @author imyousuf
 */
public class QueryParameterWithNameValue<Template extends Object>
    implements SimpleNameValueQueryParameter<Template> {
    
    private QueryParameterAdapter<Template> queryParameter = new QueryParameterAdapter<Template>();

    protected QueryParameterWithNameValue() {
    }

    public Template getValue() {
        return queryParameter.getValue();
    }

    public ParameterType getParameterType() {
        return queryParameter.getParameterType();
    }

    public boolean isInitialized() {
        return queryParameter.isInitialized();
    }

    public OperatorType getOperatorType() {
        return queryParameter.getOperatorType();
    }

    public String getPropertyName() {
        return queryParameter.getPropertyName();
    }

    public void init(ParameterType type,
                     String propertyName,
                     Template value) {
        queryParameter.setType(type);
        queryParameter.setPropertyName(propertyName);
        queryParameter.setValue(value);
        queryParameter.setInitialized(true);
    }

}
