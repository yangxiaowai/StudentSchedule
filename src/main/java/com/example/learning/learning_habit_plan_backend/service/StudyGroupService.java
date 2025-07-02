package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.StudyGroup;
import com.example.learning.learning_habit_plan_backend.entity.StudyGroupMember;
import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import com.example.learning.learning_habit_plan_backend.repository.StudyGroupRepository;
import com.example.learning.learning_habit_plan_backend.repository.StudyGroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyGroupService {
    
    @Autowired
    private StudyGroupRepository studyGroupRepository;
    
    @Autowired
    private StudyGroupMemberRepository studyGroupMemberRepository;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * 创建学习小组
     */
    public StudyGroup createGroup(StudyGroup group) {
        // 生成邀请码
        group.setInviteCode(generateInviteCode());
        group.setCurrentMembers(1);
        
        StudyGroup savedGroup = studyGroupRepository.save(group);
        
        // 创建者自动成为小组成员
        StudyGroupMember creator = new StudyGroupMember(
            savedGroup.getId(), 
            savedGroup.getCreatorId(), 
            StudyGroupMember.MemberRole.CREATOR
        );
        studyGroupMemberRepository.save(creator);
        
        return savedGroup;
    }
    
    /**
     * 加入学习小组
     */
    public boolean joinGroup(Long groupId, Long userId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            return false;
        }
        
        StudyGroup group = groupOpt.get();
        
        // 检查是否已经是成员
        if (studyGroupMemberRepository.existsByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)) {
            return false;
        }
        
        // 检查人数限制
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            return false;
        }
        
        // 添加成员
        StudyGroupMember member = new StudyGroupMember(groupId, userId, StudyGroupMember.MemberRole.MEMBER);
        studyGroupMemberRepository.save(member);
        
        // 更新小组成员数
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        studyGroupRepository.save(group);
        
        return true;
    }
    
    /**
     * 通过邀请码加入小组
     */
    public boolean joinGroupByInviteCode(String inviteCode, Long userId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findByInviteCode(inviteCode);
        if (!groupOpt.isPresent()) {
            return false;
        }
        
        return joinGroup(groupOpt.get().getId(), userId);
    }
    
    /**
     * 退出学习小组
     */
    public boolean leaveGroup(Long groupId, Long userId) {
        Optional<StudyGroupMember> memberOpt = studyGroupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (!memberOpt.isPresent() || !memberOpt.get().getIsActive()) {
            return false;
        }
        
        StudyGroupMember member = memberOpt.get();
        
        // 创建者不能退出，只能解散小组
        if (member.getRole() == StudyGroupMember.MemberRole.CREATOR) {
            return false;
        }
        
        // 软删除成员
        member.setIsActive(false);
        studyGroupMemberRepository.save(member);
        
        // 更新小组成员数
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            StudyGroup group = groupOpt.get();
            group.setCurrentMembers(group.getCurrentMembers() - 1);
            studyGroupRepository.save(group);
        }
        
        return true;
    }
    
    /**
     * 解散学习小组
     */
    public boolean disbandGroup(Long groupId, Long userId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            return false;
        }
        
        StudyGroup group = groupOpt.get();
        
        // 只有创建者可以解散小组
        if (!group.getCreatorId().equals(userId)) {
            return false;
        }
        
        // 更新小组状态
        group.setStatus(StudyGroup.GroupStatus.DISBANDED);
        studyGroupRepository.save(group);
        
        return true;
    }
    
    /**
     * 获取公开小组列表
     */
    public Page<StudyGroup> getPublicGroups(Pageable pageable) {
        return studyGroupRepository.findByIsPublicTrueAndStatus(
            StudyGroup.GroupStatus.ACTIVE, pageable);
    }
    
    /**
     * 根据学科搜索小组
     */
    public Page<StudyGroup> searchGroupsBySubject(String subject, Pageable pageable) {
        return studyGroupRepository.findBySubjectAndIsPublicTrueAndStatus(
            subject, StudyGroup.GroupStatus.ACTIVE, pageable);
    }
    
    /**
     * 根据名称搜索小组
     */
    public Page<StudyGroup> searchGroupsByName(String keyword, Pageable pageable) {
        return studyGroupRepository.findByNameContainingAndIsPublicTrueAndStatus(
            keyword, StudyGroup.GroupStatus.ACTIVE, pageable);
    }
    
    /**
     * 获取热门小组
     */
    public Page<StudyGroup> getPopularGroups(Pageable pageable) {
        return studyGroupRepository.findPopularGroups(
            StudyGroup.GroupStatus.ACTIVE, pageable);
    }
    
    /**
     * 获取用户加入的小组
     */
    public List<StudyGroupMember> getUserGroups(Long userId) {
        return studyGroupMemberRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * 获取小组成员列表
     */
    public List<StudyGroupMember> getGroupMembers(Long groupId) {
        return studyGroupMemberRepository.findByGroupIdAndIsActiveTrue(groupId);
    }
    
    /**
     * 获取小组详情
     */
    public Optional<StudyGroup> getGroupById(Long groupId) {
        return studyGroupRepository.findById(groupId);
    }
    
    /**
     * 更新小组信息
     */
    public StudyGroup updateGroup(StudyGroup group) {
        return studyGroupRepository.save(group);
    }
    
    /**
     * 获取小组共享任务
     */
    public List<Task> getGroupSharedTasks(Long groupId, Long userId) {
        // 检查用户是否是小组成员
        if (!studyGroupMemberRepository.existsByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)) {
            return List.of();
        }
        
        // 检查小组是否开启任务共享
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (!groupOpt.isPresent() || !groupOpt.get().getTaskSharingEnabled()) {
            return List.of();
        }
        
        // 获取小组所有成员的用户ID
        List<StudyGroupMember> members = getGroupMembers(groupId);
        List<Long> memberUserIds = members.stream()
                .map(StudyGroupMember::getUserId)
                .collect(Collectors.toList());
        
        // 获取所有成员的任务
        return taskService.getTasksByUserIds(memberUserIds);
    }
    
    /**
     * 获取小组共享资料
     */
    public List<LearningMaterial> getGroupSharedMaterials(Long groupId, Long userId) {
        // 检查用户是否是小组成员
        if (!studyGroupMemberRepository.existsByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)) {
            return List.of();
        }
        
        // 检查小组是否开启资料库共享
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (!groupOpt.isPresent() || !groupOpt.get().getResourceSharingEnabled()) {
            return List.of();
        }
        
        // 获取小组所有成员的用户ID
        List<StudyGroupMember> members = getGroupMembers(groupId);
        List<Long> memberUserIds = members.stream()
                .map(StudyGroupMember::getUserId)
                .collect(Collectors.toList());
        
        // 获取所有成员的资料
        return fileStorageService.getMaterialsByUserIds(memberUserIds);
    }
    
    /**
     * 根据学科获取小组共享资料
     */
    public List<LearningMaterial> getGroupSharedMaterialsBySubject(Long groupId, Long userId, String subject) {
        // 检查用户是否是小组成员
        if (!studyGroupMemberRepository.existsByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)) {
            return List.of();
        }
        
        // 检查小组是否开启资料库共享
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (!groupOpt.isPresent() || !groupOpt.get().getResourceSharingEnabled()) {
            return List.of();
        }
        
        // 获取小组所有成员的用户ID
        List<StudyGroupMember> members = getGroupMembers(groupId);
        List<Long> memberUserIds = members.stream()
                .map(StudyGroupMember::getUserId)
                .collect(Collectors.toList());
        
        // 获取所有成员指定学科的资料
        return fileStorageService.getMaterialsByUserIdsAndSubject(memberUserIds, subject);
    }
    
    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        do {
            code.setLength(0);
            for (int i = 0; i < 8; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (studyGroupRepository.findByInviteCode(code.toString()).isPresent());
        
        return code.toString();
    }
}