package com.wany.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wany.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}