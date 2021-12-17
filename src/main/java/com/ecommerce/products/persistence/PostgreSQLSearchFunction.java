package com.ecommerce.products.persistence;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.Type;

import java.util.List;

public class PostgreSQLSearchFunction implements SQLFunction {

    private StringBuilder fragment = new StringBuilder();

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return new BooleanType();
    }

    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new IllegalArgumentException("The function must be passed at least 2 arguments");
        }

        fragment.setLength(0);
        String ftsConfiguration = null;
        String field = null;
        String value = null;

        if (arguments.size() == 3) {
            ftsConfiguration = (String) arguments.get(0);
            field = (String) arguments.get(1);
            value = (String) arguments.get(2);

            fragment.append(field);
            fragment.append(" @@ ");
            fragment.append("to_tsquery(");
            fragment.append(ftsConfiguration);
            fragment.append(", ");
            fragment.append(value);
            fragment.append(")");
        } else {
            field = (String) arguments.get(0);
            value = (String) arguments.get(1);

            fragment.append(field);
            fragment.append(" @@ ");
            fragment.append("to_tsquery(");
            fragment.append(value);
            fragment.append(")");
        }

        return fragment.toString();
    }
}
