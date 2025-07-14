package com.github.xermao.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import com.github.xermao.milvus.plus.core.conditions.LambdaDeleteWrapper;
import com.github.xermao.milvus.plus.core.conditions.LambdaInsertWrapper;
import com.github.xermao.milvus.plus.core.conditions.LambdaQueryWrapper;
import com.github.xermao.milvus.plus.core.conditions.LambdaUpdateWrapper;
import com.github.xermao.milvus.plus.core.mapper.BaseMilvusMapper;
import com.github.xermao.milvus.plus.model.vo.MilvusResp;
import com.github.xermao.milvus.plus.model.vo.MilvusResult;
import com.github.xermao.milvus.plus.service.IAMService;
import com.github.xermao.milvus.plus.service.ICMService;
import com.github.xermao.milvus.plus.service.IVecMService;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Component
public class MilvusService implements IAMService, ICMService, IVecMService {
    private MilvusClientV2 client;

    @Override
    public MilvusClientV2 getClient() {
        if (client == null) {
            client = Solon.context().getBean(MilvusClientV2.class);
        }
        return client;
    }

    public <T> MilvusResp<List<MilvusResult<T>>> getById(Class<T> entityClass, Serializable... ids) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaQueryWrapper<T> lambda = mapper.lambda(entityClass, new LambdaQueryWrapper<>());
        return lambda.getById(ids);
    }

    public <T> MilvusResp<DeleteResp> removeById(Class<T> entityClass, Object... ids) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaDeleteWrapper<T> lambda = mapper.lambda(entityClass, new LambdaDeleteWrapper<>());
        return lambda.removeById(ids);
    }

    @SafeVarargs
    public final <T> MilvusResp<InsertResp> insert(T... entities) {
        Class<T> entityClass = getEntityClass(entities[0]);
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaInsertWrapper<T> lambda = mapper.lambda(entityClass, new LambdaInsertWrapper<>());
        return lambda.insert(entities);
    }

    public <T> MilvusResp<InsertResp> insert(Collection<T> entities) {
        T entity = entities.iterator().next();
        Class<T> entityClass = getEntityClass(entity);
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaInsertWrapper<T> lambda = mapper.lambda(entityClass, new LambdaInsertWrapper<>());
        return lambda.insert(entities);
    }

    @SafeVarargs
    public final <T> MilvusResp<UpsertResp> updateById(T... entities) {
        Class<T> entityClass = getEntityClass(entities[0]);
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaUpdateWrapper<T> lambda = mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
        return lambda.updateById(entities);
    }

    public <T> MilvusResp<UpsertResp> updateById(Collection<T> entities) {
        T entity = entities.iterator().next();
        Class<T> entityClass = getEntityClass(entity);
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        LambdaUpdateWrapper<T> lambda = mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
        return lambda.updateById(entities);
    }

    public <T> LambdaUpdateWrapper<T> ofUpdate(Class<T> entityClass) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        return mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
    }

    public <T> LambdaInsertWrapper<T> ofInsert(Class<T> entityClass) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        return mapper.lambda(entityClass, new LambdaInsertWrapper<>());
    }

    public <T> LambdaDeleteWrapper<T> ofDelete(Class<T> entityClass) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        return mapper.lambda(entityClass, new LambdaDeleteWrapper<>());
    }

    public <T> LambdaQueryWrapper<T> ofQuery(Class<T> entityClass) {
        BaseMilvusMapper<T> mapper = getBaseMilvusMapper();
        return mapper.lambda(entityClass, new LambdaQueryWrapper<>());
    }

    private <T> BaseMilvusMapper<T> getBaseMilvusMapper() {
        MilvusClientV2 client = getClient();
        return new BaseMilvusMapper<T>() {
            @Override
            public MilvusClientV2 getClient() {
                return client;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getEntityClass(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        return (Class<T>) entity.getClass();
    }
}
