package io.github.xermaor.milvus.plus.core.conditions;

import com.google.gson.JsonObject;
import io.github.xermaor.milvus.plus.cache.PropertyCache;
import io.github.xermaor.milvus.plus.core.FieldFunction;
import io.github.xermaor.milvus.plus.exception.MilvusPlusException;
import io.github.xermaor.milvus.plus.util.GsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ConditionBuilder 用于条件构建和处理的工具类。
 * <p>
 * 提供链式调用的方式进行条件的构造，包括文本匹配、条件过滤器、字段值比较等操作。
 * 支持动态条件的添加和作用范围的管理。
 * <p>
 * 类的字段：
 * 1. FIELD_CACHE - 用于缓存字段元信息的映射。
 * 2. filters - 用于存储条件过滤器的集合。
 * 3. textMatches - 存储文本匹配条件的集合，用于查询构建。
 */
@SuppressWarnings("unchecked")
public abstract class ConditionBuilder<T, W extends ConditionBuilder<T, W>> {
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    protected final List<String> filters = new ArrayList<>();
    protected final List<String> textMatches = new ArrayList<>();

    // =============== 条件装饰器方法 ===============

    /**
     * 条件装饰器 - 根据条件决定是否执行操作
     * @param condition 条件判断
     * @param operation 要执行的操作
     * @return 当前条件构建器
     */
    private W conditionalExecute(boolean condition, Function<W, W> operation) {
        if (!condition) {
            return (W) this;
        }
        return operation.apply((W) this);
    }

    // =============== 反射工具方法 ===============

    /**
     * 获取对象属性映射，增加缓存和异常处理优化
     */
    protected Map<String, Object> getPropertiesMap(T entity) {
        if (entity == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> propertiesMap = new HashMap<>();
        Class<?> clazz = entity.getClass();
        // 获取所有字段（包括继承的）
        Collection<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    propertiesMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                throw new MilvusPlusException("Failed to access field: " + field.getName(), e);
            }
        }
        return propertiesMap;
    }

    /**
     * 将实体对象转换为对应的JsonObject表示形式。
     *
     * @param propertyCache 属性缓存对象，包含属性键值对映射及相关信息
     * @param entity 实体对象，用于生成JsonObject
     * @return 转换后的JsonObject表示形式
     */
    protected JsonObject toJsonObject(PropertyCache propertyCache, T entity) {
        Map<String, Object> propertiesMap = getPropertiesMap(entity);
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String tk = propertyCache.functionToPropertyMap.get(key);
            if (StringUtils.isNotEmpty(tk)) {
                GsonUtil.put(jsonObject, tk, value);
            }
        }
        return jsonObject;
    }

    /**
     * 递归获取类的所有字段
     */
    private Collection<Field> getAllFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> fields = new ArrayList<>();
            Class<?> currentClass = c;
            while (currentClass != null && currentClass != Object.class) {
                Collections.addAll(fields, currentClass.getDeclaredFields());
                currentClass = currentClass.getSuperclass();
            }
            return Collections.unmodifiableList(fields);
        });
    }


    /**
     * 添加一个文本匹配条件到查询中。
     *
     * @param fieldName 字段的名称，该字段需要进行文本匹配操作。
     * @param value 用于匹配的文本值，不能为空。
     * @return 当前实例，支持链式调用。
     */
    // =============== TEXT_MATCH 条件方法 ===============
    public W textMatch(String fieldName, String value) {
        validateFieldName(fieldName);
        validateNotEmpty(value, "文本匹配值不能为空");
        String match = String.format("TEXT_MATCH(%s, '%s')",
                wrapFieldName(fieldName), escapeValue(value));
        textMatches.add(match);
        return (W) this;
    }

    /**
     * 根据指定的条件，对给定字段名和值进行文本匹配操作。
     *
     * @param condition 一个布尔值，表示是否执行文本匹配操作。如果为 true，则进行匹配操作；否则跳过。
     * @param fieldName 字段名称，表示需要执行文本匹配的目标字段。
     * @param value 匹配的文本值，用于与字段的内容进行比较。
     * @return 返回当前对象实例，用于链式调用。
     */
    public W textMatch(boolean condition, String fieldName, String value) {
        return conditionalExecute(condition, w -> w.textMatch(fieldName, value));
    }

    public W textMatch(String fieldName, Collection<String> values) {
        validateFieldName(fieldName);
        validateNotEmpty(values, "文本匹配值列表不能为空");
        String joinedValues = values.stream()
                .map(this::escapeValue)
                .collect(Collectors.joining(" "));
        String match = String.format("TEXT_MATCH(%s, '%s')",
                wrapFieldName(fieldName), joinedValues);
        textMatches.add(match);
        return (W) this;
    }

    /**
     * 根据指定的条件，对指定字段的值进行文本匹配。
     *
     * @param condition 判断是否执行匹配操作的条件，true 表示执行匹配，false 表示不执行。
     * @param fieldName 需要进行文本匹配的字段名称。
     * @param values 用于匹配的文本值列表。
     * @return 返回操作后的对象实例。
     */
    public W textMatch(boolean condition, String fieldName, Collection<String> values) {
        return conditionalExecute(condition, w -> w.textMatch(fieldName, values));
    }

    /**
     * 根据字段生成匹配文本的条件。
     *
     * @param fieldFunction 字段的函数引用，用于定位需要匹配的字段
     * @param value 要匹配的文本值
     * @return 构建条件后的对象实例
     */
    public W textMatch(FieldFunction<T, ?> fieldFunction, String value) {
        return textMatch(getFieldName(fieldFunction), value);
    }

    /**
     * 根据指定条件执行文本匹配操作。
     *
     * @param condition 指定是否执行文本匹配操作的条件
     * @param fieldFunction 字段函数，用于指定需要匹配的字段
     * @param value 要匹配的文本值
     * @return 返回当前对象以支持链式调用
     */
    public W textMatch(boolean condition, FieldFunction<T, ?> fieldFunction, String value) {
        return conditionalExecute(condition, w -> w.textMatch(fieldFunction, value));
    }

    /**
     * 根据字段函数匹配指定文本值集合。
     *
     * @param fieldFunction 字段函数，用于指定需要匹配的字段
     * @param values 字符串列表，表示需要匹配的文本值集合
     * @return 当前对象实例，用于链式调用
     */
    public W textMatch(FieldFunction<T, ?> fieldFunction, Collection<String> values) {
        return textMatch(getFieldName(fieldFunction), values);
    }

    /**
     * 根据指定的条件、字段函数以及值列表进行文本匹配操作。
     *
     * @param condition 是否执行文本匹配的条件。如果为 true，则执行文本匹配操作；否则不执行。
     * @param fieldFunction 字段函数，指定需要进行匹配的字段。
     * @param values 文本匹配的值列表，用于确定匹配的内容。
     * @return 返回操作后的结果对象。
     */
    public W textMatch(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<String> values) {
        return conditionalExecute(condition, w -> w.textMatch(fieldFunction, values));
    }

    /**
     * 检查字段值是否等于指定值，生成过滤条件。
     *
     * @param fieldName 字段名称
     * @param value 字段值，用于与字段进行等值比较
     * @return 返回处理后的对象，包含该过滤条件
     */
    // =============== 基础比较操作 ===============
    public W eq(String fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    /**
     * 添加等于条件到当前查询中。
     *
     * @param condition 一个布尔值，表示是否添加该条件。如果为true，则添加等于条件；如果为false，则不添加条件。
     * @param fieldName 字段名称，指定要比较的字段。
     * @param value 值，指定与字段进行比较的值。
     * @return 当前对象，用于支持方法链调用。
     */
    public W eq(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.eq(fieldName, value));
    }

    /**
     * 判断字段是否等于指定值的条件方法。
     *
     * @param fieldFunction 字段的函数表示，用于指定字段
     * @param value 用于比较的值
     * @return 返回当前查询构造器对象，用于链式调用
     */
    public W eq(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, "==", value);
    }

    /**
     * 判断条件是否满足后执行等值判断的条件操作。
     *
     * @param condition 布尔值，表示是否执行后续条件操作
     * @param fieldFunction 字段函数，表示需要比较的字段
     * @param value 用于与字段值进行比较的值
     * @return 返回当前对象以支持链式调用
     */
    public W eq(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.eq(fieldFunction, value));
    }

    /**
     * 添加不等于条件过滤器的方法。
     *
     * @param fieldName 字段名称，用于指定需要比较的字段。
     * @param value 比较值，用于指定字段的比较目标值。
     * @return 返回当前对象实例，用于链式调用。
     */
    public W ne(String fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    /**
     * 添加不等于条件到查询中。
     *
     * @param condition 条件为 true 时才会应用不等于查询条件
     * @param fieldName 字段名称
     * @param value     不等于字段的值
     * @return 当前对象本身，用于方法链调用
     */
    public W ne(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.ne(fieldName, value));
    }

    /**
     * 添加不等条件的过滤器。
     *
     * @param fieldFunction 字段的函数引用，用于指定需要比较的字段
     * @param value 比较的值
     * @return 更新后的过滤器条件对象
     */
    public W ne(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, "!=", value);
    }

    /**
     * 判断条件为 true 时，对指定字段应用不等于操作。
     *
     * @param condition 条件，决定是否执行操作
     * @param fieldFunction 字段的函数式接口，用于指定需要操作的字段
     * @param value 字段的比较值
     * @return 更新后的当前对象
     */
    public W ne(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.ne(fieldFunction, value));
    }

    /**
     * 添加大于条件的过滤器。
     *
     * @param fieldName 字段名称，指定要进行比较的字段。
     * @param value 字段值，用于与字段进行大于比较的值。
     * @return 返回当前过滤器对象，用于链式调用。
     */
    public W gt(String fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    /**
     * 用于在条件满足时生成大于 (greater than) 的查询条件。
     *
     * @param condition 判断是否执行该条件的方法，true 表示执行，false 表示不执行
     * @param fieldName 字段名称，用于指定比较操作的字段
     * @param value 字段值，用于与字段名称对应的值进行比较
     * @return 返回执行后的查询或关联的对象结果
     */
    public W gt(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.gt(fieldName, value));
    }

    /**
     * 添加大于条件的过滤器。
     *
     * @param fieldFunction 字段函数，用于指定字段
     * @param value 比较的值
     * @return 修改后的过滤器条件
     */
    public W gt(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, ">", value);
    }

    /**
     * 用于生成大于条件的查询.
     *
     * @param condition 决定是否执行该条件的布尔值
     * @param fieldFunction 字段函数，用于指定字段
     * @param value 用于比较的值
     * @return 返回当前对象实例
     */
    public W gt(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.gt(fieldFunction, value));
    }

    /**
     * 添加一个筛选条件，指定字段必须大于或等于给定的值。
     *
     * @param fieldName 字段名称
     * @param value 字段对应的值
     * @return 当前对象以支持链式调用
     */
    public W ge(String fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    /**
     * 判断是否执行 "大于等于" 条件操作。
     *
     * @param condition 一个布尔值，表示是否需要执行此操作
     * @param fieldName 字段名称，用于指定条件作用的字段
     * @param value 用于比较的值
     * @return 返回当前对象，通过级联调用以支持链式操作
     */
    public W ge(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.ge(fieldName, value));
    }

    /**
     * 添加一个大于等于条件的过滤器。
     *
     * @param fieldFunction 字段的函数引用，用于指定需要比较的字段
     * @param value 比较的值
     * @return 返回当前对象实例，允许链式调用
     */
    public W ge(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, ">=", value);
    }

    /**
     * 在指定条件满足时，添加字段大于或等于指定值的查询条件。
     *
     * @param condition 是否执行该条件操作
     * @param fieldFunction 字段的Lambda函数
     * @param value 与字段比较的值
     * @return 当前操作对象
     */
    public W ge(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.ge(fieldFunction, value));
    }

    /**
     * 添加小于条件过滤的方法。
     *
     * @param fieldName 字段名称，用于指定需要比较的字段
     * @param value 字段值，用于指定与字段进行比较的值
     * @return 返回包含新过滤条件的当前对象
     */
    public W lt(String fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    /**
     * 如果条件为真，则执行小于（less than）操作。
     *
     * @param condition 判断条件，只有为 true 时才会执行小于操作
     * @param fieldName 字段名称，用于指定参与比较的字段
     * @param value     输入的值，与指定字段进行比较
     * @return 返回当前对象实例，便于链式调用
     */
    public W lt(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.lt(fieldName, value));
    }

    /**
     * 构建小于条件的过滤器。
     *
     * @param fieldFunction 字段对应的函数引用
     * @param value 与字段比较的值
     * @return 构造好的过滤器对象
     */
    public W lt(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, "<", value);
    }

    /**
     * 用于根据给定的条件将小于条件的约束应用到查询中。
     *
     * @param condition 条件值，指定是否执行该方法中的逻辑
     * @param fieldFunction 字段映射函数，用于指定查询中比较的字段
     * @param value 被比较的值
     * @return 返回当前查询构造器实例，支持链式调用
     */
    public W lt(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.lt(fieldFunction, value));
    }

    /**
     * 添加小于或等于 (<=) 的过滤条件。
     *
     * @param fieldName 字段名称
     * @param value 字段的值
     * @return 当前对象，便于链式调用
     */
    public W le(String fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    /**
     * 添加小于等于 (<=) 条件到查询中。
     *
     * @param condition 是否添加条件的布尔值，如果为 false，则不执行此条件。
     * @param fieldName 字段名称，用于指定比较的字段。
     * @param value 字段的比较值。
     * @return 当前查询对象，支持链式调用。
     */
    public W le(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.le(fieldName, value));
    }

    /**
     * 构建小于等于 (<=) 条件的过滤器方法。
     *
     * @param fieldFunction 字段的函数表达式，指定需要比较的字段
     * @param value         比较的值
     * @return 返回构造好的过滤器条件对象
     */
    public W le(FieldFunction<T, ?> fieldFunction, Object value) {
        return addFilter(fieldFunction, "<=", value);
    }

    /**
     * 添加小于或等于 (less than or equal) 的查询条件。
     *
     * @param condition 条件是否成立，只有为 true 时才会加入该查询条件
     * @param fieldFunction 字段的函数引用，用于指定查询的字段
     * @param value 用于比较的值
     * @return 构建条件后的当前对象
     */
    public W le(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.le(fieldFunction, value));
    }

    /**
     * 添加一个范围条件，指定字段值需在给定的范围内，范围包含开始值和结束值。
     *
     * @param fieldName 字段名称，不能为空或不合法。
     * @param start     范围的开始值，不能为空。
     * @param end       范围的结束值，不能为空。
     * @return 返回当前对象，以便支持链式调用。
     */
    // =============== 范围和空值检查 ===============
    public W between(String fieldName, Object start, Object end) {
        validateFieldName(fieldName);
        validateNotNull(start, "范围开始值不能为空");
        validateNotNull(end, "范围结束值不能为空");
        String wrappedField = wrapFieldName(fieldName);
        String filter = String.format("%s >= %s AND %s <= %s",
                wrappedField, convertValue(start), wrappedField, convertValue(end));
        filters.add(filter);
        return (W) this;
    }

    /**
     * 构建一个 BETWEEN 查询语句，根据指定的条件执行。
     *
     * @param condition 判断是否执行 BETWEEN 查询的条件
     * @param fieldName 字段名，指定对哪个字段进行 BETWEEN 查询
     * @param start 起始值，用于指定范围的开始
     * @param end 结束值，用于指定范围的结束
     * @return 返回当前查询条件构建对象
     */
    public W between(boolean condition, String fieldName, Object start, Object end) {
        return conditionalExecute(condition, w -> w.between(fieldName, start, end));
    }

    /**
     * 用于生成条件，判断字段值是否位于指定的范围之间（包含开始值和结束值）。
     *
     * @param fieldFunction 字段函数，指定要判断的字段
     * @param start 范围的开始值
     * @param end 范围的结束值
     * @return 返回类型为W，表示该条件的构造结果
     */
    public W between(FieldFunction<T, ?> fieldFunction, Object start, Object end) {
        return between(getFieldName(fieldFunction), start, end);
    }

    /**
     * 在指定字段上添加范围查询条件，用于判断字段值是否在起止范围内。
     *
     * @param condition 条件是否生效，true 表示执行范围查询，false 表示忽略该条件
     * @param fieldFunction 字段的函数表达式，用于指定要查询的字段
     * @param start 范围查询的起始值
     * @param end 范围查询的结束值
     * @return 返回更新后的对象，以支持链式调用
     */
    public W between(boolean condition, FieldFunction<T, ?> fieldFunction, Object start, Object end) {
        return conditionalExecute(condition, w -> w.between(fieldFunction, start, end));
    }

    /**
     * 添加一个条件以排除指定字段的值在给定范围之间的数据。
     *
     * @param fieldName 字段的名称
     * @param start 范围的起始值
     * @param end 范围的结束值
     * @return 返回当前查询条件构造器实例
     */
    public W notBetween(String fieldName, Object start, Object end) {
        return not(builder -> builder.between(fieldName, start, end));
    }

    /**
     * 在指定条件下，生成一个表示字段值不在给定范围内的查询条件。
     *
     * @param condition 条件是否成立，如果为 true 则生成“不在...范围内”的查询条件，否则不执行操作。
     * @param fieldName 字段名，用于表示查询条件中涉及的字段。
     * @param start 范围的起始值。
     * @param end 范围的结束值。
     * @return 返回当前查询构造器实例（通常是一个可链式调用的对象）。
     */
    public W notBetween(boolean condition, String fieldName, Object start, Object end) {
        return conditionalExecute(condition, w -> w.notBetween(fieldName, start, end));
    }

    /**
     * 构造一个条件，用于检查指定字段的值不在给定的范围内（不包括范围的两端）。
     *
     * @param fieldFunction 表示要检查的字段的函数
     * @param start 范围的起始值
     * @param end 范围的结束值
     * @return 当前查询条件构造器对象，便于链式调用
     */
    public W notBetween(FieldFunction<T, ?> fieldFunction, Object start, Object end) {
        return not(builder -> builder.between(fieldFunction, start, end));
    }

    /**
     * 用于判断指定字段的值是否不在给定的范围之内。
     *
     * @param condition 判断这个方法是否需要执行的条件，true 表示执行，false 表示不执行
     * @param fieldFunction 表示字段的函数式接口
     * @param start 范围的起始值
     * @param end 范围的结束值
     * @return 返回执行条件后的结果对象
     */
    public W notBetween(boolean condition, FieldFunction<T, ?> fieldFunction, Object start, Object end) {
        return conditionalExecute(condition, w -> w.notBetween(fieldFunction, start, end));
    }

    /**
     * 添加字段为空值的过滤条件。
     *
     * @param fieldName 字段名称
     * @return 返回当前调用链的对象实例
     */
    public W isNull(String fieldName) {
        validateFieldName(fieldName);
        filters.add(wrapFieldName(fieldName) + " IS NULL");
        return (W) this;
    }

    /**
     * 判断指定字段是否为 null。
     *
     * @param condition 判断的条件，只有当条件为 true 时才执行操作
     * @param fieldName 需要判断是否为 null 的字段名称
     * @return 返回当前调用链的对象实例
     */
    public W isNull(boolean condition, String fieldName) {
        return conditionalExecute(condition, w -> w.isNull(fieldName));
    }

    /**
     * 判断指定字段是否为NULL。
     *
     * @param fieldFunction 字段的函数式接口，用于指定要判断是否为NULL的字段
     * @return 返回当前调用链的对象实例
     */
    public W isNull(FieldFunction<T, ?> fieldFunction) {
        return isNull(getFieldName(fieldFunction));
    }

    /**
     * 检查指定字段是否为NULL。
     *
     * @param condition 条件布尔值，决定是否执行该方法。
     * @param fieldFunction 字段函数，用于指定需要检查的字段。
     * @return 返回当前调用链的对象实例。
     */
    public W isNull(boolean condition, FieldFunction<T, ?> fieldFunction) {
        return conditionalExecute(condition, w -> w.isNull(fieldFunction));
    }

    /**
     * 判断指定字段是否不为NULL，并将对应的筛选条件添加到过滤器中。
     *
     * @param fieldName 字段名称，不允许为null或空字符串。
     * @return 返回当前调用链的对象实例
     */
    public W isNotNull(String fieldName) {
        validateFieldName(fieldName);
        filters.add(wrapFieldName(fieldName) + " IS NOT NULL");
        return (W) this;
    }

    /**
     * 判断指定字段是否为非空值。
     *
     * @param condition 判断条件的布尔值，如果为 true 则执行检查，否则跳过。
     * @param fieldName 需要检查非空的字段名称。
     * @return 返回当前调用链的对象实例
     */
    public W isNotNull(boolean condition, String fieldName) {
        return conditionalExecute(condition, w -> w.isNotNull(fieldName));
    }

    /**
     * 判断指定字段是否不为空。
     *
     * @param fieldFunction 字段的函数表达式，用于传入需要判断的字段
     * @return 返回当前调用链的对象实例
     */
    public W isNotNull(FieldFunction<T, ?> fieldFunction) {
        return isNotNull(getFieldName(fieldFunction));
    }

    /**
     * 检查指定字段是否为非空，并根据条件执行相关操作。
     *
     * @param condition 条件值，决定是否执行操作
     * @param fieldFunction 字段的函数式接口，指定需要检查的字段
     * @return 返回当前调用链的对象实例
     */
    public W isNotNull(boolean condition, FieldFunction<T, ?> fieldFunction) {
        return conditionalExecute(condition, w -> w.isNotNull(fieldFunction));
    }

    /**
     * 添加 IN 操作条件到查询过滤器中。
     *
     * @param fieldName 字段名称，表示需要进行 IN 操作的字段
     * @param values 值列表，表示 IN 操作中的值集合
     * @return 返回当前调用链的对象实例
     */
    // =============== IN 和 LIKE 操作 ===============
    public W in(String fieldName, Collection<?> values) {
        validateFieldName(fieldName);
        validateNotEmpty(values, "IN 操作的值列表不能为空");
        String valueList = convertValues(values);
        filters.add(wrapFieldName(fieldName) + " IN " + valueList);
        return (W) this;
    }

    /**
     * 根据指定条件对字段进行包含范围的条件操作。
     *
     * @param condition 是否执行条件操作的布尔值
     * @param fieldName 需要进行条件操作的字段名称
     * @param values    字段需要匹配的值的列表
     * @return 返回当前调用链的对象实例
     */
    public W in(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.in(fieldName, values));
    }

    /**
     * 构建一个条件，用于判断字段的值是否在指定的值列表中。
     *
     * @param fieldFunction 字段的函数式表达，用于获取字段名
     * @param values 值列表，用于进行匹配
     * @return 返回当前调用链的对象实例
     */
    public W in(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return in(getFieldName(fieldFunction), values);
    }

    /**
     * 在指定条件下应用 IN 查询条件。
     *
     * @param condition 是否执行该条件，true 表示执行，false 表示不执行
     * @param fieldFunction 字段函数，用于指定操作的字段
     * @param values 用于指定 IN 查询的值列表
     * @return 返回当前调用链的对象实例
     */
    public W in(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.in(fieldFunction, values));
    }

    /**
     * 构造一个查询条件，表示字段的值不在给定的值列表中。
     *
     * @param fieldName 字段名称。
     * @param values 值列表，字段的值不能在此列表中。
     * @return 返回当前调用链的对象实例
     */
    public W notIn(String fieldName, Collection<?> values) {
        return not(builder -> builder.in(fieldName, values));
    }

    /**
     * 根据指定的条件，将字段值不在给定列表中的约束添加到查询中。
     *
     * @param condition 判断是否执行此操作的条件，当为 true 时执行，false 时跳过
     * @param fieldName 字段名称，用于指定要判断的数据库字段
     * @param values    值列表，字段值不应在此列表中
     * @return 返回当前调用链的对象实例
     */
    public W notIn(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.notIn(fieldName, values));
    }

    /**
     * 用于构建一个表示字段值不在给定列表中的条件。
     *
     * @param fieldFunction 一个字段函数，用于指定需要进行条件判断的字段
     * @param values 一个列表，包含不允许的字段值
     * @return 返回当前调用链的对象实例
     */
    public W notIn(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return not(builder -> builder.in(fieldFunction, values));
    }

    /**
     * 用于构建条件为“NOT IN”的查询语句。
     *
     * @param condition 条件是否成立，只有当条件为 true 时，才会执行“NOT IN”操作
     * @param fieldFunction 用于指定字段的函数
     * @param values 需要排除的值列表
     * @return 返回当前调用链的对象实例
     */
    public W notIn(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.notIn(fieldFunction, values));
    }

    /**
     * 添加一个模糊查询条件，字段名和查询值必须符合规范。
     *
     * @param fieldName 字段名，用于指定查询的字段
     * @param value 模糊查询的值，不能为空
     * @return 返回当前调用链的对象实例
     */
    public W like(String fieldName, String value) {
        validateFieldName(fieldName);
        validateNotEmpty(value, "LIKE 操作的值不能为空");
        filters.add(String.format("%s LIKE '%%%s%%'",
                wrapFieldName(fieldName), escapeValue(value)));
        return (W) this;
    }

    /**
     * 在指定条件成立时，对字段应用模糊查询。
     *
     * @param condition 判断条件，只有在此条件为真时，才会执行模糊查询操作
     * @param fieldName 字段名，要进行模糊查询的字段
     * @param value     查询条件的值
     * @return 返回当前调用链的对象实例
     */
    public W like(boolean condition, String fieldName, String value) {
        return conditionalExecute(condition, w -> w.like(fieldName, value));
    }

    /**
     * 根据指定的字段和值进行模糊查询。
     *
     * @param fieldFunction 用于获取字段名称的函数
     * @param value 用于模糊匹配的值
     * @return 返回当前调用链的对象实例
     */
    public W like(FieldFunction<T, ?> fieldFunction, String value) {
        return like(getFieldName(fieldFunction), value);
    }

    /**
     * 根据条件构建模糊查询条件。
     *
     * @param condition 判断是否执行条件的布尔值
     * @param fieldFunction 指定字段的函数
     * @param value 模糊查询匹配的值
     * @return 返回当前调用链的对象实例
     */
    public W like(boolean condition, FieldFunction<T, ?> fieldFunction, String value) {
        return conditionalExecute(condition, w -> w.like(fieldFunction, value));
    }

    /**
     * 用于添加一个 "NOT LIKE" 条件到查询中。
     *
     * @param fieldName 字段的名称，用于指定需要应用 "NOT LIKE" 条件的字段。
     * @param value 字段的值，用于指定 "NOT LIKE" 条件的匹配模式，通常包含通配符。
     * @return 返回当前调用链的对象实例
     */
    public W notLike(String fieldName, String value) {
        return not(builder -> builder.like(fieldName, value));
    }

    /**
     * 根据条件添加字段不匹配的查询条件。
     *
     * @param condition 判断是否执行条件的布尔值
     * @param fieldName 要进行不匹配查询的字段名称
     * @param value 用于匹配的值
     * @return 返回当前对象以支持链式调用
     */
    public W notLike(boolean condition, String fieldName, String value) {
        return conditionalExecute(condition, w -> w.notLike(fieldName, value));
    }

    /**
     * 添加一个“NOT LIKE”条件到查询中，用于指定字段值不符合给定的模式。
     *
     * @param fieldFunction 字段的函数引用，用于解析字段。
     * @param value 用于比较的模式字符串。
     * @return 返回当前调用链的对象实例
     */
    public W notLike(FieldFunction<T, ?> fieldFunction, String value) {
        return not(builder -> builder.like(fieldFunction, value));
    }

    /**
     * 根据指定条件对查询添加 NOT LIKE 语句。
     *
     * @param condition 条件是否满足，若为 true，则执行 NOT LIKE 操作，否则忽略该操作
     * @param fieldFunction 字段的函数，用于指定 NOT LIKE 操作的字段
     * @param value 模式字符串，表示 NOT LIKE 的匹配值
     * @return 返回当前调用链的对象实例
     */
    public W notLike(boolean condition, FieldFunction<T, ?> fieldFunction, String value) {
        return conditionalExecute(condition, w -> w.notLike(fieldFunction, value));
    }

    /**
     * 在指定字段上添加左匹配的 LIKE 筛选条件。
     *
     * @param fieldName 字段名，需要保证字段名合法且非空
     * @param value 匹配值，不能为空
     * @return 返回当前调用链的对象实例
     */
    public W likeLeft(String fieldName, String value) {
        validateFieldName(fieldName);
        validateNotEmpty(value, "LIKE 操作的值不能为空");
        filters.add(String.format("%s LIKE '%s%%'",
                wrapFieldName(fieldName), escapeValue(value)));
        return (W) this;
    }

    /**
     * 执行左匹配的条件查询操作。
     *
     * @param condition 判断是否执行该操作的条件，true 表示执行，false 表示不执行
     * @param fieldName 字段名称，用于指定进行匹配的列
     * @param value 匹配的值，将在 SQL 中生成左匹配的查询条件
     * @return 返回操作完成后的对象，用于支持链式调用
     */
    public W likeLeft(boolean condition, String fieldName, String value) {
        return conditionalExecute(condition, w -> w.likeLeft(fieldName, value));
    }

    /**
     * 构造一个左匹配的模糊查询条件。
     *
     * @param fieldFunction 用于获取字段名称的函数
     * @param value 查询值，将会被拼接为左匹配的模糊查询格式
     * @return 当前查询条件构造器实例
     */
    public W likeLeft(FieldFunction<T, ?> fieldFunction, String value) {
        return likeLeft(getFieldName(fieldFunction), value);
    }

    /**
     * 根据指定条件执行左匹配的模糊查询操作。
     *
     * @param condition      判断是否执行操作的条件，true 时执行操作，false 时不执行。
     * @param fieldFunction  获取字段的函数表达式，指定需要进行匹配的字段。
     * @param value          匹配的值，会在值前添加通配符进行左匹配查询。
     * @return 如果条件为 true，则返回执行操作后的查询对象；否则返回当前查询对象。
     */
    public W likeLeft(boolean condition, FieldFunction<T, ?> fieldFunction, String value) {
        return conditionalExecute(condition, w -> w.likeLeft(fieldFunction, value));
    }

    /**
     * 添加一个右模糊匹配（LIKE）过滤条件。
     *
     * @param fieldName 字段名称，用于指定数据库表中的列。
     * @param value 匹配值，将会用于构建右模糊查询条件。
     * @return 当前对象自身，用于支持链式调用。
     */
    public W likeRight(String fieldName, String value) {
        validateFieldName(fieldName);
        validateNotEmpty(value, "LIKE 操作的值不能为空");
        filters.add(String.format("%s LIKE '%%%s'",
                wrapFieldName(fieldName), escapeValue(value)));
        return (W) this;
    }

    /**
     * 在满足条件的情况下，向查询中添加右匹配的模糊查询条件。
     *
     * @param condition 判断是否执行该操作的布尔条件
     * @param fieldName 要匹配的字段名称
     * @param value 匹配的值
     * @return 修改后的查询对象
     */
    public W likeRight(boolean condition, String fieldName, String value) {
        return conditionalExecute(condition, w -> w.likeRight(fieldName, value));
    }

    /**
     * 构造一个字段右匹配（以指定值开头）的条件。
     *
     * @param fieldFunction 提供字段的函数式接口
     * @param value 用于匹配的字符串值
     * @return 当前查询条件的更新对象
     */
    public W likeRight(FieldFunction<T, ?> fieldFunction, String value) {
        return likeRight(getFieldName(fieldFunction), value);
    }

    /**
     * 在满足条件时，构造一个以指定值为右匹配的条件。
     *
     * @param condition 条件是否满足，为 true 时才会执行匹配逻辑
     * @param fieldFunction 字段字段函数，用于指定操作的字段
     * @param value 用于右匹配的字符串值
     * @return 当前查询构造器对象，支持链式调用
     */
    public W likeRight(boolean condition, FieldFunction<T, ?> fieldFunction, String value) {
        return conditionalExecute(condition, w -> w.likeRight(fieldFunction, value));
    }

    /**
     * 检查 JSON 字段中是否包含指定的值。
     *
     * @param fieldName JSON 对象中的字段名称
     * @param value 需要检查的值，可以是字符串、数字或其他支持的类型
     * @return 返回当前对象，用于方法链调用
     */
    // =============== JSON 操作 ===============
    public W jsonContains(String fieldName, Object value) {
        return addFunction("JSON_CONTAINS", fieldName, value);
    }

    /**
     * 判断指定条件是否成立，若成立则在 JSON 中判断指定字段是否包含给定值。
     *
     * @param condition 判断条件，决定是否执行后续操作
     * @param fieldName JSON 字段的名称
     * @param value 用于检查是否存在于 JSON 字段中的值
     * @return 当前对象实例以支持链式调用
     */
    public W jsonContains(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.jsonContains(fieldName, value));
    }

    /**
     * 检查给定字段的JSON数据中是否包含指定的值。
     *
     * @param fieldFunction 用于指定字段的函数
     * @param value 要检查的值
     * @return 配置了条件的当前对象
     */
    public W jsonContains(FieldFunction<T, ?> fieldFunction, Object value) {
        return jsonContains(getFieldName(fieldFunction), value);
    }

    /**
     * 判断指定的JSON字段是否包含给定的值，并在条件为真时执行相关操作。
     *
     * @param condition 判断是否执行操作的布尔条件
     * @param fieldFunction 用于指定JSON字段的函数
     * @param value JSON字段中需要判断是否包含的值
     * @return 修改后的对象实例
     */
    public W jsonContains(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.jsonContains(fieldFunction, value));
    }

    /**
     * 用于制定条件，表示字段中不包含指定的JSON值。
     *
     * @param fieldName 字段名称
     * @param value 要匹配的JSON值
     * @return 更新后的条件构造器
     */
    public W notJsonContains(String fieldName, Object value) {
        return not(builder -> builder.jsonContains(fieldName, value));
    }

    /**
     * 检查指定字段的JSON中是否不包含给定值，如果满足条件则执行。
     *
     * @param condition 判断是否执行的条件，true时执行操作，false时不执行
     * @param fieldName 要检查的JSON字段名称
     * @param value 要检查是否不存在于JSON字段中的值
     * @return 当前操作的对象实例
     */
    public W notJsonContains(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.notJsonContains(fieldName, value));
    }

    /**
     * 添加一个条件以确保指定字段的 JSON 数据不包含指定的值。
     *
     * @param fieldFunction 指定目标字段的函数引用
     * @param value 要检查字段 JSON 数据中是否不包含的值
     * @return 返回当前构造器对象以支持链式调用
     */
    public W notJsonContains(FieldFunction<T, ?> fieldFunction, Object value) {
        return not(builder -> builder.jsonContains(fieldFunction, value));
    }

    /**
     * 检查指定字段的值是否不包含目标值，并根据条件决定是否执行操作。
     *
     * @param condition 条件是否执行此方法，true 表示执行，false 表示不执行
     * @param fieldFunction 字段的函数式接口，用于指定需要操作的字段
     * @param value 检查字段值是否不包含的目标值
     * @return 当前对象自身，用于链式调用
     */
    public W notJsonContains(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.notJsonContains(fieldFunction, value));
    }

    /**
     * 添加一个 JSON_CONTAINS_ALL 函数来检查指定 JSON 字段是否包含所有指定的值。
     *
     * @param fieldName 要检查的 JSON 字段的名称
     * @param values 要检查的值列表
     * @return 返回当前对象以支持方法链调用
     */
    public W jsonContainsAll(String fieldName, Collection<?> values) {
        return addFunction("JSON_CONTAINS_ALL", fieldName, values);
    }

    /**
     * 判断JSON字段是否包含提供的所有值，并在满足条件时执行相应操作。
     *
     * @param condition 条件是否满足的布尔值，决定是否执行操作
     * @param fieldName JSON字段的名称
     * @param values 需要校验是否包含于JSON字段中的值列表
     * @return 返回当前对象，用于支持方法链调用
     */
    public W jsonContainsAll(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.jsonContainsAll(fieldName, values));
    }

    /**
     * 判断指定字段的 JSON 数据是否包含指定列表中的所有值。
     *
     * @param fieldFunction 用于指定字段的函数
     * @param values 要检查的值列表
     * @return 返回操作结果的包装对象
     */
    public W jsonContainsAll(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return jsonContainsAll(getFieldName(fieldFunction), values);
    }

    /**
     * 判断指定字段的JSON数据是否包含指定列表中的所有值。
     *
     * @param condition 一个布尔值，用于控制是否执行该操作
     * @param fieldFunction 字段函数，用于指定需要判断的字段
     * @param values 包含需要检查的值的列表
     * @return 返回当前对象本身，用于链式调用
     */
    public W jsonContainsAll(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.jsonContainsAll(fieldFunction, values));
    }

    /**
     * 判断指定字段的 JSON 数据中是否包含任意指定的值。
     *
     * @param fieldName JSON 字段的名称
     * @param values 要匹配的值列表
     * @return 返回当前对象实例
     */
    public W jsonContainsAny(String fieldName, Collection<?> values) {
        return addFunction("JSON_CONTAINS_ANY", fieldName, values);
    }

    /**
     * 检查目标字段的值中是否包含提供的任意值。
     *
     * @param condition 条件，若为 true 则执行方法，若为 false 则跳过。
     * @param fieldName 要检查的字段名称。
     * @param values 一个列表，包含要匹配的值。
     * @return 返回当前实例，允许方法链调用。
     */
    public W jsonContainsAny(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.jsonContainsAny(fieldName, values));
    }

    /**
     * 检查JSON字段中是否包含列表中的任意值。
     *
     * @param fieldFunction 字段函数，用于指定需要检查的字段。
     * @param values 值的列表，用于和JSON字段进行匹配检查。
     * @return 返回当前对象，支持链式调用。
     */
    public W jsonContainsAny(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return jsonContainsAny(getFieldName(fieldFunction), values);
    }

    /**
     * 检查指定字段的JSON是否包含给定列表中的任意一个值，并根据条件执行相应操作。
     *
     * @param condition 控制执行操作的条件。如果为true，则执行操作；否则不执行。
     * @param fieldFunction 用于指定JSON字段的函数表达式。
     * @param values 包含要检查的值的列表。
     * @return 返回链式操作的当前对象。
     */
    public W jsonContainsAny(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.jsonContainsAny(fieldFunction, values));
    }

    /**
     * 检查指定的数组字段中是否包含指定的值。
     *
     * @param fieldName 数组字段的名称
     * @param value 要检查的值
     * @return 当前对象，便于链式调用
     */
    // =============== 数组操作 ===============
    public W arrayContains(String fieldName, Object value) {
        return addFunction("ARRAY_CONTAINS", fieldName, value);
    }

    /**
     * 检查指定条件是否满足，如果满足则执行数组包含操作。
     *
     * @param condition 判断操作是否执行的条件
     * @param fieldName 要检查的字段名称
     * @param value 检查是否包含的值
     * @return 执行操作后的对象
     */
    public W arrayContains(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.arrayContains(fieldName, value));
    }

    /**
     * 判断指定字段的数组是否包含某个值。
     *
     * @param fieldFunction 用于获取字段名的函数
     * @param value         用于匹配的目标值
     * @return 返回更新后的查询实例
     */
    public W arrayContains(FieldFunction<T, ?> fieldFunction, Object value) {
        return arrayContains(getFieldName(fieldFunction), value);
    }

    /**
     * 判断指定字段的值中是否包含给定的值。
     *
     * @param condition 一个布尔值，用于决定是否执行该方法；为 true 时执行，为 false 时不执行。
     * @param fieldFunction 字段函数，用于指定操作的字段。
     * @param value 要检查是否包含在字段值中的目标值。
     * @return 如果 condition 为 true，则返回操作结果；如果为 false，则直接返回当前对象。
     */
    public W arrayContains(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.arrayContains(fieldFunction, value));
    }

    /**
     * 用于检查指定字段的值不包含特定的数组元素。
     *
     * @param fieldName 字段名称
     * @param value 要检查不包含的值
     * @return 更新后的对象实例
     */
    public W notArrayContains(String fieldName, Object value) {
        return not(builder -> builder.arrayContains(fieldName, value));
    }

    /**
     * 检查指定数组字段中是否不包含特定值，并根据条件执行相应逻辑。
     *
     * @param condition 条件是否满足的布尔值，只有在为 true 时才执行操作
     * @param fieldName 字段名称，表示数组字段的名称
     * @param value     数组字段中待检查是否不存在的值
     * @return 返回包含更新逻辑后的对象
     */
    public W notArrayContains(boolean condition, String fieldName, Object value) {
        return conditionalExecute(condition, w -> w.notArrayContains(fieldName, value));
    }

    /**
     * 检查指定字段的数组是否不包含指定的值。
     *
     * @param fieldFunction 字段函数，用于指定目标字段
     * @param value 需要检查是否不存在于字段数组中的值
     * @return 一个构建器对象，用于链式调用后续条件
     */
    public W notArrayContains(FieldFunction<T, ?> fieldFunction, Object value) {
        return not(builder -> builder.arrayContains(fieldFunction, value));
    }

    /**
     * 用于判断给定条件下，指定字段的值是否不包含在数组中。
     *
     * @param condition 判断是否执行逻辑的条件
     * @param fieldFunction 用于获取字段的方法引用
     * @param value 用于判断的数组值
     * @return 如果条件满足，将返回操作后的对象，否则返回当前对象
     */
    public W notArrayContains(boolean condition, FieldFunction<T, ?> fieldFunction, Object value) {
        return conditionalExecute(condition, w -> w.notArrayContains(fieldFunction, value));
    }

    /**
     * 检查指定数组字段是否包含指定列表中的所有元素。
     *
     * @param fieldName 字段名称，表示需要检查的数组字段。
     * @param values 值列表，表示需要检查的目标元素集合。
     * @return 当前对象，用于链式调用。
     */
    public W arrayContainsAll(String fieldName, Collection<?> values) {
        return addFunction("ARRAY_CONTAINS_ALL", fieldName, values);
    }

    /**
     * 检查数组字段是否包含指定的所有值，并根据条件执行操作。
     *
     * @param condition 判断是否执行操作的条件
     * @param fieldName 数组字段的名称
     * @param values    要检查的值列表
     * @return 若条件满足则返回操作结果，否则返回当前对象
     */
    public W arrayContainsAll(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.arrayContainsAll(fieldName, values));
    }

    /**
     * 判断数组是否包含指定的所有值。
     *
     * @param fieldFunction 一个函数，用于获取数组对应的字段的路径
     * @param values 一个列表，包含需要检查是否存在于数组中的所有值
     * @return 返回满足条件的查询构造器
     */
    public W arrayContainsAll(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return arrayContainsAll(getFieldName(fieldFunction), values);
    }

    /**
     * 判断数组是否包含指定的全部元素，并根据条件执行相关逻辑。
     *
     * @param condition 执行条件，为 true 时执行操作。
     * @param fieldFunction 字段函数，用于指定数组对应的字段。
     * @param values 包含在数组中的目标值列表。
     * @return 返回操作后的对象实例。
     */
    public W arrayContainsAll(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.arrayContainsAll(fieldFunction, values));
    }

    /**
     * 判断数组字段是否包含给定列表中的任意值。
     *
     * @param fieldName 数组字段的名称
     * @param values 给定的值列表
     * @return 返回当前对象以支持链式调用
     */
    public W arrayContainsAny(String fieldName, Collection<?> values) {
        return addFunction("ARRAY_CONTAINS_ANY", fieldName, values);
    }

    /**
     * 检查给定字段的值数组中是否包含指定的一组值，并根据条件执行相应操作。
     *
     * @param condition 条件标志，指示是否执行此操作，如果为true则执行；
     * @param fieldName 字段名称，指定目标数组字段的名称；
     * @param values    值列表，指定需要检查是否存在于目标数组字段中的一组值；
     * @return 返回操作后的对象。
     */
    public W arrayContainsAny(boolean condition, String fieldName, Collection<?> values) {
        return conditionalExecute(condition, w -> w.arrayContainsAny(fieldName, values));
    }

    /**
     * 判断数组字段是否包含任何指定的值。
     *
     * @param fieldFunction 字段的函数式接口，用于指定需要判断的字段
     * @param values 要匹配的值的列表
     * @return 条件封装对象
     */
    public W arrayContainsAny(FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return arrayContainsAny(getFieldName(fieldFunction), values);
    }

    /**
     * 检查数组是否包含任意指定的值，并根据条件执行操作。
     *
     * @param condition 一个布尔值，决定是否执行操作
     * @param fieldFunction 用于指定字段的函数
     * @param values 要检查的值列表
     * @return 如果条件满足，返回操作执行后的结果；否则返回当前实例
     */
    public W arrayContainsAny(boolean condition, FieldFunction<T, ?> fieldFunction, Collection<?> values) {
        return conditionalExecute(condition, w -> w.arrayContainsAny(fieldFunction, values));
    }

    /**
     * 设置字段的数组长度筛选条件。
     *
     * @param fieldName 字段名，用于指定需要筛选的字段。
     * @param length 数组的期望长度。
     * @return 当前对象实例，用于链式调用。
     */
    public W arrayLength(String fieldName, int length) {
        validateFieldName(fieldName);
        filters.add(String.format("%s.length() == %d", wrapFieldName(fieldName), length));
        return (W) this;
    }

    /**
     * 根据条件设置指定字段的数组长度。
     *
     * @param condition 条件判断值，若为true则执行操作。
     * @param fieldName 字段名称，对其设置数组长度。
     * @param length 数组长度的值。
     * @return 返回操作后的对象实例。
     */
    public W arrayLength(boolean condition, String fieldName, int length) {
        return conditionalExecute(condition, w -> w.arrayLength(fieldName, length));
    }

    /**
     * 设置数组字段的长度约束。
     *
     * @param fieldFunction 字段的函数引用，用于指定目标字段
     * @param length 数组的长度约束值
     * @return 当前对象实例，支持链式调用
     */
    public W arrayLength(FieldFunction<T, ?> fieldFunction, int length) {
        return arrayLength(getFieldName(fieldFunction), length);
    }

    /**
     * 检查数组的长度并根据条件执行相关操作。
     *
     * @param condition 执行条件，如果为 true 则执行操作，否则跳过。
     * @param fieldFunction 用于指定要检查的字段的函数。
     * @param length 数组的期望长度值。
     * @return 操作结果返回值。
     */
    public W arrayLength(boolean condition, FieldFunction<T, ?> fieldFunction, int length) {
        return conditionalExecute(condition, w -> w.arrayLength(fieldFunction, length));
    }

    /**
     * 对当前构建器和另一个构建器的过滤条件进行 AND 逻辑操作，并返回当前构建器。
     *
     * @param otherBuilder 另一个条件构建器，不可为空
     * @return 合并过滤条件后的当前构建器
     * @throws IllegalArgumentException 如果 otherBuilder 为 null，则抛出该异常
     */
    // =============== 逻辑操作 ===============
    public W and(W otherBuilder) {
        validateNotNull(otherBuilder, "AND 操作的条件构建器不能为空");
        if (CollectionUtils.isNotEmpty(filters) && CollectionUtils.isNotEmpty(otherBuilder.filters)) {
            String leftFilter = combineFiltersWithOperator(filters, "AND");
            String rightFilter = combineFiltersWithOperator(otherBuilder.filters, "AND");
            filters.clear();
            filters.add("(" + leftFilter + " AND " + rightFilter + ")");
        } else if (CollectionUtils.isNotEmpty(otherBuilder.filters)) {
            filters.addAll(otherBuilder.filters);
        }

        return (W) this;
    }

    /**
     * 根据指定条件执行逻辑操作。
     *
     * @param condition 布尔值，表示是否执行逻辑操作
     * @param other 要应用的逻辑操作对象
     * @return 如果条件为真，则返回逻辑操作后的结果；否则返回当前对象
     */
    public W and(boolean condition, W other) {
        return conditionalExecute(condition, w -> w.and(other));
    }

    /**
     * 添加一个 AND 操作到当前构建器中，并使用指定的条件构建器函数来定义 AND 操作的条件。
     *
     * @param conditionBuilder 用于构建 AND 操作条件的函数，该函数接收一个新的构建器实例并返回修改后的构建器实例
     * @return 返回包含 AND 操作的新构建器实例
     */
    public W and(Function<W, W> conditionBuilder) {
        validateNotNull(conditionBuilder, "AND 操作的条件构建器函数不能为空");
        W tempBuilder = createNewInstance();
        return and(conditionBuilder.apply(tempBuilder));
    }

    /**
     * 根据指定的条件执行并操作。如果条件为真，则使用提供的函数来构建新的逻辑。
     *
     * @param condition 条件表达式，决定是否执行并操作
     * @param conditionBuilder 用于生成并逻辑的函数
     * @return 返回更新后的对象实例
     */
    public W and(boolean condition, Function<W, W> conditionBuilder) {
        return conditionalExecute(condition, w -> w.and(conditionBuilder));
    }

    /**
     * 将当前构建器的过滤条件与另一个构建器的过滤条件通过逻辑 OR 组合起来。
     * 如果两个构建器的过滤条件均存在，则使用 OR 操作符将两部分条件组合为一条复合条件。
     * 如果其中一个构建器的过滤条件为空，则直接使用另一个构建器的过滤条件。
     *
     * @param otherBuilder 另一个条件构建器，不能为空
     * @return 返回当前构建器实例
     */
    public W or(W otherBuilder) {
        validateNotNull(otherBuilder, "OR 操作的条件构建器不能为空");
        if (CollectionUtils.isNotEmpty(filters) && CollectionUtils.isNotEmpty(otherBuilder.filters)) {
            String leftFilter = combineFiltersWithOperator(filters, "AND");
            String rightFilter = combineFiltersWithOperator(otherBuilder.filters, "AND");
            filters.clear();
            filters.add("(" + leftFilter + " OR " + rightFilter + ")");
        } else if (!otherBuilder.filters.isEmpty()) {
            filters.addAll(otherBuilder.filters);
        }
        return (W) this;
    }

    /**
     * 如果给定的条件满足，则执行逻辑或操作。
     *
     * @param condition 指定的条件，当为 true 时执行逻辑或操作。
     * @param other 用于进行逻辑或操作的另一个 W 对象。
     * @return 返回操作后的 W 对象。
     */
    public W or(boolean condition, W other) {
        return conditionalExecute(condition, w -> w.or(other));
    }

    /**
     * 添加一个 "OR" 条件到当前查询条件中。
     * 此方法接受一个条件构建器函数，将其应用于一个新的构建器实例，并将结果加入到当前条件的 "OR" 子句中。
     *
     * @param conditionBuilder 用于构建 "OR" 条件的函数，该函数接收一个新的构建器实例并返回更新后的构建器。
     * @return 当前查询条件的构建器实例，包含新的 "OR" 子句。
     */
    public W or(Function<W, W> conditionBuilder) {
        validateNotNull(conditionBuilder, "The conditional builder function for the OR operation cannot be empty");
        W tempBuilder = createNewInstance();
        return or(conditionBuilder.apply(tempBuilder));
    }

    /**
     * 根据给定条件执行逻辑“或”操作。
     *
     * @param condition 用于判断是否执行操作的布尔条件
     * @param conditionBuilder 用于构建逻辑“或”操作的函数
     * @return 返回逻辑“或”操作之后的结果
     */
    public W or(boolean condition, Function<W, W> conditionBuilder) {
        return conditionalExecute(condition, w -> w.or(conditionBuilder));
    }

    /**
     * 取反当前过滤条件，将过滤条件内容用 NOT 运算符包裹起来。
     * 如果当前过滤条件列表不为空，将条件组合为字符串并取反。
     *
     * @return 返回当前对象实例，支持链式调用。
     */
    public W not() {
        if (CollectionUtils.isNotEmpty(filters)) {
            String filterContent = filters.size() == 1 ? filters.getFirst() : String.join(" AND ", filters);
            String negatedFilter = "NOT (" + filterContent + ")";
            filters.clear();
            filters.add(negatedFilter);
        }
        return (W) this;
    }

    /**
     * 用于执行逻辑非操作的方法。
     *
     * @param condition 布尔值，指定是否执行逻辑非操作
     * @return 操作结果的条件构建器
     */
    public W not(boolean condition) {
        return conditionalExecute(condition, ConditionBuilder::not);
    }

    /**
     * 将另一个条件构建器的条件取反并添加到当前对象的条件集合中。
     *
     * @param other 需要进行 NOT 操作的条件构建器，不能为空
     * @return 当前对象自身，用于链式调用
     */
    public W not(W other) {
        validateNotNull(other, "The conditional builder for NOT operation cannot be empty");
        if (CollectionUtils.isNotEmpty(other.filters)) {
            String filterContent = other.filters.size() == 1 ? other.filters.getFirst() : String.join(" AND ", other.filters);
            String negatedFilter = "NOT (" + filterContent + ")";
            filters.add(negatedFilter);
        }
        return (W) this;
    }

    /**
     * 根据指定的条件，对当前实例执行逻辑非操作。
     *
     * @param condition 判断条件，当为 true 时执行逻辑非操作。
     * @param other 用于逻辑非操作的另一个实例。
     * @return 返回执行逻辑非操作后的实例。
     */
    public W not(boolean condition, W other) {
        return conditionalExecute(condition, w -> w.not(other));
    }

    /**
     * 对给定条件构建器执行 NOT 操作。
     *
     * @param conditionBuilder 用于构建条件的函数式接口，不能为 null。
     * @return 执行 NOT 操作后的新条件构建器实例。
     */
    public W not(Function<W, W> conditionBuilder) {
        validateNotNull(conditionBuilder, "The conditional builder function for NOT operation cannot be empty");
        // 创建新的条件构建器实例用于lambda表达式
        W tempBuilder = createNewInstance();
        return not(conditionBuilder.apply(tempBuilder));
    }

    /**
     * 如果条件为真，执行条件生成器的逻辑并将其结果取反。
     *
     * @param condition 判断条件的布尔值，当为 true 时执行条件生成器的逻辑
     * @param conditionBuilder 条件生成器，提供一个操作 W 对象的方法
     * @return 返回执行取反处理后的 W 对象
     */
    public W not(boolean condition, Function<W, W> conditionBuilder) {
        return conditionalExecute(condition, w -> w.not(conditionBuilder));
    }

    /**
     * 将多个过滤条件使用指定的逻辑运算符组合成单一的字符串表达式。
     *
     * @param filters 一个字符串列表，每个字符串表示一个过滤条件
     * @param operator 逻辑运算符，用于连接过滤条件（例如 "AND" 或 "OR"）
     * @return 一个字符串，表示使用逻辑运算符组合的完整过滤表达式
     */
    private String combineFiltersWithOperator(List<String> filters, String operator) {
        if (filters.size() == 1) {
            return filters.getFirst();
        }
        return "(" + String.join(" " + operator + " ", filters) + ")";
    }

    /**
     * 创建一个新的实例。
     *
     * @return 返回一个新创建的实例。
     */
    protected abstract W createNewInstance();

    /**
     * 构建包含所有过滤条件的查询字符串。
     * <p>
     * 收集文本匹配过滤条件和其他过滤条件，将它们组合为一个以 "AND" 分隔的字符串。
     * 如果没有任何过滤条件，则返回空字符串。
     *
     * @return 一个由 "AND" 分隔的完整过滤条件字符串；如果无过滤条件则返回空字符串
     */
    // =============== 构建方法 ===============
    public String build() {
        Collection<String> allFilters = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(textMatches)) {
            allFilters.add(String.join(" AND ", textMatches));
        }
        if (CollectionUtils.isNotEmpty(filters)) {
            allFilters.addAll(filters);
        }
        return allFilters.isEmpty() ? "" : String.join(" AND ", allFilters);
    }

    // =============== 工具方法 ===============
    private String getFieldName(FieldFunction<T, ?> fieldFunction) {
        validateNotNull(fieldFunction, "Field function cannot be empty");
        return fieldFunction.getFieldName(fieldFunction);
    }

    /**
     * 将输入的值转换为特定格式的字符串表示。
     *
     * @param value 要转换的对象，可以为null、字符串、集合或其他类型的对象
     * @return 转换后的字符串表示，当值为null时返回"NULL"，
     *         当值为字符串时返回带有转义字符的字符串，
     *         当值为集合时返回集合内元素的转换字符串，
     *         当值为其他对象类型时返回其toString方法的结果
     */
    protected String convertValue(Object value) {
        return switch (value) {
            case null -> "NULL";
            case String string -> "'" + escapeValue(string) + "'";
            case Collection<?> collection -> convertValues(collection);
            default -> value.toString();
        };
    }

    /**
     * 将集合中的值转换为字符串形式，并以逗号分隔的方式进行连接。
     * 转换后的字符串会被包裹在方括号中。
     *
     * @param values 集合，包含需要转换的值
     * @return 转换后的字符串，包含集合中所有转换后的值，格式为: [value1, value2, ...]
     */
    protected String convertValues(Collection<?> values) {
        return values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 对字段名称进行包装处理的方法。
     *
     * @param fieldName 要包装的字段名称
     * @return 包装后的字段名称
     */
    protected String wrapFieldName(String fieldName) {
        return fieldName; // 可根据需要添加引号或其他包装
    }

    private String escapeValue(String value) {
        return value.replace("'", "\\'").replace("\\", "\\\\");
    }

    private W addFilter(String fieldName, String operator, Object value) {
        validateFieldName(fieldName);
        validateNotNull(value, "The comparison value cannot be empty");
        String filter = String.format("%s %s %s",
                wrapFieldName(fieldName), operator, convertValue(value));
        filters.add(filter);
        return (W) this;
    }

    private W addFilter(FieldFunction<T, ?> fieldFunction, String operator, Object value) {
        return addFilter(getFieldName(fieldFunction), operator, value);
    }

    private W addFunction(String functionName, String fieldName, Object value) {
        validateFieldName(fieldName);
        validateNotNull(value, functionName + " 操作的值不能为空");
        String valueStr = value instanceof Collection ?
                convertValues((Collection<?>) value) : convertValue(value);
        filters.add(String.format("%s(%s, %s)", functionName, wrapFieldName(fieldName), valueStr));
        return (W) this;
    }

    // =============== 验证方法 ===============
    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new MilvusPlusException("字段名不能为空");
        }
    }

    private void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new MilvusPlusException(message);
        }
    }

    private void validateNotEmpty(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new MilvusPlusException(message);
        }
    }

    private void validateNotEmpty(Collection<?> values, String message) {
        if (values == null || values.isEmpty()) {
            throw new MilvusPlusException(message);
        }
    }
}