package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.FilePreviewResponse;
import com.example.learning.learning_habit_plan_backend.service.FilePreviewService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

@Service
public class FilePreviewServiceImpl implements FilePreviewService {

    private static final Logger logger = Logger.getLogger(FilePreviewServiceImpl.class.getName());

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 支持的文件类型及大小限制（单位：MB）
    private static final int MAX_TEXT_SIZE = 20; // 20MB
    private static final int MAX_PDF_SIZE = 20; // 20MB
    private static final int MAX_OFFICE_SIZE = 20; // 20MB
    private static final int MAX_IMAGE_SIZE = 20; // 20MB
    
    // 图片转换质量
    private static final float PDF_IMAGE_QUALITY = 0.8f; // PDF转图片质量
    private static final int MAX_PDF_PAGES = 10; // 最大预览PDF页数

    @Override
    public FilePreviewResponse previewFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                logger.warning("文件不存在: " + filePath);
                return FilePreviewResponse.error(fileName, "文件不存在");
            }

            String fileExtension = getFileExtension(fileName).toLowerCase();
            long fileSize = Files.size(filePath) / (1024 * 1024); // 转换为MB
            
            logger.info("预览文件: " + fileName + ", 类型: " + fileExtension + ", 大小: " + fileSize + "MB");

            // 根据文件类型应用不同限制和处理方法
            switch (fileExtension) {
                case "txt":
                    if (fileSize > MAX_TEXT_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("文本文件超过%dMB限制，请下载查看", MAX_TEXT_SIZE));
                    }
                    return previewTextFile(filePath, fileName, fileExtension);
                    
                case "pdf":
                    if (fileSize > MAX_PDF_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("PDF文件超过%dMB限制，请下载查看", MAX_PDF_SIZE));
                    }
                    return previewPdfFile(filePath, fileName, fileExtension);
                    
                case "doc":
                case "docx":
                    if (fileSize > MAX_OFFICE_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("Word文件超过%dMB限制，请下载查看", MAX_OFFICE_SIZE));
                    }
                    return previewWordFile(filePath, fileName, fileExtension);
                    
                case "ppt":
                case "pptx":
                    if (fileSize > MAX_OFFICE_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("PowerPoint文件超过%dMB限制，请下载查看", MAX_OFFICE_SIZE));
                    }
                    return previewPptFile(filePath, fileName, fileExtension);
                    
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                    if (fileSize > MAX_IMAGE_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("图片文件超过%dMB限制，请下载查看", MAX_IMAGE_SIZE));
                    }
                    return previewImageFile(filePath, fileName, fileExtension);
                    
                default:
                    logger.warning("不支持的文件类型: " + fileExtension);
                    return FilePreviewResponse.error(fileName, "不支持的文件类型: " + fileExtension);
            }

        } catch (Exception e) {
            logger.severe("文件预览失败: " + e.getMessage());
            e.printStackTrace();
            return FilePreviewResponse.error(fileName, "文件预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 预览文本文件
     */
    private FilePreviewResponse previewTextFile(Path filePath, String fileName, String fileExtension) throws IOException {
        // 读取文本内容
        String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        
        // 返回文本内容的Base64编码
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8))
        );
    }
    
    /**
     * 预览PDF文件 - 转换为图片序列
     */
    private FilePreviewResponse previewPdfFile(Path filePath, String fileName, String fileExtension) throws IOException {
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = Math.min(document.getNumberOfPages(), MAX_PDF_PAGES);
            
            // 如果页数超过限制，记录日志
            if (document.getNumberOfPages() > MAX_PDF_PAGES) {
                logger.info("PDF页数超过限制，仅预览前" + MAX_PDF_PAGES + "页: " + fileName);
            }
            
            // 将PDF页面渲染为图片并转为Base64
            List<String> pageImages = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150); // 150 DPI提供较好的质量和大小平衡
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                pageImages.add(Base64.getEncoder().encodeToString(baos.toByteArray()));
            }
            
            // 将所有页面的Base64编码图片合并为一个JSON数组字符串
            String pdfContent = String.join(",", pageImages);
            
            return FilePreviewResponse.success(
                    fileName,
                    fileExtension,
                    pdfContent
            );
        }
    }
    
    /**
     * 预览Word文件 - 提取文本内容
     */
    private FilePreviewResponse previewWordFile(Path filePath, String fileName, String fileExtension) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (InputStream is = Files.newInputStream(filePath)) {
            if ("doc".equals(fileExtension)) {
                // 处理DOC文件
                HWPFDocument doc = new HWPFDocument(is);
                content.append(doc.getDocumentText());
            } else {
                // 处理DOCX文件
                XWPFDocument docx = new XWPFDocument(is);
                docx.getParagraphs().forEach(paragraph -> {
                    content.append(paragraph.getText()).append("\n");
                });
            }
        }
        
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(content.toString().getBytes(StandardCharsets.UTF_8))
        );
    }
    
    /**
     * 预览PowerPoint文件 - 转换为图片序列
     */
    private FilePreviewResponse previewPptFile(Path filePath, String fileName, String fileExtension) throws IOException {
        List<String> slideImages = new ArrayList<>();
        
        try (InputStream is = Files.newInputStream(filePath)) {
            SlideShow<?, ?> slideShow;
            
            if ("ppt".equals(fileExtension)) {
                // 处理PPT文件
                slideShow = new HSLFSlideShow(is);
            } else {
                // 处理PPTX文件
                slideShow = new XMLSlideShow(is);
            }
            
            // 转换幻灯片为图片
            for (int i = 0; i < slideShow.getSlides().size(); i++) {
                java.awt.Dimension pgsize = slideShow.getPageSize();
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D graphics = img.createGraphics();
                
                // 设置渲染质量
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                
                // 填充背景
                graphics.setColor(java.awt.Color.WHITE);
                graphics.fill(new java.awt.Rectangle(0, 0, pgsize.width, pgsize.height));
                
                // 绘制幻灯片
                slideShow.getSlides().get(i).draw(graphics);
                
                // 转换为Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                slideImages.add(Base64.getEncoder().encodeToString(baos.toByteArray()));
                
                graphics.dispose();
            }
        }
        
        // 将所有幻灯片的Base64编码图片合并为一个JSON数组字符串
        String pptContent = String.join(",", slideImages);
        
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                pptContent
        );
    }
    
    /**
     * 预览图片文件
     */
    private FilePreviewResponse previewImageFile(Path filePath, String fileName, String fileExtension) throws IOException {
        // 直接读取图片文件并转为Base64
        byte[] fileContent = Files.readAllBytes(filePath);
        
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(fileContent)
        );
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

    private boolean isOfficeFile(String extension) {
        return extension.matches("doc|docx|ppt|pptx");
    }
}
