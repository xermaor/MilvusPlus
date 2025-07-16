package io.github.xermaor.milvus.plus.service;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.github.xermaor.milvus.plus.annotation.MilvusCollection;
import io.github.xermaor.milvus.plus.converter.MilvusConverter;
import io.github.xermaor.milvus.plus.exception.MilvusPlusException;
import io.github.xermaor.milvus.plus.model.MilvusEntity;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.response.ListAliasResp;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractMilvusClientBuilder implements MilvusClientBuilder, ICMService {

    private final static Logger log = LoggerFactory.getLogger(AbstractMilvusClientBuilder.class);

    protected String[] packages;
    protected MilvusClientV2 client;

    @Override
    public void initialize() {
        handler();
    }

    public void handler() {
        List<Class<?>> classes = getClass(packages);
        if (classes.isEmpty()) {
            log.warn("no any collections have been initialized, see if the [packages] parameter is configured correctly. :( !");
            return;
        }
        performBusinessLogic(classes);
    }

    @Override
    public MilvusClientV2 getClient() {
        return client;
    }

    // 获取指定包下实体类
    private List<Class<?>> getClass(String... packages) {
        if (packages == null || packages.length == 0) {
            throw new MilvusPlusException("model package is null, please configure the [packages] parameter");
        }

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packages) // 指定扫描的包路径
                .enableClassInfo()                               // 启用类信息扫描
                .enableAnnotationInfo()                          // 启用注解信息扫描
                .scan()) {                                       // 执行扫描

            return scanResult.getClassesWithAnnotation(MilvusCollection.class.getName()) // 获取带指定注解的类
                    .stream()
                    .map(classInfo -> {
                        try {
                            return Class.forName(classInfo.getName()); // 加载类
                        } catch (ClassNotFoundException e) {
                            throw new MilvusPlusException("Failed to load class: " + classInfo.getName(), e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }


    // 缓存 + 是否构建集合
    public void performBusinessLogic(List<Class<?>> annotatedClasses) {
        for (Class<?> milvusClass : annotatedClasses) {
            MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
            createCollection(milvusEntity);
            aliasProcess(milvusEntity);
        }
    }

    private void aliasProcess(MilvusEntity milvusEntity) {
        if (StringUtils.isBlank(milvusEntity.collectionName()) || CollectionUtils.isEmpty(milvusEntity.alias())) {
            return;
        }
        ListAliasResp listAliasResp = listAliases(milvusEntity);
        Optional.ofNullable(listAliasResp)
                .ifPresent(aliasInfo -> {
                    // 获取不存在的别名
                    List<String> aliasList = milvusEntity.alias().stream()
                            .filter(e -> !aliasInfo.getAlias().contains(e))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList());
                    log.info("processing alias: {}", aliasList);
                    createAlias(new MilvusEntity(
                            milvusEntity.collectionName(), milvusEntity.description(),
                            aliasList, milvusEntity.indexParams(), milvusEntity.milvusFields(),
                            milvusEntity.partitionName(), milvusEntity.consistencyLevel(),
                            milvusEntity.enableDynamicField(), milvusEntity.functions()
                    ));
                });
    }
}