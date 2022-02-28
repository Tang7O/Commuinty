package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 查询会话列表, 针对每一个会话返回一条最新的私信
     * @param userId 用户Id
     * @param offset 每页的私信的起始编号
     * @param limit  每页私信的数量
     * @return       这一页的私信
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询当前用户的会话数量
     * @param userId 用户Id
     * @return       当前用户会话数量
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId 会话ID
     * @param offset         当前页的起始消息ID
     * @param limit          每页的消息数量
     * @return               当前页的消息
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId 会话Id
     * @return  数量
     */
    int selectLettersCount(String conversationId);

    /**
     * 查询未读私信的数量
     * @param userId         用户Id
     * @param conversationId 会话Id
     * @return               未读私信的数量
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 新增一个消息
     * @param message 要添加的消息
     * @return        添加消息的个数
     */
    int insertMessage(Message message);

    // 修改消息状态

    /**
     * 修改消息的状态
     * @param ids     要修改的消息的Id
     * @param status  修改后的状态
     * @return        修改的数量
     */
    int updateStatus(List<Integer> ids, int status);

    /**
     * 查询某个主题下最新的通知
     * @param userId 用户Id
     * @param topic  主题类型
     * @return       主题下的最新的消息
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的通知数量
     * @param userId 用户Id
     * @param topic  主题类型
     * @return       数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 查询某个主题下的未读通知数量
     * @param userId 用户Id
     * @param topic  主题类型
     * @return       未读通知数量
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * 查询某个主题下的所有通知
     * @param userId  用户ID
     * @param topic   主题类型
     * @param offset  分页
     * @param limit   分页
     * @return        返回本页的通知
     */
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
