package io.github.xermaor.milvus.demo.model;

import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import lombok.Data;
import io.github.xermaor.milvus.plus.annotation.*;

import java.util.List;

@Data
@MilvusCollection(name = "face_collection",level = ConsistencyLevel.STRONG,enableDynamicField = true)
public class Face {
    @MilvusField(
            name = "person_id", // 字段名称
            dataType = DataType.Int64, // 数据类型为64位整数
            isPrimaryKey = true, // 标记为主键
            autoID = true // 假设这个ID是自动生成的
    )
    private Long personId; // 人员的唯一标识符

    @MilvusField(
            name = "person_name",
            dataType = DataType.VarChar
    )
    private String personName; // 人员姓名

    @MilvusField(
            name = "person_info",
            dataType = DataType.JSON
    )
    private Person person;

    @MilvusField(
            name = "temp",
            dataType = DataType.Int64
    )
    private Integer temp;

//    @MilvusField(
//            name = "text",
//            dataType = DataType.VarChar,
//            enableAnalyzer = true,
//            enableMatch = true
//    )
//    private String text; // 文本

    @MilvusField(
            name = "face_vector", // 字段名称
            dataType = DataType.FloatVector, // 数据类型为浮点型向量
            dimension = 128 // 向量维度，假设人脸特征向量的维度是128
    )
    @MilvusIndex(
            indexType = IndexParam.IndexType.IVF_FLAT, // 使用IVF_FLAT索引类型
            metricType = IndexParam.MetricType.L2, // 使用L2距离度量类型
            indexName = "face_index", // 索引名称
            extraParams = { // 指定额外的索引参数
                    @ExtraParam(key = "nlist", value = "100") // 例如，IVF的nlist参数
            }
    )
    private List<Float> faceVector; // 存储人脸特征的向量


//    //后续添加
//
//    private String sex;
//
//
//    private Integer age;

}