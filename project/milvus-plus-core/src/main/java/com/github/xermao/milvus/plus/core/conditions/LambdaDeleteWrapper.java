package com.github.xermao.milvus.plus.core.conditions;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import org.apache.commons.lang3.StringUtils;
import com.github.xermao.milvus.plus.cache.ConversionCache;
import com.github.xermao.milvus.plus.core.FieldFunction;
import com.github.xermao.milvus.plus.model.vo.MilvusResp;
import com.github.xermao.milvus.plus.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建器内部类，用于构建remove请求
 */
public class LambdaDeleteWrapper<T> extends AbstractChainWrapper<T, LambdaDeleteWrapper<T>> implements Wrapper<LambdaDeleteWrapper<T>, T> {
    private final static Logger log = LoggerFactory.getLogger(LambdaDeleteWrapper.class);

    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;
    private final List<Object> ids = new ArrayList<>();

    /**
     * 设置删除操作所使用的分区名称。
     *
     * @param partitionName 分区名称，用于指定删除操作的目标分区，参数为字符串类型。
     * @return 当前 {@code LambdaDeleteWrapper} 实例，支持链式调用。
     */
    public LambdaDeleteWrapper<T> partition(String partitionName) {
        this.partitionName = partitionName;
        return this;
    }

    /**
     * 设置删除操作所使用的分区名称。
     *
     * @param partitionName 分区字段，通过 {@code FieldFunction<T, ?>} 的方式指定分区字段，
     *                      用于定位删除操作目标分区。
     * @return 当前 {@code LambdaDeleteWrapper} 实例，支持链式调用。
     */
    public LambdaDeleteWrapper<T> partition(FieldFunction<T, ?> partitionName) {
        this.partitionName = partitionName.getFieldName(partitionName);
        return this;
    }

    protected LambdaDeleteWrapper<T> createNewInstance() {
        LambdaDeleteWrapper<T> wrapper = new LambdaDeleteWrapper<>();
        wrapper.entityType = entityType;
        return wrapper;
    }

    /**
     * 设置要删除实体的 ID，可以接受多个 ID 作为参数。该方法会将输入的 ID 数组转换为列表并调用相应的方法。
     *
     * @param id 要删除的实体 ID，支持多个 ID，参数类型为可变参数 Object。
     * @return 当前 {@code LambdaDeleteWrapper} 实例，方便链式调用。
     */
    public LambdaDeleteWrapper<T> id(Object... id) {
        return id(List.of(id));
    }

    /**
     * 设置要删除的实体 ID 列表。
     *
     * @param ids 要删除的实体 ID 列表，参数类型为 List，对应的每个对象为实体的 ID。
     * @return 当前 {@code LambdaDeleteWrapper} 实例，用于支持链式调用。
     */
    public LambdaDeleteWrapper<T> id(List<Object> ids) {
        this.ids.addAll(ids);
        return this;
    }

    /**
     * 构建一个包含删除请求的 {@code DeleteReq} 对象。
     * 该方法根据当前实例的字段值（如集合名称、过滤条件、分区名称及 ID 列表等）生成删除请求对象。
     *
     * @return 构建后的 {@code DeleteReq} 对象，用于执行删除操作
     */
    private DeleteReq buildReq() {
        DeleteReq.DeleteReqBuilder<?, ?> builder = DeleteReq.builder()
                .collectionName(this.collectionName);
        String filterStr = this.build();
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        if (StringUtils.isNotEmpty(partitionName)) {
            builder.partitionName(partitionName);
        }
        if (!ids.isEmpty()) {
            builder.ids(this.ids);
        }
        // Set other parameters as needed
        return builder.build();
    }

    /**
     * 删除数据操作的具体实现。
     * 调用此方法会构建删除请求并通过 Milvus 客户端执行删除操作。
     * 删除操作对指定的集合及条件下的实体数据生效。
     *
     * @return 包装了删除响应的 MilvusResp 对象，其中包含删除操作的成功状态和实际结果。
     * @throws MilvusException 当执行过程中的任何异常出现时都会抛出。
     */
    public MilvusResp<DeleteResp> remove() throws MilvusException {
        return executeWithRetry(
                () -> {
                    DeleteReq deleteReq = buildReq();
                    log.info("build remove param-->{}", GsonUtil.toJson(deleteReq));
                    DeleteResp delete = client.delete(deleteReq);
                    return new MilvusResp<>(true, delete);
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }

    public MilvusResp<DeleteResp> removeById(Object... ids) throws MilvusException {
        this.id(ids);
        return remove();
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.client = client;
        this.collectionName = collectionName;
        this.entityType = entityType;
    }

    @Override
    public LambdaDeleteWrapper<T> wrapper() {
        return this;
    }
}