package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.StudyGroup;
import com.example.learning.learning_habit_plan_backend.entity.StudyGroupMember;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import com.example.learning.learning_habit_plan_backend.service.StudyGroupService;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/study-groups")
@Tag(name = "学习小组管理", description = "学习小组相关API")
public class StudyGroupController {
    
    @Autowired
    private StudyGroupService studyGroupService;
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "创建学习小组")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody StudyGroup group) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置创建者ID
            group.setCreatorId(currentUser.getId());
            
            StudyGroup createdGroup = studyGroupService.createGroup(group);
            response.put("success", true);
            response.put("message", "学习小组创建成功");
            response.put("data", createdGroup);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取公开小组列表")
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyGroup> groups = studyGroupService.getPublicGroups(pageable);
            
            response.put("success", true);
            response.put("data", groups.getContent());
            response.put("totalElements", groups.getTotalElements());
            response.put("totalPages", groups.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据学科搜索小组")
    @GetMapping("/search/subject")
    public ResponseEntity<Map<String, Object>> searchGroupsBySubject(
            @RequestParam String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyGroup> groups = studyGroupService.searchGroupsBySubject(subject, pageable);
            
            response.put("success", true);
            response.put("data", groups.getContent());
            response.put("totalElements", groups.getTotalElements());
            response.put("totalPages", groups.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据名称搜索小组")
    @GetMapping("/search/name")
    public ResponseEntity<Map<String, Object>> searchGroupsByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyGroup> groups = studyGroupService.searchGroupsByName(keyword, pageable);
            
            response.put("success", true);
            response.put("data", groups.getContent());
            response.put("totalElements", groups.getTotalElements());
            response.put("totalPages", groups.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取热门小组")
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyGroup> groups = studyGroupService.getPopularGroups(pageable);
            
            response.put("success", true);
            response.put("data", groups.getContent());
            response.put("totalElements", groups.getTotalElements());
            response.put("totalPages", groups.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "加入学习小组")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, Object>> joinGroup(@PathVariable Long groupId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = studyGroupService.joinGroup(groupId, currentUser.getId());
            if (success) {
                response.put("success", true);
                response.put("message", "成功加入学习小组");
            } else {
                response.put("success", false);
                response.put("message", "加入失败，可能已经是成员或小组已满");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "加入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "通过邀请码加入小组")
    @PostMapping("/join-by-code")
    public ResponseEntity<Map<String, Object>> joinGroupByInviteCode(@RequestParam String inviteCode) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = studyGroupService.joinGroupByInviteCode(inviteCode, currentUser.getId());
            if (success) {
                response.put("success", true);
                response.put("message", "成功加入学习小组");
            } else {
                response.put("success", false);
                response.put("message", "邀请码无效或加入失败");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "加入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "退出学习小组")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Map<String, Object>> leaveGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = studyGroupService.leaveGroup(groupId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "成功退出学习小组");
            } else {
                response.put("success", false);
                response.put("message", "退出失败，创建者不能退出小组");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "退出失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "解散学习小组")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> disbandGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = studyGroupService.disbandGroup(groupId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "学习小组已解散");
            } else {
                response.put("success", false);
                response.put("message", "解散失败，只有创建者可以解散小组");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "解散失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户加入的小组")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGroups(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<StudyGroupMember> userGroups = studyGroupService.getUserGroups(userId);
            response.put("success", true);
            response.put("data", userGroups);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组成员列表")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable Long groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<StudyGroupMember> members = studyGroupService.getGroupMembers(groupId);
            response.put("success", true);
            response.put("data", members);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组详情")
    @GetMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Long groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<StudyGroup> group = studyGroupService.getGroupById(groupId);
            if (group.isPresent()) {
                response.put("success", true);
                response.put("data", group.get());
            } else {
                response.put("success", false);
                response.put("message", "小组不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "更新小组信息")
    @PutMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> updateGroup(
            @PathVariable Long groupId,
            @RequestBody StudyGroup group) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            group.setId(groupId);
            StudyGroup updatedGroup = studyGroupService.updateGroup(group);
            response.put("success", true);
            response.put("message", "小组信息更新成功");
            response.put("data", updatedGroup);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组共享任务")
    @GetMapping("/{groupId}/shared-tasks")
    public ResponseEntity<Map<String, Object>> getGroupSharedTasks(@PathVariable Long groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Task> sharedTasks = studyGroupService.getGroupSharedTasks(groupId, currentUser.getId());
            response.put("success", true);
            response.put("data", sharedTasks);
            response.put("message", "获取小组共享任务成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组共享资料")
    @GetMapping("/{groupId}/shared-materials")
    public ResponseEntity<Map<String, Object>> getGroupSharedMaterials(@PathVariable Long groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<LearningMaterial> sharedMaterials = studyGroupService.getGroupSharedMaterials(groupId, currentUser.getId());
            response.put("success", true);
            response.put("data", sharedMaterials);
            response.put("message", "获取小组共享资料成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据学科获取小组共享资料")
    @GetMapping("/{groupId}/shared-materials/subject")
    public ResponseEntity<Map<String, Object>> getGroupSharedMaterialsBySubject(
            @PathVariable Long groupId,
            @RequestParam String subject) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录或不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<LearningMaterial> sharedMaterials = studyGroupService.getGroupSharedMaterialsBySubject(groupId, currentUser.getId(), subject);
            response.put("success", true);
            response.put("data", sharedMaterials);
            response.put("message", "获取小组共享资料成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}