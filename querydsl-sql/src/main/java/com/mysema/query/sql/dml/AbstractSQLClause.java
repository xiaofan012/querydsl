/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.query.sql.dml;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysema.query.QueryException;
import com.mysema.query.sql.Configuration;
import com.mysema.query.types.ParamNotSetException;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.Param;

/**
 * AbstractSQLClause is a superclass for SQL based DMLClause implementations
 * 
 * @author tiwe
 *
 */
public class AbstractSQLClause {
    
    protected final Configuration configuration;
    
    public AbstractSQLClause(Configuration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Set the parameters to the given PreparedStatement
     * 
     * @param stmt preparedStatement to be populated
     * @param objects list of constants
     * @param constantPaths list of paths related to the constants
     * @param params map of param to value for param resolving
     */
    protected void setParameters(PreparedStatement stmt, List<?> objects, List<Path<?>> constantPaths, Map<Param<?>, ?> params) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() + " paths, but got " + constantPaths.size());
        }
        int counter = 1;
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            try {
                if (Param.class.isInstance(o)) {
                    if (!params.containsKey(o)) {
                        throw new ParamNotSetException((Param<?>) o);
                    }
                    o = params.get(o);
                }
                counter += configuration.set(stmt, constantPaths.get(i), counter, o);
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }    
    
    protected Map<String, Field> getPathFields(Class<?> cl) {
        Map<String, Field> fields = new HashMap<String, Field>();
        while (!cl.equals(Object.class)) {
            for (Field field : cl.getDeclaredFields()) {
                if (Path.class.isAssignableFrom(field.getType()) && !fields.containsKey(field.getName())) {
                    field.setAccessible(true);
                    fields.put(field.getName(), field);
                }
            }
            cl = cl.getSuperclass();
        }
        return fields;
    }
    
    protected void close(PreparedStatement stmt) {
        try {
            stmt.close();
        } catch (SQLException e) {
            throw new QueryException(e);
        }
    }

    protected void close(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new QueryException(e);
        }
    }

}
