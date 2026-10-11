package com.wany.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wany.entity.Favorite;
import com.wany.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 收藏列表（联表查询商品与卖家信息）
     * 注意：即使商品被逻辑删除也返回，便于前端提示"已下架"
     */
    Page<FavoriteVO> getFavoriteList(Page<FavoriteVO> page, @Param("userId") Long userId);
}
