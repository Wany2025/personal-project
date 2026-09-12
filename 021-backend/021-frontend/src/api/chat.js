import request from '@/utils/request'

export const chatApi = {
  // 发送文本消息
  sendMessage: (data) => {
    return request({
      url: '/chat/send',
      method: 'post',
      data
    })
  },

  // 会话列表
  getSessionList: () => {
    return request({
      url: '/chat/list',
      method: 'get'
    })
  },

  // 聊天记录（与某人关于某商品）
  getMessages: (params) => {
    return request({
      url: '/chat/messages',
      method: 'get',
      params
    })
  },

  // 标记已读
  markRead: (params) => {
    return request({
      url: '/chat/read',
      method: 'put',
      params
    })
  },

  // 未读消息总数
  getUnreadCount: () => {
    return request({
      url: '/chat/unread/count',
      method: 'get'
    })
  }
}
