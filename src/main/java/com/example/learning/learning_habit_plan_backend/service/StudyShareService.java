package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.StudyShare;
import com.example.learning.learning_habit_plan_backend.repository.StudyShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudyShareService {
    
    @Autowired
    private StudyShareRepository studyShareRepository;
    
    // @Autowired
    // private UserLearningStatsService userLearningStatsService; // 移除排行榜相关服务
    
    /**
     * 创建学习分享
     */
    public StudyShare createShare(StudyShare share) {
        StudyShare savedShare = studyShareRepository.save(share);
        
        // 移除用户统计信息更新
        // userLearningStatsService.incrementSharesCount(share.getUserId());
        // userLearningStatsService.addExperiencePoints(share.getUserId(), 10); // 分享获得10经验值
        
        return savedShare;
    }
    
    /**
     * 获取公开分享列表
     */
    public Page<StudyShare> getPublicShares(Pageable pageable) {
        return studyShareRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 根据分享类型获取分享
     */
    public Page<StudyShare> getSharesByType(StudyShare.ShareType shareType, Pageable pageable) {
        return studyShareRepository.findByShareTypeAndIsPublicTrueOrderByCreatedAtDesc(shareType, pageable);
    }
    
    /**
     * 根据学科获取分享
     */
    public Page<StudyShare> getSharesBySubject(String subject, Pageable pageable) {
        return studyShareRepository.findBySubjectAndIsPublicTrueOrderByCreatedAtDesc(subject, pageable);
    }
    
    public Page<StudyShare> getSharesByTypeAndSubject(StudyShare.ShareType shareType, String subject, Pageable pageable) {
        return studyShareRepository.findByShareTypeAndSubjectAndIsPublicTrueOrderByCreatedAtDesc(shareType, subject, pageable);
    }
    
    /**
     * 获取小组内的分享
     */
    public Page<StudyShare> getGroupShares(Long groupId, Pageable pageable) {
        return studyShareRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
    }
    
    /**
     * 获取用户的分享
     */
    public Page<StudyShare> getUserShares(Long userId, Pageable pageable) {
        return studyShareRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 搜索分享
     */
    public Page<StudyShare> searchShares(String keyword, Pageable pageable) {
        return studyShareRepository.findByTitleContainingAndIsPublicTrue(keyword, pageable);
    }
    
    /**
     * 获取热门分享
     */
    public Page<StudyShare> getPopularShares(Pageable pageable) {
        return studyShareRepository.findPopularShares(pageable);
    }
    
    /**
     * 获取最近热门分享（最近7天）
     */
    public Page<StudyShare> getRecentPopularShares(Pageable pageable) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        return studyShareRepository.findRecentPopularShares(startDate, pageable);
    }
    
    /**
     * 根据标签搜索分享
     */
    public Page<StudyShare> searchSharesByTag(String tag, Pageable pageable) {
        return studyShareRepository.findByTagsContaining(tag, pageable);
    }
    
    /**
     * 获取推荐分享
     */
    public Page<StudyShare> getRecommendedShares(List<String> userSubjects, Pageable pageable) {
        if (userSubjects == null || userSubjects.isEmpty()) {
            return getPopularShares(pageable);
        }
        return studyShareRepository.findRecommendedShares(userSubjects, pageable);
    }
    
    /**
     * 点赞分享
     */
    public boolean likeShare(Long shareId, Long userId) {
        Optional<StudyShare> shareOpt = studyShareRepository.findById(shareId);
        if (!shareOpt.isPresent()) {
            return false;
        }
        
        StudyShare share = shareOpt.get();
        // 确保likeCount不为null
        if (share.getLikeCount() == null) {
            share.setLikeCount(0);
        }
        share.setLikeCount(share.getLikeCount() + 1);
        studyShareRepository.save(share);
        
        // 移除分享者的获赞统计更新
        // userLearningStatsService.incrementLikesReceived(share.getUserId());
        // userLearningStatsService.addExperiencePoints(share.getUserId(), 2); // 获得点赞获得2经验值
        
        return true;
    }
    
    /**
     * 取消点赞
     */
    public boolean unlikeShare(Long shareId, Long userId) {
        Optional<StudyShare> shareOpt = studyShareRepository.findById(shareId);
        if (!shareOpt.isPresent()) {
            return false;
        }
        
        StudyShare share = shareOpt.get();
        // 确保likeCount不为null
        if (share.getLikeCount() == null) {
            share.setLikeCount(0);
        }
        if (share.getLikeCount() > 0) {
            share.setLikeCount(share.getLikeCount() - 1);
            studyShareRepository.save(share);
            
            // 移除分享者的获赞统计更新
            // userLearningStatsService.decrementLikesReceived(share.getUserId());
        }
        
        return true;
    }
    
    /**
     * 增加浏览量
     */
    public void incrementViewCount(Long shareId) {
        Optional<StudyShare> shareOpt = studyShareRepository.findById(shareId);
        if (shareOpt.isPresent()) {
            StudyShare share = shareOpt.get();
        // 确保viewCount不为null
        if (share.getViewCount() == null) {
            share.setViewCount(0);
        }
        share.setViewCount(share.getViewCount() + 1);
            studyShareRepository.save(share);
        }
    }
    
    /**
     * 获取分享详情
     */
    public Optional<StudyShare> getShareById(Long shareId) {
        return studyShareRepository.findById(shareId);
    }
    
    /**
     * 更新分享
     */
    public StudyShare updateShare(StudyShare share) {
        return studyShareRepository.save(share);
    }
    
    /**
     * 删除分享
     */
    public boolean deleteShare(Long shareId, Long userId) {
        Optional<StudyShare> shareOpt = studyShareRepository.findById(shareId);
        if (!shareOpt.isPresent()) {
            return false;
        }
        
        StudyShare share = shareOpt.get();
        
        // 只有分享者可以删除
        if (!share.getUserId().equals(userId)) {
            return false;
        }
        
        studyShareRepository.delete(share);
        
        // 移除用户统计信息更新
        // userLearningStatsService.decrementSharesCount(userId);
        
        return true;
    }
    
    /**
     * 解析标签字符串为列表
     */
    public List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(tags.split(","));
    }
    
    /**
     * 将标签列表转换为字符串
     */
    public String tagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}