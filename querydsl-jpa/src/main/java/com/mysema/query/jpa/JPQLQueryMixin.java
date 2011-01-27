/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.query.jpa;

import java.util.List;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.JoinFlag;
import com.mysema.query.QueryMetadata;
import com.mysema.query.support.CollectionAnyVisitor;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Predicate;

/**
 * JPQLQueryMixin extends QueryMixin to support JPQL join construction
 * 
 * @author tiwe
 *
 * @param <T>
 */
public class JPQLQueryMixin<T> extends QueryMixin<T> {
    
    public static final JoinFlag FETCH = new JoinFlag("fetch ");
    
    public static final JoinFlag FETCH_ALL_PROPERTIES = new JoinFlag(" fetch all properties");
    
    public JPQLQueryMixin() {}

    public JPQLQueryMixin(QueryMetadata metadata) {
        super(metadata);
    }

    public JPQLQueryMixin(T self, QueryMetadata metadata) {
        super(self, metadata);
    }

    public T fetch(){
        List<JoinExpression> joins = getMetadata().getJoins();
        joins.get(joins.size()-1).addFlag(FETCH);
        return getSelf();
    }

    public T fetchAll(){
        List<JoinExpression> joins = getMetadata().getJoins();
        joins.get(joins.size()-1).addFlag(FETCH_ALL_PROPERTIES);
        return getSelf();
    }

    public T with(Predicate... conditions){
        for (Predicate condition : normalize(conditions, false)){
            getMetadata().addJoinCondition(condition);
        }
        return getSelf();
    }

    @Override
    protected Predicate[] normalize(Predicate[] conditions, boolean where) {
        for (int i = 0; i < conditions.length; i++){
            conditions[i] = normalize(conditions[i]);
        }
        return conditions;
    }

    private Predicate normalize(Predicate predicate) {
        if (predicate instanceof BooleanBuilder && ((BooleanBuilder)predicate).getValue() == null){
            return predicate;
        }else{
            return (Predicate) predicate.accept(JPQLCollectionAnyVisitor.DEFAULT, new CollectionAnyVisitor.Context());    
        }        
    }
    
}