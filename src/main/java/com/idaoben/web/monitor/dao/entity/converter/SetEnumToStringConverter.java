package com.idaoben.web.monitor.dao.entity.converter;

import com.idaoben.utils.valued_enum.DescribedValuedEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetEnumToStringConverter<T extends DescribedValuedEnum> implements AttributeConverter<Set<T>, String> {

    private static final Logger logger = LoggerFactory.getLogger(SetEnumToStringConverter.class);

    private static volatile SetEnumToStringConverter INSTANCE;

    private SetEnumToStringConverter(){}

    public static SetEnumToStringConverter getInstance() {
        SetEnumToStringConverter localRef = INSTANCE;
        if (localRef == null) {
            synchronized(SetEnumToStringConverter.class) {
                localRef = INSTANCE;
                if (localRef == null) {
                    INSTANCE = localRef = new SetEnumToStringConverter();
                }
            }
        }
        return localRef;
    }

    @Override
    public String convertToDatabaseColumn(Set<T> attributes) {
        if (!CollectionUtils.isEmpty(attributes)) {
            Class<T> clazz = (Class<T>) attributes.iterator().next().getClass();
            StringBuilder sb = new StringBuilder(clazz.getName()).append(':');
            try {
                T[] values = clazz.getEnumConstants();
                for(T enumValue : values){
                    if(attributes.contains(enumValue)){
                        sb.append('1');
                    } else {
                        sb.append('0');
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public Set<T> convertToEntityAttribute(String dbData) {
        Set<T> describedValuedEnums = new LinkedHashSet<>();
        if (!StringUtils.isEmpty(dbData)) {
            String target = dbData.split(":")[0];
            String attributesStr = dbData.split(":")[1];
            try {
                Class<T> clazz = (Class<T>) Class.forName(target);
                T[] values =  clazz.getEnumConstants();
                for(int i = 0; i < values.length; i ++){
                    if(attributesStr.length() > i){
                        if(attributesStr.charAt(i) == '1'){
                            describedValuedEnums.add(values[i]);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return describedValuedEnums;
    }

    /**
     * 获取模糊搜索用字符串
     * @param attributes
     * @return
     */
    public String getSearchString(Collection<T> attributes){
        String searchStr = null;
        if(attributes != null && !attributes.isEmpty()){
            Class<T> clazz = (Class<T>) attributes.iterator().next().getClass();
            StringBuilder sb = new StringBuilder(clazz.getName()).append(':');
            T[] values = clazz.getEnumConstants();
            for(T enumValue : values){
                if(attributes.contains(enumValue)){
                    sb.append('1');
                } else {
                    //单字母模糊匹配
                    sb.append('_');
                }
            }
            searchStr = sb.toString();
        }
        return searchStr;
    }
}
