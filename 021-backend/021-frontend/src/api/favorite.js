import request from '@/utils/request'

export const favoriteApi = {
  // 添加收藏
  addFavorite: (productId) => {
    return request({
      url: '/favorite/add',
      method: 'post',
      params: { productId }
    })
  },

  // 取消收藏
  removeFavorite: (productId) => {
    return request({
      url: `/favorite/${productId}`,
      method: 'delete'
    })
  },

  // 我的收藏列表（分页）
  getMyFavorites: (params) => {
    return request({
      url: '/favorite/my',
      method: 'get',
      params
    })
  },

  // 检查是否已收藏
  checkFavorite: (productId) => {
    return request({
      url: `/favorite/check/${productId}`,
      method: 'get'
    })
  }
}
