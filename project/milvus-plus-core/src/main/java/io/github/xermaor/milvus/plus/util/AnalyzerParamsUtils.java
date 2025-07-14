package io.github.xermaor.milvus.plus.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.xermaor.milvus.plus.annotation.AnalyzerParams;
import io.github.xermaor.milvus.plus.annotation.CustomFilter;
import io.github.xermaor.milvus.plus.annotation.Filter;
import io.github.xermaor.milvus.plus.model.AnalyzerType;
import io.github.xermaor.milvus.plus.model.BuiltInFilterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnalyzerParamsUtils {

    public static Map<String, Object> convertToMap(AnalyzerParams analyzerParams) {
        Map<String, Object> paramsMap = new HashMap<>();
        if (analyzerParams == null) {
            return paramsMap;
        }

        addTypeIfPresent(paramsMap, analyzerParams.type());
        addTokenizerIfPresent(paramsMap, analyzerParams.tokenizer());
        processFilters(paramsMap, analyzerParams.filter());

        return paramsMap;
    }

    private static void addTypeIfPresent(Map<String, Object> paramsMap, AnalyzerType analyzerType) {
        if (analyzerType != null) {
            paramsMap.put("type", analyzerType.type());
        }
    }

    private static void addTokenizerIfPresent(Map<String, Object> paramsMap, String tokenizer) {
        if (StringUtils.isNotEmpty(tokenizer)) {
            paramsMap.put("tokenizer", tokenizer);
        }
    }

    private static void processFilters(Map<String, Object> paramsMap, Filter filter) {
        if (filter == null) {
            return;
        }

        List<String> builtInFiltersList = buildBuiltInFiltersList(filter.builtInFilters());
        List<Map<String, Object>> customFiltersList = buildCustomFiltersList(filter.customFilters());

        List<Object> filters = new ArrayList<>();
        filters.addAll(builtInFiltersList);
        filters.addAll(customFiltersList);

        if (CollectionUtils.isNotEmpty(filters)) {
            paramsMap.put("filter", filters);
        }
    }

    private static List<String> buildBuiltInFiltersList(BuiltInFilterType[] builtInFilterTypes) {
        List<String> builtInFiltersList = new ArrayList<>();
        for (BuiltInFilterType builtInFilterType : builtInFilterTypes) {
            builtInFiltersList.add(builtInFilterType.name());
        }
        return builtInFiltersList;
    }

    private static List<Map<String, Object>> buildCustomFiltersList(CustomFilter[] customFilters) {
        List<Map<String, Object>> customFiltersList = new ArrayList<>();
        for (CustomFilter customFilter : customFilters) {
            Map<String, Object> filterMap = new HashMap<>();
            filterMap.put("type", customFilter.type());

            if (customFilter.max() > 0) {
                filterMap.put("max", customFilter.max());
            }

            if (customFilter.stopWords().length > 0) {
                filterMap.put("stop_words", List.of(customFilter.stopWords()));
            }

            customFiltersList.add(filterMap);
        }
        return customFiltersList;
    }
}