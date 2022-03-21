package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.tomcat.jni.User;

import java.util.List;

@Mapper
public interface CommentMapper {

     List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

     int selectCountByEntity(int entityType, int entityId);

     List<Comment> selectCommentsByUser(int userId,int offset, int limit);

     int selectCountByUser(int userId);

     int insertComment(Comment comment);

     Comment selectCommentById(int id);

     void updateStatus(int id,int status);
}
