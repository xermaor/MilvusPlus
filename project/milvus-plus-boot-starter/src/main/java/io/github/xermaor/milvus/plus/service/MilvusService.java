package io.github.xermaor.milvus.plus.service;

import io.github.xermaor.milvus.plus.config.MilvusPlusAutoConfiguration;
import io.github.xermaor.milvus.plus.core.conditions.LambdaDeleteWrapper;
import io.github.xermaor.milvus.plus.core.conditions.LambdaInsertWrapper;
import io.github.xermaor.milvus.plus.core.conditions.LambdaQueryWrapper;
import io.github.xermaor.milvus.plus.core.conditions.LambdaUpdateWrapper;
import io.github.xermaor.milvus.plus.core.mapper.BaseMilvusMapper;
import io.github.xermaor.milvus.plus.exception.MilvusPlusException;
import io.github.xermaor.milvus.plus.model.vo.MilvusResp;
import io.github.xermaor.milvus.plus.model.vo.MilvusResult;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;

import java.util.Collection;
import java.util.List;

public class MilvusService implements IAMService, ICMService, IVecMService {

    @Override
    public MilvusClientV2 getClient() {
        return MilvusPlusAutoConfiguration.milvusInit.getClient();
    }

    public <T> MilvusResp<List<MilvusResult<T>>> getById(Class<T> entityClass, Object... ids) {
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
        return insert(List.of(entities));
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
        return updateById(List.of(entities));
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
        return new BaseMilvusMapper<>() {
            @Override
            public MilvusClientV2 getClient() {
                return client;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getEntityClass(T entity) {
        if (entity == null) {
            throw new MilvusPlusException("Entity must not be null");
        }
        return (Class<T>) entity.getClass();
    }

}
