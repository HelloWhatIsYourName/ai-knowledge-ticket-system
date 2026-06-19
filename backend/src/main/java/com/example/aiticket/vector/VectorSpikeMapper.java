package com.example.aiticket.vector;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VectorSpikeMapper {
    void insert(@Param("content") String content, @Param("vectorLiteral") String vectorLiteral);

    List<VectorSpikeRecord> search(@Param("queryVectorLiteral") String queryVectorLiteral, @Param("limit") int limit);
}
