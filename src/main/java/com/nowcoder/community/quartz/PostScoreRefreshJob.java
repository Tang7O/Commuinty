package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 网站创建时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-12-21 01:53:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化网站创建时间失败!",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0) {
            logger.info("[任务取消]没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数 " + operations.size());
        while(operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post == null) {
            logger.error("该帖子不存在： id = " + postId);
            return;
        }

        // log(精华分 + 评论数*10 + 点赞数*2) + (发布时间 - 网站创建时间）
        // 是否加精
        boolean wonderful = post.getStatus() == 1;
        // 评论数
        int commentCount = post.getCommentCount();
        // 点赞数量
        long lickCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        // 帖子权重
        double w = (wonderful ? 75 : 0) + commentCount * 10L + lickCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w,1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
