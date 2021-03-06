package com.wl.lch.service.impl;

import com.wl.lch.core.LabelMapper;
import com.wl.lch.core.PostEstherMapper;
import com.wl.lch.core.ReplyEstherMapper;
import com.wl.lch.core.SubscriberMapper;
import com.wl.lch.domain.Resp;
import com.wl.lch.dto.EstherDTO;
import com.wl.lch.dto.EstherDetailDTO;
import com.wl.lch.dto.ReplyDTO;
import com.wl.lch.dto.UserDTO;
import com.wl.lch.entity.Label;
import com.wl.lch.entity.PostEsther;
import com.wl.lch.entity.ReplyEsther;
import com.wl.lch.entity.Subscriber;
import com.wl.lch.exception.BusinessException;
import com.wl.lch.service.PostService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    static final org.slf4j.Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Resource
    private PostEstherMapper postEstherMapper;

    @Resource
    private SubscriberMapper subscriberMapper;

    @Resource
    private LabelMapper labelMapper;

    @Resource
    private ReplyEstherMapper replyEstherMapper;

    @Override
    public List<EstherDTO> findPostEsther(int pageNo, int length, String type, String search) {
        int start = (pageNo - 1) * length;
        List<PostEsther> postEstherList;
        if (Resp.GOOD.equals(type)) {
            int topNum = StringUtils.isEmpty(search) ?
                    postEstherMapper.sumPageSize(true, true, null) :
                    postEstherMapper.sumPageSize(true, true, search);
            if ((topNum - start) > 0 && (topNum - start) < length) {
                postEstherList = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, topNum - start, true, true, null) :
                        postEstherMapper.selectLimit(start, topNum - start, true, true, search);
                List<PostEsther> postEstherList2 = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, length - topNum + start, true, false, null) :
                        postEstherMapper.selectLimit(start, length - topNum + start, true, false, search);
                postEstherList.addAll(postEstherList2);
            } else {
                postEstherList = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, length, true, false, null) :
                        postEstherMapper.selectLimit(start, length, true, false, search);
            }
        } else if (Resp.TOP.equals(type)) {
            postEstherList = StringUtils.isEmpty(search) ?
                    postEstherMapper.selectLimit(start, length, null, true, null) :
                    postEstherMapper.selectLimit(start, length, null, true, search);
        } else {
            int topNum = StringUtils.isEmpty(search) ?
                    postEstherMapper.sumPageSize(null, true, null) :
                    postEstherMapper.sumPageSize(null, true, search);
            if ((topNum - start) > 0 && (topNum - start) < length) {
                postEstherList = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, topNum - start, null, true, null) :
                        postEstherMapper.selectLimit(start, topNum - start, null, true, search);
                List<PostEsther> postEstherList2 = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, length - topNum + start, null, false, null) :
                        postEstherMapper.selectLimit(start, length - topNum + start, null, false, search);
                postEstherList.addAll(postEstherList2);
            } else {
                postEstherList = StringUtils.isEmpty(search) ?
                        postEstherMapper.selectLimit(start, length, null, false, null) :
                        postEstherMapper.selectLimit(start, length, null, false, search);
            }
        }
        List<EstherDTO> esthers = new ArrayList<>();
        for (PostEsther postEsther : postEstherList) {
            EstherDTO esther = getEstherDTO(postEsther);
            esthers.add(esther);
        }
        return esthers;
    }

    @Override
    public EstherDTO getEstherDTO(PostEsther postEsther) {
        EstherDTO estherDTO = new EstherDTO();
        estherDTO.setContent(postEsther.getContent());
        estherDTO.setGood(postEsther.getGood());
        estherDTO.setId(postEsther.getId());
        estherDTO.setInitTime(postEsther.getInitTime());
        estherDTO.setLabelId(postEsther.getLabelId());
        estherDTO.setReplyCount(postEsther.getReplyCount());
        estherDTO.setTitle(postEsther.getTitle());
        estherDTO.setTop(postEsther.getTop());
        estherDTO.setUserId(postEsther.getUserId());
        Subscriber subscriber = subscriberMapper.selectByPrimaryKey(postEsther.getUserId());
        if (subscriber == null) {
            logger.error("根据userId:{}查询不到subscribe", postEsther.getUserId());
            throw new BusinessException("根据userId查询不到subscribe");
        }
        estherDTO.setUser(getUserDTO(subscriber));
        Label label = labelMapper.selectByPrimaryKey(postEsther.getLabelId());
        if (label == null) {
            logger.error("根据labelId:{}查询不到label", postEsther.getLabelId());
            throw new BusinessException("根据labelId查询不到label");
        }
        estherDTO.setLabel(label);
        return estherDTO;
    }

    @Override
    public UserDTO getUserDTO(Subscriber subscriber) {
        UserDTO user = new UserDTO();
        user.setAccount(subscriber.getAccount());
        user.setCreateTime(subscriber.getCreateTime());
        user.setGender(subscriber.getGender());
        user.setId(subscriber.getId());
        user.setImageUrl(subscriber.getImageUrl());
        user.setName(subscriber.getName());
        user.setSignature(subscriber.getSignature());
//        user.setPhone(subscriber.getPhone());
//        user.setPoint(subscriber.getPoint());
//        user.setReviseTime(subscriber.getReviseTime());
//        user.setUserId(subscriber.getUserId());
        return user;
    }

    @Override
    public int sumPostPageSize(String type, String search) {
        int num = 0;
        if (Resp.GOOD.equals(type)) {
            num = StringUtils.isEmpty(search) ?
                    postEstherMapper.sumPageSize(true, null, null) :
                    postEstherMapper.sumPageSize(true, null, search);
        } else if (Resp.TOP.equals(type)) {
            num = StringUtils.isEmpty(search) ?
                    postEstherMapper.sumPageSize(null, true, null) :
                    postEstherMapper.sumPageSize(null, true, search);
        } else {
            num = StringUtils.isEmpty(search) ?
                    postEstherMapper.sumPageSize(null, null, null) :
                    postEstherMapper.sumPageSize(null, null, search);
        }
        return num;
    }

    @Override
    public EstherDetailDTO findReplyEsther(int postId, int pageNo, int length) {
        PostEsther postEsther = postEstherMapper.selectByPrimaryKey(postId);
        if (postEsther == null) {
            logger.error("根据id:{}查询不到该帖子", postId);
            throw new BusinessException("根据id查询不到该帖子");
        }
        EstherDetailDTO estherDetailDTO = new EstherDetailDTO();
        estherDetailDTO.setPosts(getEstherDTO(postEsther));
        int start = (pageNo - 1) * length;
        List<ReplyEsther> replyEsthers = replyEstherMapper.selectByPostId(postId, start, length);
        if (replyEsthers == null) {
            logger.error("根据id:{}查询不到该帖子", postId);
            throw new BusinessException("根据id查询不到该帖子");
        }
        List<ReplyDTO> replyDTOS = new ArrayList<>();
        for (ReplyEsther replyEsther : replyEsthers) {
            ReplyDTO replyDTO = new ReplyDTO();
            replyDTO.setContent(replyEsther.getContent());
            replyDTO.setId(replyEsther.getId());
            replyDTO.setInitTime(replyEsther.getInitTime());
            replyDTO.setUp(replyEsther.getUp());
            Subscriber subscriber = subscriberMapper.selectByPrimaryKey(replyEsther.getUserId());
            if (subscriber == null) {
                logger.error("没有此用户:{}", replyEsther.getUserId());
                throw new BusinessException("没有此用户");
            }
            replyDTO.setUser(getUserDTO(subscriber));
            replyDTOS.add(replyDTO);
        }
        estherDetailDTO.setReplys(replyDTOS);
        return estherDetailDTO;
    }

    @Override
    public int sumReplyPageSize(int postsId) {
        return replyEstherMapper.sumReplyPageSize(postsId);
    }


}
