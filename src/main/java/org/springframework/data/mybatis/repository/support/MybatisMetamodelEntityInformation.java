/*
 *
 *   Copyright 2016 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.springframework.data.mybatis.repository.support;

import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Date;

/**
 * mybatis meta.
 *
 * @author Jarvis Song
 */
public class MybatisMetamodelEntityInformation<T, ID extends Serializable> extends MybatisEntityInformationSupport<T, ID> {

    private PersistentProperty<?> createdDateProperty;
    private PersistentProperty<?> lastModifiedDateProperty;

    /**
     * Creates a new {@link AbstractEntityInformation} from the given domain class.
     *
     * @param persistentEntity
     * @param domainClass      must not be {@literal null}.
     */
    protected MybatisMetamodelEntityInformation(PersistentEntity<T, ?> persistentEntity, Class<T> domainClass) {
        super(persistentEntity, domainClass);

        createdDateProperty = persistentEntity.getPersistentProperty(CreatedDate.class);
        lastModifiedDateProperty = persistentEntity.getPersistentProperty(LastModifiedDate.class);
    }

    @Override
    public ID getId(T entity) {
        if (null == persistentEntity) {
            return null;
        }

        return (ID) persistentEntity.getIdentifierAccessor(entity).getIdentifier();

    }

    @Override
    public Class<ID> getIdType() {
        if (null == persistentEntity) {
            return null;
        }

        PersistentProperty idProperty = persistentEntity.getIdProperty();
        if (null == idProperty) {
            return null;
        }
        return (Class<ID>) idProperty.getType();
    }

    @Override
    public boolean hasVersion() {
        return persistentEntity.hasVersionProperty();
    }


    private void setAuditDate(PersistentProperty<?> property, T entity, Class<? extends Annotation> annotationType) {


        Class<?> type = property.getRawType();
        if (Date.class.isAssignableFrom(type)) {
            persistentEntity.getPropertyAccessor(entity).setProperty(property, new Date());
        } else if (DateTime.class == type) {
            persistentEntity.getPropertyAccessor(entity).setProperty(property, DateTime.now());
        } else if (Long.class == type || long.class == type) {
            persistentEntity.getPropertyAccessor(entity).setProperty(property, System.currentTimeMillis());
        } else {
            throw new IllegalArgumentException("now we can not support " + type.getName() + " for " + annotationType.getName());
        }
    }

    @Override
    public void setCreatedDate(T entity) {
        if (null == createdDateProperty) {
            return;
        }
        setAuditDate(createdDateProperty, entity, CreatedDate.class);
    }

    @Override
    public void setLastModifiedDate(T entity) {
        if (null == lastModifiedDateProperty) {
            return;
        }
        setAuditDate(lastModifiedDateProperty, entity, LastModifiedDate.class);

    }

    @Override
    public int increaseVersion(T entity) {
        PersistentProperty<?> versionProperty = persistentEntity.getVersionProperty();
        if (null == versionProperty) {
            return 0;
        }

        PersistentPropertyAccessor accessor = persistentEntity.getPropertyAccessor(entity);
        int newVer = (Integer) accessor.getProperty(versionProperty) + 1;
        accessor.setProperty(versionProperty, newVer);

        return newVer;
    }

    @Override
    public void setVersion(T entity, int version) {
        PersistentProperty<?> versionProperty = persistentEntity.getVersionProperty();
        if (null == versionProperty) {
            return;
        }
        persistentEntity.getPropertyAccessor(entity).setProperty(versionProperty, version);

    }
}