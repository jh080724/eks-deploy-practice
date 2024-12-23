package com.playdata.orderingservice.ordering.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrdering is a Querydsl query type for Ordering
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrdering extends EntityPathBase<Ordering> {

    private static final long serialVersionUID = -1194610091L;

    public static final QOrdering ordering = new QOrdering("ordering");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<OrderDetail, QOrderDetail> orderDetails = this.<OrderDetail, QOrderDetail>createList("orderDetails", OrderDetail.class, QOrderDetail.class, PathInits.DIRECT2);

    public final EnumPath<OrderStatus> orderStatus = createEnum("orderStatus", OrderStatus.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QOrdering(String variable) {
        super(Ordering.class, forVariable(variable));
    }

    public QOrdering(Path<? extends Ordering> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrdering(PathMetadata metadata) {
        super(Ordering.class, metadata);
    }

}

