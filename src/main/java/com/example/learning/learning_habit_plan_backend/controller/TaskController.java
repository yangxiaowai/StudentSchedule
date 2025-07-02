package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.Task;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.service.TaskService;
import com.example.learning.learning_habit_plan_backend.service.UserService;
// 添加以下两行导入
import com.example.learning.learning_habit_plan_backend.service.FileStorageService;
import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import com.example.learning.learning_habit_plan_backend.model.ErrorResponse;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    // 添加FileStorageService依赖
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestParam("name") String name,
            @RequestParam("subject") String subject,
            @RequestParam("content") String content,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "progress", required = false) Integer progress,
            @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "fileUrl", required = false) String fileUrl
    ) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再创建任务");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            Task task = new Task();
            task.setName(name);
            task.setSubject(subject);
            task.setContent(content);
            task.setStartTime(startTime != null && !startTime.isEmpty() ? LocalDateTime.parse(startTime) : null);
            task.setEndTime(endTime != null && !endTime.isEmpty() ? LocalDateTime.parse(endTime) : null);
            task.setContentType(type);  // 添加这一行
            task.setRemark(remark);
            task.setProgress(progress);
            task.setCompleted(isCompleted);
            // 设置用户ID
            task.setUserId(currentUser.getId());

            // 处理文件信息 - 修改这部分逻辑
            if (file != null && !file.isEmpty()) {
                // 使用FileStorageService统一处理文件上传
                FileUploadResponse fileResponse = fileStorageService.storeFile(file, subject, "task");
                
                // 设置任务的文件信息
                task.setFileName(fileResponse.getFileName());
                task.setFileUrl(fileResponse.getFileDownloadUri());
                task.setFilePath(fileResponse.getFileDownloadUri());
            } else if (fileName != null && !fileName.isEmpty() && fileUrl != null && !fileUrl.isEmpty()) {
                // 如果前端已经上传了文件并提供了文件信息，直接使用
                task.setFileName(fileName);
                task.setFileUrl(fileUrl);
                task.setFilePath(fileUrl);
            }

            Task saved = taskService.save(task);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse("保存任务失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

private static final String UPLOAD_DIR = "D:/upload_files/"; 

private String saveUploadedFile(MultipartFile file) throws IOException {
    File dir = new File(UPLOAD_DIR);
    if (!dir.exists()) {
        dir.mkdirs();
    }
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    String filePath = UPLOAD_DIR + fileName;
    File dest = new File(filePath);
    file.transferTo(dest);

    // 返回给前端一个相对访问路径，比如 /uploads/ 文件名（前提是你要配置静态资源映射）
    return "/uploads/" + fileName;
}

@GetMapping
public ResponseEntity<?> getAllTasks() {
    try {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);
        
        if (currentUser == null) {
            ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再查看任务");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // 只获取当前用户的任务
        List<Task> tasks = taskService.getTasksByUserId(currentUser.getId());
        return ResponseEntity.ok(tasks);
    } catch (Exception e) {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse("获取任务失败", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

@PutMapping("/{id}")
public ResponseEntity<?> updateTask(
        @PathVariable Long id,
        @RequestParam("name") String name,
        @RequestParam("subject") String subject,
        @RequestParam("content") String content,
        @RequestParam("startTime") String startTime,
        @RequestParam("endTime") String endTime,
        @RequestParam(value = "type", required = false) String type,
        @RequestParam(value = "remark", required = false) String remark,
        @RequestParam(value = "progress", required = false) Integer progress,
        @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "fileUrl", required = false) String fileUrl
) {
    try {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);
        
        if (currentUser == null) {
            ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再更新任务");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Task task = taskService.findById(id);
        if (task == null) {
            ErrorResponse errorResponse = new ErrorResponse("任务不存在", "找不到ID为" + id + "的任务");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // 验证用户权限，只允许用户更新自己的任务
        if (!task.getUserId().equals(currentUser.getId())) {
            ErrorResponse errorResponse = new ErrorResponse("权限不足", "您只能更新自己的任务");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        task.setName(name);
        task.setSubject(subject);
        task.setContent(content);
        task.setStartTime(startTime != null && !startTime.isEmpty() ? LocalDateTime.parse(startTime) : null);
        task.setEndTime(endTime != null && !endTime.isEmpty() ? LocalDateTime.parse(endTime) : null);
        task.setContentType(type);  // 添加这一行
        task.setRemark(remark);
        task.setProgress(progress);
        task.setCompleted(isCompleted);

        // 处理文件信息
        if (file != null && !file.isEmpty()) {
            // 如果有新文件上传，保存文件并更新路径
            String filePath = saveUploadedFile(file);
            task.setFilePath(filePath);
            task.setFileUrl(filePath);
            task.setFileName(file.getOriginalFilename());
        } else if (fileName != null && !fileName.isEmpty() && fileUrl != null && !fileUrl.isEmpty()) {
            // 如果前端提供了文件信息，更新文件信息
            task.setFileName(fileName);
            task.setFileUrl(fileUrl);
        } else if (fileName == null && fileUrl == null) {
            // 如果前端明确传递了null值，表示要清除文件信息
            task.setFileName(null);
            task.setFileUrl(null);
            task.setFilePath(null);
        }

        Task updated = taskService.save(task);
        return ResponseEntity.ok(updated);
    } catch (Exception e) {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse("更新任务失败", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

@DeleteMapping("/{id}")
public ResponseEntity<?> deleteTask(@PathVariable Long id) {
    try {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);
        
        if (currentUser == null) {
            ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再删除任务");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Task task = taskService.findById(id);
        if (task == null) {
            ErrorResponse errorResponse = new ErrorResponse("任务不存在", "找不到ID为" + id + "的任务");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // 验证用户权限，只允许用户删除自己的任务
        if (!task.getUserId().equals(currentUser.getId())) {
            ErrorResponse errorResponse = new ErrorResponse("权限不足", "您只能删除自己的任务");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        taskService.deleteById(id);
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse("删除任务失败", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

@PutMapping("/{id}/progress")
public ResponseEntity<?> updateTaskProgress(@PathVariable Long id, @RequestBody ProgressUpdateRequest request) {
    try {
        Task task = taskService.findById(id);
        if (task == null) {
            ErrorResponse errorResponse = new ErrorResponse("任务不存在", "找不到ID为 " + id + " 的任务");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        task.setProgress(request.getProgress());
        
        // 如果进度达到100%，自动标记为已完成
        if (request.getProgress() != null && request.getProgress() >= 100) {
            task.setCompleted(true);
        }
        
        Task updated = taskService.save(task);
        return ResponseEntity.ok(updated);
    } catch (Exception e) {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse("更新任务进度失败", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

// 进度更新请求的内部类
public static class ProgressUpdateRequest {
    private Integer progress;
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}

@PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        // 使用统一的上传目录
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = UPLOAD_DIR + fileName;
        File dest = new File(filePath);

        file.transferTo(dest);

        // 返回访问URL，前端可以通过 http://localhost:8080/uploads/ 文件名 访问
        return ResponseEntity.ok("/uploads/" + fileName);
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败");
    }
}

@GetMapping("/user/{userId}")
public ResponseEntity<?> getUserTasks(@PathVariable Long userId) {
    try {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);
        
        if (currentUser == null) {
            ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再查看任务");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // 验证目标用户是否存在
        User targetUser = userService.findById(userId);
        if (targetUser == null) {
            ErrorResponse errorResponse = new ErrorResponse("用户不存在", "找不到ID为" + userId + "的用户");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // 获取指定用户的任务
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    } catch (Exception e) {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse("获取任务失败", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

}