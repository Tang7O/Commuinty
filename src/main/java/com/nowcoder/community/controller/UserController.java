package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null){
            model.addAttribute("error", "你还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常!", e);
        }
        // 更新当前用户的头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存储路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败：" + e.getMessage());
        }

    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordMsg", "两次输入的密码不一致");
            return "/site/setting";
        }

        User user = hostHolder.getUser();

        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());

        if(!user.getPassword().equals(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "原密码不正确");
            return "/site/setting";
        }

        newPassword = CommunityUtil.md5(newPassword + user.getSalt());

        if(user.getPassword().equals(newPassword)) {
            model.addAttribute("newPasswordMsg", "新密码不能和原密码相同");
            return "/site/setting";
        }

        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/index";
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId ,Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        model.addAttribute("followeeCount",followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        model.addAttribute("followerCount",followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }

        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

    @RequestMapping(path = "/my-post/{userId}", method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId , Model model, Page page) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        int postCount = discussPostService.findDiscussPostRows(userId);
        page.setRows(postCount);

        model.addAttribute("postCount",postCount);

        List<DiscussPost> list =  discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit(),0);
        List<Map<String,Object>> discussPosts = new ArrayList<>();

        if(list != null){
            for(DiscussPost post : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/site/my-post";
    }

    @RequestMapping(path = "/my-reply/{userId}", method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId ,Model model,Page page) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        int rows = commentService.findCommentCountByUser(userId);
        page.setRows(rows);

        model.addAttribute("commentCount",rows);

        List<Comment> list =  commentService.findCommentsByUser(userId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> comments = new ArrayList<>();

        if(list != null){
            for(Comment comment : list){
                Map<String, Object> map = new HashMap<>();
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("comment",comment);
                map.put("post",post);
                comments.add(map);
            }
        }
        model.addAttribute("comments",comments);

        return "/site/my-reply";
    }
}
