/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.commonentities.datamodel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * <p>Entity of the Common Datamodel (which uses all the possible JPA 2.0 Annotations as described in the 
 * <a href="http://www.j2ee.me/javaee/6/docs/api/javax/persistence/package-summary.html">javax.persistence documentation</a>)
 * All the Entity0xxx named entities are the base of a class hierarchy and define the primary key for the entire hierarchy. 
 * Per the JSR 317 final spec (page 28), the primary key of these base classes may be any of the Java primitive types, 
 * Java primitive wrapper types, or a composite primary key (via @IdClass annotation)
 * 
 * 
 * <p>These annotations are exercised:
 * <ul>
 *      <li><b>@Id</b> (22 of 65):
 *      <p>Specifies the primary key property or field of an entity
 *      <p>These are the possible @Id combinations:
 *      <ol>
 *          <li>@Id on field/property (of type byte)
 *          <li>@Id on field/property (of type Byte)
 *          <li>@Id on field/property (of type char)
 *          <li>@Id on field/property (of type Character)
 *          <li>@Id on field/property (of type String)
 *          <li>@Id on field/property (of type double)
 *          <li>@Id on field/property (of type Double)
 *          <li>@Id on field/property (of type float)
 *          <li>@Id on field/property (of type Float)
 *          <li>@Id on field/property (of type int)
 *          <li>@Id on field/property (of type Integer)
 *          <li>@Id on field/property (of type long)
 *          <li>@Id on field/property (of type Long)
 *          <li>@Id on field/property (of type short)
 *          <li>@Id on field/property (of type Short)
 *          <li>@Id on field/property (of type BigDecimal)
 *          <li>@Id on field/property (of type BigInteger)
 *          <li>@Id on field/property (of type java.util.Date) 
 *          <li>@Id on field/property (of type java.sql.Date)
 *          <li>@Id on field/property (of type Object)
 *          <li>@Id on field/property (of type Object ID)
 *      </ol>
 *      <p>These are the @Id combinations exercised in this entity:
 *      <ul>
 *          <li>@Id on field/property (of type Object)
 *      </ul>
 * 
 *      <li><b>@Version</b> (65 of 65):
 *      <p>This annotation specifies the version field or property of an entity class that serves as its optimistic lock value
 *      <p>These are the possible @Version combinations:
 *      <ul>
 *          <li>@Version on field/property (of type int)
 *          <li>@Version on field/property (of type Integer)
 *          <li>@Version on field/property (of type long)
 *          <li>@Version on field/property (of type Long)
 *          <li>@Version on field/property (of type short)
 *          <li>@Version on field/property (of type Short)
 *          <li>@Version on field/property (of type Timestamp)
 *      </ul>
 *      <p>These are the @Version combinations exercised in this entity:
 *      <ul>
 *          <li>@Version on field/property (of type long)
 *      </ul>
 * </ul>
 * 
 * 
 * <p><b>Notes:</b>
 * <ol>
 *      <li>The parent entity is Entity00xx which has a simple primary key 
 *      <li>The dependent entity is Entity04xx which also has a simple primary key, but is mapped by a relationship 
 *          annotation to Entity00xx
 *      <li>The primary key type of Entity4xx is thus the primary key type as Entity00xx
 * </ol>
 * 
 * 
 * <p><b>UML:</b>
 * <pre>
 * 
 *          +--------------+                +--------------+
 *          |              |                |              |
 *          |  Entity04xx  |--------------->|  Entity00xx  |
 *          |              |  1          1  |              |
 *          +--------------+                +--------------+ 
 * 
 * </pre>
 */
@NamedQueries ( 
    value={
        @NamedQuery(name  = "ENTITY0407_SELECT",
                    query = "SELECT e FROM Entity0407 e WHERE e.entity0407_id = :id_0407")
    }
)
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="CDM_Entity0407")
public class Entity0407 {

    @Id
    @OneToOne
    private Entity0007  entity0407_id;

    @Version
    private long        entity0407_version;

    private String      entity0407_string01;

    private String      entity0407_string02;

    private String      entity0407_string03;


    public Entity0407() {
    }

    public Entity0407(Entity0007  id,
                      long        version,
                      String      string01,
                      String      string02,
                      String      string03) {
        this.entity0407_id       = id;
        this.entity0407_version  = version;
        this.entity0407_string01 = string01;
        this.entity0407_string02 = string02;
        this.entity0407_string03 = string03;
    }

    public String toString() {
        return( " Entity0407: "          + 
                " entity0407_id: "       + getEntity0407_id() + 
                " entity0407_version: "  + getEntity0407_version() + 
                " entity0407_string01: " + getEntity0407_string01() +
                " entity0407_string02: " + getEntity0407_string02() + 
                " entity0407_string03: " + getEntity0407_string03() );
    }


    //----------------------------------------------------------------------------------------------
    // Persisent property accessor(s)
    //----------------------------------------------------------------------------------------------
    public Entity0007 getEntity0407_id() {
        return entity0407_id;
    }
    public void setEntity0407_id(Entity0007 p) {
        this.entity0407_id = p;
    }


    public long getEntity0407_version() {
        return entity0407_version;
    }
    public void setEntity0407_version(long p) {
        this.entity0407_version = p;
    }


    public String getEntity0407_string01() {
        return entity0407_string01;
    }
    public void setEntity0407_string01(String p) {
        this.entity0407_string01 = p;
    }


    public String getEntity0407_string02() {
        return entity0407_string02;
    }
    public void setEntity0407_string02(String p) {
        this.entity0407_string02 = p;
    }


    public String getEntity0407_string03() {
        return entity0407_string03;
    }
    public void setEntity0407_string03(String p) {
        this.entity0407_string03 = p;
    }
}
