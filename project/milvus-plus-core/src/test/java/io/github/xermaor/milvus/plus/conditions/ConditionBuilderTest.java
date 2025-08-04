package io.github.xermaor.milvus.plus.conditions;

import io.github.xermaor.milvus.plus.core.FieldFunction;
import io.github.xermaor.milvus.plus.core.conditions.ConditionBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionBuilderTest {

    private TestConditionBuilder createBuilderInstance() {
        return new TestConditionBuilder();
    }

    @Test
    void testTextMatchSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch("name", "value");
        assertEquals("TEXT_MATCH(name, \"value\")", builder.build());
    }

    @Test
    void testTextMatchConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch(false, "name", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testTextMatchConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch(true, "name", "value");
        assertEquals("TEXT_MATCH(name, \"value\")", builder.build());
    }

    @Test
    void testTextMatchListValues() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch("name", List.of("val1", "val2"));
        assertEquals("TEXT_MATCH(name, \"val1 val2\")", builder.build());
    }

    @Test
    void testTextMatchConditionFalseWithList() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch(false, "name", List.of("val1", "val2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testTextMatchConditionTrueWithList() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.textMatch(true, "name", List.of("val1", "val2"));
        assertEquals("TEXT_MATCH(name, \"val1 val2\")", builder.build());
    }

    @Test
    void testTextMatchUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(fieldFunction, "value");
        assertEquals("TEXT_MATCH(fieldName, \"value\")", builder.build());
    }

    @Test
    void testTextMatchConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testTextMatchConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(true, fieldFunction, "value");
        assertEquals("TEXT_MATCH(fieldName, \"value\")", builder.build());
    }

    @Test
    void testTextMatchListValuesUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(fieldFunction, List.of("val1", "val2"));
        assertEquals("TEXT_MATCH(fieldName, \"val1 val2\")", builder.build());
    }

    @Test
    void testTextMatchConditionFalseWithListUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(false, fieldFunction, List.of("val1", "val2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testTextMatchConditionTrueWithListUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.textMatch(true, fieldFunction, List.of("val1", "val2"));
        assertEquals("TEXT_MATCH(fieldName, \"val1 val2\")", builder.build());
    }

    @Test
    void testEqSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("age", 25);
        assertEquals("age == 25", builder.build());
    }

    @Test
    void testEqConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq(false, "age", 25);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testEqConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq(true, "age", 25);
        assertEquals("age == 25", builder.build());
    }

    @Test
    void testEqUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("name");

        builder.eq(fieldFunction, "John");
        assertEquals("name == \"John\"", builder.build());
    }

    @Test
    void testEqConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("name");

        builder.eq(false, fieldFunction, "John");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testEqConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("name");

        builder.eq(true, fieldFunction, "John");
        assertEquals("name == \"John\"", builder.build());
    }

    @Test
    void testNeSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ne("status", "inactive");
        assertEquals("status != \"inactive\"", builder.build());
    }

    @Test
    void testNeConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ne(false, "status", "inactive");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNeConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ne(true, "status", "inactive");
        assertEquals("status != \"inactive\"", builder.build());
    }

    @Test
    void testNeUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.ne(fieldFunction, "inactive");
        assertEquals("status != \"inactive\"", builder.build());
    }

    @Test
    void testNeConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.ne(false, fieldFunction, "inactive");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNeConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.ne(true, fieldFunction, "inactive");
        assertEquals("status != \"inactive\"", builder.build());
    }

    @Test
    void testGtSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.gt("age", 30);
        assertEquals("age > 30", builder.build());
    }

    @Test
    void testGtConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.gt(false, "age", 30);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testGtConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.gt(true, "age", 30);
        assertEquals("age > 30", builder.build());
    }

    @Test
    void testGtUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.gt(fieldFunction, 30);
        assertEquals("age > 30", builder.build());
    }

    @Test
    void testGtConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.gt(false, fieldFunction, 30);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testGtConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.gt(true, fieldFunction, 30);
        assertEquals("age > 30", builder.build());
    }

    @Test
    void testGeSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ge("score", 85);
        assertEquals("score >= 85", builder.build());
    }

    @Test
    void testGeConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ge(false, "score", 85);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testGeConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.ge(true, "score", 85);
        assertEquals("score >= 85", builder.build());
    }

    @Test
    void testGeUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("score");

        builder.ge(fieldFunction, 85);
        assertEquals("score >= 85", builder.build());
    }

    @Test
    void testGeConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("score");

        builder.ge(false, fieldFunction, 85);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testGeConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("score");

        builder.ge(true, fieldFunction, 85);
        assertEquals("score >= 85", builder.build());
    }

    @Test
    void testLtSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.lt("age", 18);
        assertEquals("age < 18", builder.build());
    }

    @Test
    void testLtConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.lt(false, "age", 18);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLtConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.lt(true, "age", 18);
        assertEquals("age < 18", builder.build());
    }

    @Test
    void testLtUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.lt(fieldFunction, 18);
        assertEquals("age < 18", builder.build());
    }

    @Test
    void testLtConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.lt(false, fieldFunction, 18);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLtConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("age");

        builder.lt(true, fieldFunction, 18);
        assertEquals("age < 18", builder.build());
    }

    @Test
    void testLeSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.le("price", 200);
        assertEquals("price <= 200", builder.build());
    }

    @Test
    void testLeConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.le(false, "price", 200);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLeConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.le(true, "price", 200);
        assertEquals("price <= 200", builder.build());
    }

    @Test
    void testLeUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("price");

        builder.le(fieldFunction, 200);
        assertEquals("price <= 200", builder.build());
    }

    @Test
    void testLeConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("price");

        builder.le(false, fieldFunction, 200);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLeConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("price");

        builder.le(true, fieldFunction, 200);
        assertEquals("price <= 200", builder.build());
    }

    @Test
    void testBetweenWithValidValues() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.between("age", 18, 30);
        assertEquals("age >= 18 && age <= 30", builder.build());
    }

    @Test
    void testBetweenWithConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.between(false, "age", 18, 30);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testBetweenWithConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.between(true, "age", 18, 30);
        assertEquals("age >= 18 && age <= 30", builder.build());
    }

    @Test
    void testBetweenUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.between(fieldFunction, 10, 20);
        assertEquals("testField >= 10 && testField <= 20", builder.build());
    }

    @Test
    void testBetweenConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.between(false, fieldFunction, 10, 20);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testBetweenConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.between(true, fieldFunction, 10, 20);
        assertEquals("testField >= 10 && testField <= 20", builder.build());
    }

    @Test
    void testNotBetweenWithValidValues() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notBetween("age", 18, 30);
        assertEquals("not (age >= 18 && age <= 30)", builder.build());
    }

    @Test
    void testNotBetweenConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notBetween(false, "age", 18, 30);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotBetweenConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notBetween(true, "age", 18, 30);
        assertEquals("not (age >= 18 && age <= 30)", builder.build());
    }

    @Test
    void testNotBetweenUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.notBetween(fieldFunction, 10, 20);
        assertEquals("not (testField >= 10 && testField <= 20)", builder.build());
    }

    @Test
    void testNotBetweenConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.notBetween(false, fieldFunction, 10, 20);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotBetweenConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("testField");

        builder.notBetween(true, fieldFunction, 10, 20);
        assertEquals("not (testField >= 10 && testField <= 20)", builder.build());
    }

    @Test
    void testIsNull() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNull("fieldName");
        assertEquals("fieldName is null", builder.build());
    }

    @Test
    void testIsNullConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNull(false, "fieldName");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testIsNullConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNull(true, "fieldName");
        assertEquals("fieldName is null", builder.build());
    }

    @Test
    void testIsNullUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNull(fieldFunction);
        assertEquals("fieldName is null", builder.build());
    }

    @Test
    void testIsNullConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNull(false, fieldFunction);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testIsNullConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNull(true, fieldFunction);
        assertEquals("fieldName is null", builder.build());
    }

    @Test
    void testIsNotNull() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNotNull("fieldName");
        assertEquals("fieldName is not null", builder.build());
    }

    @Test
    void testIsNotNullConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNotNull(false, "fieldName");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testIsNotNullConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.isNotNull(true, "fieldName");
        assertEquals("fieldName is not null", builder.build());
    }

    @Test
    void testIsNotNullUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNotNull(fieldFunction);
        assertEquals("fieldName is not null", builder.build());
    }

    @Test
    void testIsNotNullConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNotNull(false, fieldFunction);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testIsNotNullConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.isNotNull(true, fieldFunction);
        assertEquals("fieldName is not null", builder.build());
    }

    @Test
    void testInSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.in("status", List.of("active"));
        assertEquals("status in [\"active\"]", builder.build());
    }

    @Test
    void testInConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.in(false, "status", List.of("active"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testInConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.in(true, "status", List.of("active"));
        assertEquals("status in [\"active\"]", builder.build());
    }

    @Test
    void testInUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.in(fieldFunction, List.of("active", "inactive"));
        assertEquals("status in [\"active\", \"inactive\"]", builder.build());
    }

    @Test
    void testInConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.in(false, fieldFunction, List.of("active", "inactive"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testInConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.in(true, fieldFunction, List.of("active", "inactive"));
        assertEquals("status in [\"active\", \"inactive\"]", builder.build());
    }

    @Test
    void testNotInSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notIn("status", List.of("inactive"));
        assertEquals("not (status in [\"inactive\"])", builder.build());
    }

    @Test
    void testNotInConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notIn(false, "status", List.of("inactive"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotInConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notIn(true, "status", List.of("inactive"));
        assertEquals("not (status in [\"inactive\"])", builder.build());
    }

    @Test
    void testNotInUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.notIn(fieldFunction, List.of("active", "inactive"));
        assertEquals("not (status in [\"active\", \"inactive\"])", builder.build());
    }

    @Test
    void testNotInConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.notIn(false, fieldFunction, List.of("active", "inactive"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotInConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("status");

        builder.notIn(true, fieldFunction, List.of("active", "inactive"));
        assertEquals("not (status in [\"active\", \"inactive\"])", builder.build());
    }

    @Test
    void testLike() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.like("name", "value");
        assertEquals("name like \"%value%\"", builder.build());
    }

    @Test
    void testLikeConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.like(false, "name", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.like(true, "name", "value");
        assertEquals("name like \"%value%\"", builder.build());
    }

    @Test
    void testLikeUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.like(fieldFunction, "value");
        assertEquals("fieldName like \"%value%\"", builder.build());
    }

    @Test
    void testLikeConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.like(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.like(true, fieldFunction, "value");
        assertEquals("fieldName like \"%value%\"", builder.build());
    }

    @Test
    void testNotLike() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notLike("name", "value");
        assertEquals("not (name like \"%value%\")", builder.build());
    }

    @Test
    void testNotLikeConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notLike(false, "name", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotLikeConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notLike(true, "name", "value");
        assertEquals("not (name like \"%value%\")", builder.build());
    }

    @Test
    void testNotLikeUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.notLike(fieldFunction, "value");
        assertEquals("not (fieldName like \"%value%\")", builder.build());
    }

    @Test
    void testNotLikeConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.notLike(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotLikeConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.notLike(true, fieldFunction, "value");
        assertEquals("not (fieldName like \"%value%\")", builder.build());
    }

    @Test
    void testLikeLeft() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeLeft("name", "value");
        assertEquals("name like \"%value\"", builder.build());
    }

    @Test
    void testLikeLeftConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeLeft(false, "name", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeLeftConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeLeft(true, "name", "value");
        assertEquals("name like \"%value\"", builder.build());
    }

    @Test
    void testLikeLeftUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeLeft(fieldFunction, "value");
        assertEquals("fieldName like \"%value\"", builder.build());
    }

    @Test
    void testLikeLeftConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeLeft(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeLeftConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeLeft(true, fieldFunction, "value");
        assertEquals("fieldName like \"%value\"", builder.build());
    }

    @Test
    void testLikeRight() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeRight("name", "value");
        assertEquals("name like \"value%\"", builder.build());
    }

    @Test
    void testLikeRightConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeRight(false, "name", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeRightConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.likeRight(true, "name", "value");
        assertEquals("name like \"value%\"", builder.build());
    }

    @Test
    void testLikeRightUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeRight(fieldFunction, "value");
        assertEquals("fieldName like \"value%\"", builder.build());
    }

    @Test
    void testLikeRightConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeRight(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testLikeRightConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("fieldName");

        builder.likeRight(true, fieldFunction, "value");
        assertEquals("fieldName like \"value%\"", builder.build());
    }

    @Test
    void testJsonContains() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContains("data", "value");
        assertEquals("JSON_CONTAINS(data, \"value\")", builder.build());
    }

    @Test
    void testJsonContainsConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContains(false, "data", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContains(true, "data", "value");
        assertEquals("JSON_CONTAINS(data, \"value\")", builder.build());
    }

    @Test
    void testJsonContainsUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContains(fieldFunction, "value");
        assertEquals("JSON_CONTAINS(jsonField, \"value\")", builder.build());
    }

    @Test
    void testJsonContainsConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContains(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContains(true, fieldFunction, "value");
        assertEquals("JSON_CONTAINS(jsonField, \"value\")", builder.build());
    }

    @Test
    void testNotJsonContains() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notJsonContains("data", "value");
        assertEquals("not (JSON_CONTAINS(data, \"value\"))", builder.build());
    }

    @Test
    void testNotJsonContainsConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notJsonContains(false, "data", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotJsonContainsConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notJsonContains(true, "data", "value");
        assertEquals("not (JSON_CONTAINS(data, \"value\"))", builder.build());
    }

    @Test
    void testNotJsonContainsUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("data");

        builder.notJsonContains(fieldFunction, "value");
        assertEquals("not (JSON_CONTAINS(data, \"value\"))", builder.build());
    }

    @Test
    void testNotJsonContainsConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("data");

        builder.notJsonContains(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotJsonContainsConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("data");

        builder.notJsonContains(true, fieldFunction, "value");
        assertEquals("not (JSON_CONTAINS(data, \"value\"))", builder.build());
    }

    @Test
    void testJsonContainsAll() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAll("data", List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ALL(data, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAllConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAll(false, "data", List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsAllConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAll(true, "data", List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ALL(data, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAllUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAll(fieldFunction, List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ALL(jsonField, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAllConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAll(false, fieldFunction, List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsAllConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAll(true, fieldFunction, List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ALL(jsonField, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAny() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAny("data", List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ANY(data, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAnyConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAny(false, "data", List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsAnyConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.jsonContainsAny(true, "data", List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ANY(data, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAnyUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAny(fieldFunction, List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ANY(jsonField, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testJsonContainsAnyConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAny(false, fieldFunction, List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testJsonContainsAnyConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("jsonField");

        builder.jsonContainsAny(true, fieldFunction, List.of("value1", "value2"));
        assertEquals("JSON_CONTAINS_ANY(jsonField, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContains("items", "value");
        assertEquals("ARRAY_CONTAINS(items, \"value\")", builder.build());
    }

    @Test
    void testArrayContainsConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContains(false, "items", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContains(true, "items", "value");
        assertEquals("ARRAY_CONTAINS(items, \"value\")", builder.build());
    }

    @Test
    void testArrayContainsUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContains(fieldFunction, "value");
        assertEquals("ARRAY_CONTAINS(items, \"value\")", builder.build());
    }

    @Test
    void testArrayContainsConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContains(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContains(true, fieldFunction, "value");
        assertEquals("ARRAY_CONTAINS(items, \"value\")", builder.build());
    }

    @Test
    void testNotArrayContains() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notArrayContains("items", "value");
        assertEquals("not (ARRAY_CONTAINS(items, \"value\"))", builder.build());
    }

    @Test
    void testNotArrayContainsConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notArrayContains(false, "items", "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotArrayContainsConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.notArrayContains(true, "items", "value");
        assertEquals("not (ARRAY_CONTAINS(items, \"value\"))", builder.build());
    }

    @Test
    void testNotArrayContainsUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.notArrayContains(fieldFunction, "value");
        assertEquals("not (ARRAY_CONTAINS(items, \"value\"))", builder.build());
    }

    @Test
    void testNotArrayContainsConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.notArrayContains(false, fieldFunction, "value");
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotArrayContainsConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.notArrayContains(true, fieldFunction, "value");
        assertEquals("not (ARRAY_CONTAINS(items, \"value\"))", builder.build());
    }

    @Test
    void testArrayContainsAllWithValidValues() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAll("items", List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ALL(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAllConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAll(false, "items", List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsAllConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAll(true, "items", List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ALL(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAllUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAll(fieldFunction, List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ALL(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAllConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAll(false, fieldFunction, List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsAllConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAll(true, fieldFunction, List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ALL(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAny() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAny("items", List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ANY(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAnyConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAny(false, "items", List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsAnyConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayContainsAny(true, "items", List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ANY(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAnyUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAny(fieldFunction, List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ANY(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayContainsAnyConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAny(false, fieldFunction, List.of("value1", "value2"));
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayContainsAnyConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayContainsAny(true, fieldFunction, List.of("value1", "value2"));
        assertEquals("ARRAY_CONTAINS_ANY(items, [\"value1\", \"value2\"])", builder.build());
    }

    @Test
    void testArrayLengthSingleValue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayLength("items", 5);
        assertEquals("items.length() == 5", builder.build());
    }

    @Test
    void testArrayLengthConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayLength(false, "items", 5);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayLengthConditionTrue() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.arrayLength(true, "items", 5);
        assertEquals("items.length() == 5", builder.build());
    }

    @Test
    void testArrayLengthUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayLength(fieldFunction, 5);
        assertEquals("items.length() == 5", builder.build());
    }

    @Test
    void testArrayLengthConditionFalseUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayLength(false, fieldFunction, 5);
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testArrayLengthConditionTrueUsingFieldFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        FieldFunction<Object, ?> fieldFunction = mock(FieldFunction.class);
        when(fieldFunction.getFieldName(fieldFunction)).thenReturn("items");

        builder.arrayLength(true, fieldFunction, 5);
        assertEquals("items.length() == 5", builder.build());
    }

    @Test
    void testOrCombiningTwoConditions() {
        TestConditionBuilder builder1 = createBuilderInstance();
        builder1.eq("age", 25);

        builder1.or(builder2 -> builder2.ne("status", "inactive"));

        assertEquals("(age == 25 || status != \"inactive\")", builder1.build());
    }

    @Test
    void testOrConditionFalseCombiningTwoConditions() {
        TestConditionBuilder builder1 = createBuilderInstance();
        builder1.eq("age", 25);

        builder1.or(false, builder2 -> builder2.ne("status", "inactive"));

        assertEquals("age == 25", builder1.build());
    }

    @Test
    void testOrWithFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("age", 30);
        builder.or(b -> b.ne("status", "active"));

        assertEquals("(age == 30 || status != \"active\")", builder.build());
    }

    @Test
    void testOrConditionTrueWithFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.gt("score", 90);
        builder.or(true, b -> b.lt("score", 100));

        assertEquals("(score > 90 || score < 100)", builder.build());
    }

    @Test
    void testNotFiltersSingleCondition() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("name", "John");
        builder.not();
        assertEquals("not (name == \"John\")", builder.build());
    }

    @Test
    void testNotEmptyFilters() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.not();
        assertTrue(builder.build().isEmpty());
    }

    @Test
    void testNotCombiningMultipleFilters() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("age", 25).ne("status", "inactive");
        builder.not();
        assertEquals("not (age == 25 && status != \"inactive\")", builder.build());
    }

    @Test
    void testNotConditionFalse() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("name", "Paul");
        builder.not(false);
        assertEquals("name == \"Paul\"", builder.build());
    }

    @Test
    void testAndCombiningTwoConditions() {
        TestConditionBuilder builder1 = createBuilderInstance();
        builder1.eq("age", 25);

        builder1.and(builder2 -> builder2.ne("status", "inactive"));

        assertEquals("(age == 25 && status != \"inactive\")", builder1.build());
    }

    @Test
    void testAndConditionFalseCombiningTwoConditions() {
        TestConditionBuilder builder1 = createBuilderInstance();
        builder1.eq("age", 25);


        builder1.and(false, builder2 -> builder2.ne("status", "inactive"));

        assertEquals("age == 25", builder1.build());
    }

    @Test
    void testAndWithFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.eq("name", "John");
        builder.and(b -> b.gt("score", 85));

        assertEquals("(name == \"John\" && score > 85)", builder.build());
    }

    @Test
    void testAndConditionFalseWithFunction() {
        TestConditionBuilder builder = createBuilderInstance();
        builder.and(false, b -> b.eq("name", "John").gt("score", 85));

        assertTrue(builder.build().isEmpty());
    }

    static class TestConditionBuilder extends ConditionBuilder<Object, TestConditionBuilder> {
        @Override
        protected TestConditionBuilder createNewInstance() {
            return new TestConditionBuilder();
        }
    }
}