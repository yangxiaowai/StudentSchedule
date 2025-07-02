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
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

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
    private static final int MAX_TEXT_SIZE = 10; // 10MB - 优化性能
    private static final int MAX_PDF_SIZE = 50; // 50MB - PDF可以更大
    private static final int MAX_OFFICE_SIZE = 30; // 30MB - Office文件适中
    private static final int MAX_IMAGE_SIZE = 20; // 20MB
    
    // 性能优化参数

    private static final int MAX_PDF_PAGES = 20; // 最大预览PDF页数
    private static final int PDF_DPI = 120; // PDF渲染DPI，降低以提升速度
    private static final int MAX_TEXT_PREVIEW_SIZE = 1024 * 1024; // 文本预览最大1MB
    
    // 缓存相关（简单内存缓存）
    private static final java.util.Map<String, FilePreviewResponse> previewCache = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟缓存

    @Override
    public FilePreviewResponse previewFile(String fileName) {
        try {
            // 检查缓存
            String cacheKey = fileName + "_" + System.currentTimeMillis() / CACHE_EXPIRE_TIME;
            FilePreviewResponse cached = previewCache.get(cacheKey);
            if (cached != null) {
                logger.info("从缓存返回预览: " + fileName);
                return cached;
            }
            
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                logger.warning("文件不存在: " + filePath);
                return FilePreviewResponse.error(fileName, "文件不存在");
            }

            String fileExtension = getFileExtension(fileName).toLowerCase();
            long fileSize = Files.size(filePath) / (1024 * 1024); // 转换为MB
            long fileSizeBytes = Files.size(filePath);
            
            logger.info("预览文件: " + fileName + ", 类型: " + fileExtension + ", 大小: " + fileSize + "MB");
            
            // 快速检查文件是否过大
            if (fileSizeBytes > 100 * 1024 * 1024) { // 100MB绝对限制
                return FilePreviewResponse.error(fileName, "文件过大，无法预览");
            }

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
                    
                case "xls":
                case "xlsx":
                    if (fileSize > MAX_OFFICE_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("Excel文件超过%dMB限制，请下载查看", MAX_OFFICE_SIZE));
                    }
                    return previewExcelFile(filePath, fileName, fileExtension);
                    
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                case "webp":
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
     * 缓存预览结果
     */
    private void cachePreviewResult(String fileName, FilePreviewResponse response) {
        try {
            String cacheKey = fileName + "_" + System.currentTimeMillis() / CACHE_EXPIRE_TIME;
            previewCache.put(cacheKey, response);
            
            // 清理过期缓存
            if (previewCache.size() > 100) {
                previewCache.clear();
            }
        } catch (Exception e) {
            logger.warning("缓存预览结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 预览文本文件（TXT等）- 优化版本，支持智能格式化
     */
    private FilePreviewResponse previewTextFile(Path filePath, String fileName, String fileExtension) throws IOException {
        // 读取文本内容，对大文件进行截断
        byte[] fileBytes = Files.readAllBytes(filePath);
        String rawContent;
        boolean isTruncated = false;
        
        if (fileBytes.length > MAX_TEXT_PREVIEW_SIZE) {
            // 截断大文件，只显示前面部分
            byte[] truncatedBytes = new byte[MAX_TEXT_PREVIEW_SIZE];
            System.arraycopy(fileBytes, 0, truncatedBytes, 0, MAX_TEXT_PREVIEW_SIZE);
            rawContent = new String(truncatedBytes, StandardCharsets.UTF_8);
            isTruncated = true;
        } else {
            rawContent = new String(fileBytes, StandardCharsets.UTF_8);
        }
        
        // 智能格式化文本内容为HTML
        String htmlContent = formatTextToHtml(rawContent, fileName, isTruncated);
        
        // 返回HTML内容的Base64编码
        FilePreviewResponse response = FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(htmlContent.getBytes(StandardCharsets.UTF_8))
        );
        
        // 缓存结果
        cachePreviewResult(fileName, response);
        return response;
    }
    
    /**
     * 将纯文本格式化为HTML，提供更好的阅读体验
     */
    private String formatTextToHtml(String rawContent, String fileName, boolean isTruncated) {
        StringBuilder htmlContent = new StringBuilder();
        
        // HTML文档头部和样式
        htmlContent.append("<div style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f8f9fa; color: #333;'>")
                  .append("<div style='background: white; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); overflow: hidden;'>")
                  .append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 16px 20px; border-bottom: 1px solid #e1e5e9;'>")
                  .append("<h3 style='margin: 0; font-size: 16px; font-weight: 600; display: flex; align-items: center;'>")
                  .append("<span style='margin-right: 8px;'>📄</span>")
                  .append(escapeHtml(fileName))
                  .append("</h3></div>")
                  .append("<div style='padding: 20px;'>");
        
        // 检测文本类型并应用相应格式化
        String formattedContent = detectAndFormatText(rawContent);
        htmlContent.append(formattedContent);
        
        // 如果文件被截断，添加提示信息
        if (isTruncated) {
            htmlContent.append("<div style='margin-top: 30px; padding: 16px; background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%); border-radius: 8px; border-left: 4px solid #e17055;'>")
                      .append("<div style='display: flex; align-items: center; color: #2d3436;'>")
                      .append("<span style='font-size: 20px; margin-right: 10px;'>⚠️</span>")
                      .append("<div>")
                      .append("<strong>文件过大提示</strong><br>")
                      .append("<span style='font-size: 14px;'>仅显示前 ").append(MAX_TEXT_PREVIEW_SIZE / 1024).append(" KB 内容，完整内容请下载文件查看</span>")
                      .append("</div></div></div>");
        }
        
        htmlContent.append("</div></div></div>");
        
        return htmlContent.toString();
    }
    
    /**
     * 检测文本类型并应用相应的格式化
     */
    private String detectAndFormatText(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "<div style='text-align: center; color: #6c757d; padding: 40px;'><i style='font-size: 48px; margin-bottom: 16px; display: block;'>📄</i><p style='margin: 0;'>文件内容为空</p></div>";
        }
        
        // 检测是否为代码文件（基于内容特征）
        if (isCodeLikeContent(content)) {
            return formatAsCode(content);
        }
        
        // 检测是否为结构化文档（如Markdown、配置文件等）
        if (isStructuredDocument(content)) {
            return formatAsStructuredDocument(content);
        }
        
        // 检测是否为日志文件
        if (isLogFile(content)) {
            return formatAsLog(content);
        }
        
        // 默认作为普通文本处理
        return formatAsPlainText(content);
    }
    
    /**
     * 检测是否为代码类内容
     */
    private boolean isCodeLikeContent(String content) {
        // 检查代码特征
        String[] codeIndicators = {
            "function", "class", "import", "#include", "<?php", "<script", "def ", "public class",
            "private ", "public ", "protected ", "static ", "const ", "var ", "let ", "if (", "for (",
            "while (", "switch (", "try {", "catch (", "finally {", "return ", "throw ", "new ",
            "@Override", "@Component", "@Service", "@Controller", "package ", "namespace "
        };
        
        String lowerContent = content.toLowerCase();
        int codeFeatures = 0;
        
        for (String indicator : codeIndicators) {
            if (lowerContent.contains(indicator.toLowerCase())) {
                codeFeatures++;
            }
        }
        
        // 检查括号和分号的使用频率
        long braceCount = content.chars().filter(ch -> ch == '{' || ch == '}').count();
        long semicolonCount = content.chars().filter(ch -> ch == ';').count();
        long parenCount = content.chars().filter(ch -> ch == '(' || ch == ')').count();
        
        return codeFeatures >= 3 || (braceCount > 5 && semicolonCount > 5) || parenCount > 10;
    }
    
    /**
     * 检测是否为结构化文档
     */
    private boolean isStructuredDocument(String content) {
        // 检查Markdown特征
        return content.contains("# ") || content.contains("## ") || content.contains("### ") ||
               content.contains("* ") || content.contains("- ") || content.contains("1. ") ||
               content.contains("**") || content.contains("__") || content.contains("```") ||
               content.contains("[TOC]") || content.contains("---");
    }
    
    /**
     * 检测是否为日志文件
     */
    private boolean isLogFile(String content) {
        String[] logIndicators = {
            "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "FATAL",
            "Exception", "Stack trace", "at ", "Caused by",
            "[ERROR]", "[WARN]", "[INFO]", "[DEBUG]"
        };
        
        int logFeatures = 0;
        for (String indicator : logIndicators) {
            if (content.contains(indicator)) {
                logFeatures++;
            }
        }
        
        // 检查时间戳模式
        boolean hasTimestamp = content.matches(".*\\d{4}-\\d{2}-\\d{2}.*") || 
                              content.matches(".*\\d{2}:\\d{2}:\\d{2}.*");
        
        return logFeatures >= 2 || (logFeatures >= 1 && hasTimestamp);
    }
    
    /**
     * 格式化为代码显示
     */
    private String formatAsCode(String content) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; overflow: hidden;'>")
            .append("<div style='background: #e9ecef; padding: 8px 16px; border-bottom: 1px solid #dee2e6; font-size: 12px; color: #6c757d; font-weight: 600;'>")
            .append("💻 代码文件")
            .append("</div>")
            .append("<pre style='margin: 0; padding: 16px; overflow-x: auto; font-family: \"SFMono-Regular\", Consolas, \"Liberation Mono\", Menlo, monospace; font-size: 13px; line-height: 1.45; background: #ffffff;'>")
            .append("<code style='color: #24292e;'>")
            .append(escapeHtml(content))
            .append("</code></pre></div>");
        
        return html.toString();
    }
    
    /**
     * 格式化为结构化文档
     */
    private String formatAsStructuredDocument(String content) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; overflow: hidden;'>")
            .append("<div style='background: #e9ecef; padding: 8px 16px; border-bottom: 1px solid #dee2e6; font-size: 12px; color: #6c757d; font-weight: 600;'>")
            .append("📝 结构化文档")
            .append("</div>")
            .append("<div style='padding: 16px; background: #ffffff;'>");
        
        // 简单的Markdown-like格式化
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                html.append("<br>");
                continue;
            }
            
            if (line.startsWith("# ")) {
                html.append("<h1 style='color: #2c3e50; margin: 20px 0 10px 0; font-size: 24px; font-weight: bold; border-bottom: 2px solid #3498db; padding-bottom: 8px;'>")
                    .append(escapeHtml(line.substring(2)))
                    .append("</h1>");
            } else if (line.startsWith("## ")) {
                html.append("<h2 style='color: #2c3e50; margin: 18px 0 8px 0; font-size: 20px; font-weight: bold;'>")
                    .append(escapeHtml(line.substring(3)))
                    .append("</h2>");
            } else if (line.startsWith("### ")) {
                html.append("<h3 style='color: #2c3e50; margin: 16px 0 6px 0; font-size: 16px; font-weight: bold;'>")
                    .append(escapeHtml(line.substring(4)))
                    .append("</h3>");
            } else if (line.startsWith("* ") || line.startsWith("- ")) {
                html.append("<li style='margin: 4px 0; color: #333; line-height: 1.6;'>")
                    .append(escapeHtml(line.substring(2)))
                    .append("</li>");
            } else if (line.matches("\\d+\\. .*")) {
                html.append("<li style='margin: 4px 0; color: #333; line-height: 1.6; list-style-type: decimal;'>")
                    .append(escapeHtml(line.replaceFirst("\\d+\\. ", "")))
                    .append("</li>");
            } else {
                html.append("<p style='margin: 8px 0; color: #333; line-height: 1.6;'>")
                    .append(escapeHtml(line))
                    .append("</p>");
            }
        }
        
        html.append("</div></div>");
        return html.toString();
    }
    
    /**
     * 格式化为日志显示
     */
    private String formatAsLog(String content) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; overflow: hidden;'>")
            .append("<div style='background: #e9ecef; padding: 8px 16px; border-bottom: 1px solid #dee2e6; font-size: 12px; color: #6c757d; font-weight: 600;'>")
            .append("📋 日志文件")
            .append("</div>")
            .append("<div style='padding: 16px; background: #ffffff; max-height: 600px; overflow-y: auto;'>");
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            String lineStyle = "margin: 2px 0; font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace; font-size: 12px; line-height: 1.4; padding: 2px 0;";
            
            if (line.contains("ERROR") || line.contains("FATAL") || line.contains("Exception")) {
                lineStyle += " color: #dc3545; background-color: #f8d7da; padding: 2px 4px; border-radius: 3px;";
            } else if (line.contains("WARN")) {
                lineStyle += " color: #856404; background-color: #fff3cd; padding: 2px 4px; border-radius: 3px;";
            } else if (line.contains("INFO")) {
                lineStyle += " color: #0c5460; background-color: #d1ecf1; padding: 2px 4px; border-radius: 3px;";
            } else if (line.contains("DEBUG") || line.contains("TRACE")) {
                lineStyle += " color: #6c757d;";
            } else {
                lineStyle += " color: #333;";
            }
            
            html.append("<div style='").append(lineStyle).append("'>")
                .append(escapeHtml(line))
                .append("</div>");
        }
        
        html.append("</div></div>");
        return html.toString();
    }
    
    /**
     * 格式化为普通文本
     */
    private String formatAsPlainText(String content) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='background: #ffffff; padding: 20px; border-radius: 6px; border: 1px solid #e9ecef;'>");
        
        String[] paragraphs = content.split("\n\n+");
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim().replaceAll("\n", " ");
            if (paragraph.isEmpty()) {
                continue;
            }
            
            // 检测是否为标题（短文本且可能包含特殊字符）
            if (paragraph.length() < 100 && (paragraph.matches(".*[：:].{0,50}") || 
                paragraph.matches(".*(第.*章|第.*节|\\d+[.、]).*"))) {
                html.append("<h3 style='color: #2c3e50; margin: 20px 0 10px 0; font-size: 18px; font-weight: bold; border-bottom: 1px solid #e9ecef; padding-bottom: 8px;'>")
                    .append(escapeHtml(paragraph))
                    .append("</h3>");
            } else {
                html.append("<p style='margin: 12px 0; color: #333; line-height: 1.8; text-align: justify; text-indent: 2em; font-size: 14px;'>")
                    .append(escapeHtml(paragraph))
                    .append("</p>");
            }
        }
        
        html.append("</div>");
        return html.toString();
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
                // 使用优化的DPI设置提升渲染速度
                BufferedImage image = renderer.renderImageWithDPI(i, PDF_DPI);
                
                // 压缩图片以减少传输大小
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                // 使用JPEG格式减少文件大小（对于PDF预览足够）
                if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                    BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    rgbImage.getGraphics().drawImage(image, 0, 0, null);
                    image = rgbImage;
                }
                
                ImageIO.write(image, "jpeg", baos);
                pageImages.add(Base64.getEncoder().encodeToString(baos.toByteArray()));
            }
            
            // 将所有页面的Base64编码图片合并为一个JSON数组字符串
            String pdfContent = String.join(",", pageImages);
            
            FilePreviewResponse response = FilePreviewResponse.success(
                    fileName,
                    fileExtension,
                    pdfContent
            );
            
            // 缓存结果
            cachePreviewResult(fileName, response);
            return response;
        }
    }
    
    /**
     * 预览Word文件 - 直接转换为HTML
     */
    private FilePreviewResponse previewWordFile(Path filePath, String fileName, String fileExtension) throws IOException {
        try {
            // 统一使用HTML方式预览，确保中文字符正确显示
            return convertDocToHtml(filePath, fileName, fileExtension);
        } catch (Exception e) {
            logger.warning("Word文档转换失败: " + e.getMessage());
            throw new IOException("Word文档预览失败: " + e.getMessage(), e);
        }
    }
    


    /**
     * DOC文件转换为HTML（保留原有逻辑作为备用方案）
     */
    private FilePreviewResponse convertDocToHtml(Path filePath, String fileName, String fileExtension) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px;'>");
        
        try (InputStream is = Files.newInputStream(filePath)) {
            if ("doc".equals(fileExtension)) {
                // 处理DOC文件 - 改进的结构化处理
                HWPFDocument doc = new HWPFDocument(is);
                
                try {
                    // 尝试提取段落信息
                    org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
                    
                    // 检查是否有表格
                    org.apache.poi.hwpf.usermodel.TableIterator tableIterator = new org.apache.poi.hwpf.usermodel.TableIterator(range);
                    boolean hasTables = tableIterator.hasNext();
                    
                    if (hasTables) {
                        // 处理包含表格的文档
                        processDocWithTables(range, htmlContent);
                    } else {
                        // 处理纯文本文档
                        processDocTextOnly(range, htmlContent);
                    }
                    
                } catch (Exception e) {
                    // 如果结构化处理失败，回退到简单文本提取
                    org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
                    String text = range.text();
                    text = cleanWordText(text);
                    
                    String[] paragraphs = text.split("[\r\n]+");
                    for (String paragraph : paragraphs) {
                        paragraph = paragraph.trim();
                        if (!paragraph.isEmpty()) {
                            if (isTitle(paragraph)) {
                                htmlContent.append("<h3 style='color: #2c3e50; margin-top: 25px; margin-bottom: 15px; font-weight: bold; font-size: 18px; border-bottom: 2px solid #3498db; padding-bottom: 5px;'>")
                                          .append(escapeHtml(paragraph))
                                          .append("</h3>");
                            } else {
                                htmlContent.append("<p style='margin-bottom: 15px; text-indent: 2em; line-height: 1.8; color: #333; font-size: 14px;'>")
                                          .append(escapeHtml(paragraph))
                                          .append("</p>");
                            }
                        }
                    }
                }
                
                doc.close();
            } else {
                // 处理DOCX文件 - 改进的中文支持
                XWPFDocument docx = new XWPFDocument(is);
                
                // 获取所有段落
                List<XWPFParagraph> paragraphs = docx.getParagraphs();
                
                for (XWPFParagraph paragraph : paragraphs) {
                    String text = paragraph.getText();
                    
                    // 跳过空段落
                    if (text == null || text.trim().isEmpty()) {
                        continue;
                    }
                    
                    text = text.trim();
                    
                    // HTML转义，确保特殊字符正确显示
                    text = escapeHtml(text);
                    
                    // 检测标题样式
                    String styleName = paragraph.getStyle();
                    boolean isHeading = false;
                    
                    if (styleName != null) {
                        String lowerStyle = styleName.toLowerCase();
                        isHeading = lowerStyle.contains("heading") || lowerStyle.contains("title") || lowerStyle.contains("标题");
                    }
                    
                    // 检查段落格式
                    if (!isHeading) {
                        // 通过文本特征判断是否为标题
                        if (text.length() < 50 && (
                            text.matches(".*\\d+[.、].*") || 
                            text.matches(".*[一二三四五六七八九十]+[、.].*") ||
                            text.matches("第.*章.*") ||
                            text.matches(".*摘要.*") ||
                            text.matches(".*总结.*") ||
                            text.matches(".*结论.*")
                        )) {
                            isHeading = true;
                        }
                    }
                    
                    if (isHeading) {
                        htmlContent.append("<h3 style='color: #2c3e50; margin-top: 25px; margin-bottom: 15px; font-weight: bold; font-size: 18px; border-bottom: 2px solid #3498db; padding-bottom: 5px;'>")
                                  .append(text)
                                  .append("</h3>");
                    } else {
                        htmlContent.append("<p style='margin-bottom: 15px; text-indent: 2em; line-height: 1.8; color: #333; font-size: 14px;'>")
                                  .append(text)
                                  .append("</p>");
                    }
                }
                
                docx.close();
            }
        }
        
        htmlContent.append("</div>");
        
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(htmlContent.toString().getBytes(StandardCharsets.UTF_8))
        );
    }
    
    /**
     * 预览PowerPoint文件 - 转换为高质量图片序列
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
            
            try {
                int totalSlides = slideShow.getSlides().size();
                int maxSlides = Math.min(totalSlides, 50); // 限制最大幻灯片数量
                
                // 获取原始页面尺寸
                java.awt.Dimension originalSize = slideShow.getPageSize();
                
                // 计算优化后的尺寸（保持宽高比，提高清晰度）
                double scale = Math.min(1200.0 / originalSize.width, 900.0 / originalSize.height);
                scale = Math.max(scale, 1.0); // 确保不会缩小
                
                int scaledWidth = (int) (originalSize.width * scale);
                int scaledHeight = (int) (originalSize.height * scale);
                
                // 转换幻灯片为图片
                for (int i = 0; i < maxSlides; i++) {
                    BufferedImage img = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D graphics = img.createGraphics();
                    
                    try {
                        // 设置高质量渲染
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                        graphics.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                        
                        // 填充白色背景
                        graphics.setColor(java.awt.Color.WHITE);
                        graphics.fillRect(0, 0, scaledWidth, scaledHeight);
                        
                        // 应用缩放
                        graphics.scale(scale, scale);
                        
                        // 绘制幻灯片
                        slideShow.getSlides().get(i).draw(graphics);
                        
                        // 转换为Base64（使用JPEG格式以减小文件大小，同时保持质量）
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        
                        // 创建高质量的JPEG编码器
                        javax.imageio.ImageWriter jpegWriter = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
                        javax.imageio.ImageWriteParam jpegWriteParam = jpegWriter.getDefaultWriteParam();
                        jpegWriteParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                        jpegWriteParam.setCompressionQuality(0.95f); // 高质量
                        
                        javax.imageio.stream.ImageOutputStream imageOutputStream = javax.imageio.ImageIO.createImageOutputStream(baos);
                        jpegWriter.setOutput(imageOutputStream);
                        jpegWriter.write(null, new javax.imageio.IIOImage(img, null, null), jpegWriteParam);
                        
                        imageOutputStream.close();
                        jpegWriter.dispose();
                        
                        slideImages.add(Base64.getEncoder().encodeToString(baos.toByteArray()));
                        
                    } finally {
                        graphics.dispose();
                    }
                }
                
                // 如果幻灯片数量被限制，记录信息
                if (totalSlides > maxSlides) {
                    logger.info("PPT文件 {} 包含 {} 张幻灯片，仅预览前 {} 张", fileName, totalSlides, maxSlides);
                }
                
            } finally {
                // 确保slideShow被正确关闭
                if (slideShow != null) {
                    slideShow.close();
                }
            }
        }
        
        // 将所有幻灯片的Base64编码图片合并为一个JSON数组字符串
        String pptContent = String.join(",", slideImages);
        
        FilePreviewResponse response = FilePreviewResponse.success(
                fileName,
                fileExtension,
                pptContent
        );
        
        // 缓存结果
        cachePreviewResult(fileName, response);
        return response;
    }
    
    /**
     * 预览图片文件
     */
    private FilePreviewResponse previewImageFile(Path filePath, String fileName, String fileExtension) throws IOException {
        // 直接读取图片文件并转为Base64
        byte[] fileContent = Files.readAllBytes(filePath);
        
        FilePreviewResponse response = FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(fileContent)
        );
        
        // 缓存结果
        cachePreviewResult(fileName, response);
        return response;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
    
    /**
     * 预览Excel文件 - 转换为HTML表格（优化版）
     */
    private FilePreviewResponse previewExcelFile(Path filePath, String fileName, String fileExtension) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; padding: 20px; background-color: #f8f9fa;'>");
        
        try (InputStream is = Files.newInputStream(filePath)) {
            Workbook workbook;
            
            if ("xls".equals(fileExtension)) {
                workbook = new HSSFWorkbook(is);
            } else {
                workbook = new XSSFWorkbook(is);
            }
            
            // 限制预览的工作表数量和行数
            int maxSheets = Math.min(workbook.getNumberOfSheets(), 3);
            int maxRows = 150; // 增加到150行
            int maxCols = 15;  // 限制列数
            
            for (int sheetIndex = 0; sheetIndex < maxSheets; sheetIndex++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                if (maxSheets > 1) {
                    htmlContent.append("<div style='margin: 30px 0 15px 0;'>")
                              .append("<h3 style='color: #2c3e50; margin: 0; padding: 12px 16px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 8px 8px 0 0; font-size: 16px; font-weight: 600;'>")
                              .append("📊 工作表: ").append(escapeHtml(sheet.getSheetName()))
                              .append("</h3></div>");
                }
                
                // 表格容器
                htmlContent.append("<div style='background: white; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); overflow: hidden; margin-bottom: 25px;'>")
                          .append("<div style='overflow-x: auto;'>")
                          .append("<table style='border-collapse: collapse; width: 100%; font-size: 13px; min-width: 600px;'>");
                
                int lastRowNum = Math.min(sheet.getLastRowNum(), maxRows - 1);
                boolean hasData = false;
                
                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                    
                    if (row == null) {
                        continue;
                    }
                    
                    // 检查行是否有数据
                    boolean rowHasData = false;
                    for (int cellIndex = 0; cellIndex < Math.min(row.getLastCellNum(), maxCols); cellIndex++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
                        if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                            rowHasData = true;
                            break;
                        }
                    }
                    
                    if (!rowHasData) {
                        continue;
                    }
                    
                    hasData = true;
                    htmlContent.append("<tr>");
                    
                    int actualLastCellNum = Math.min(row.getLastCellNum(), maxCols);
                    
                    for (int cellIndex = 0; cellIndex < actualLastCellNum; cellIndex++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
                        String cellValue = getCellValueAsString(cell);
                        
                        // 基础样式
                        StringBuilder cellStyle = new StringBuilder();
                        cellStyle.append("border: 1px solid #e1e5e9; padding: 10px 12px; vertical-align: top; max-width: 200px; word-wrap: break-word;");
                        
                        // 表头样式
                        if (rowIndex == 0 || isHeaderRow(row, maxCols)) {
                            cellStyle.append(" background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); font-weight: 600; color: #495057; text-align: center;");
                        } else {
                            cellStyle.append(" background-color: #ffffff;");
                        }
                        
                        // 数字右对齐
                        if (isNumericCell(cell)) {
                            cellStyle.append(" text-align: right; font-family: 'Courier New', monospace;");
                        }
                        
                        // 空单元格样式
                        if (cellValue.trim().isEmpty()) {
                            cellStyle.append(" background-color: #f8f9fa;");
                            cellValue = "&nbsp;";
                        }
                        
                        htmlContent.append("<td style='").append(cellStyle).append("'>")
                                  .append(escapeHtml(cellValue))
                                  .append("</td>");
                    }
                    
                    htmlContent.append("</tr>");
                }
                
                htmlContent.append("</table></div>");
                
                // 数据统计信息
                if (hasData) {
                    int totalRows = sheet.getLastRowNum() + 1;
                    int displayedRows = Math.min(totalRows, maxRows);
                    
                    htmlContent.append("<div style='padding: 12px 16px; background-color: #f8f9fa; border-top: 1px solid #e1e5e9; font-size: 12px; color: #6c757d;'>")
                              .append("📈 显示 ").append(displayedRows).append(" / ").append(totalRows).append(" 行数据");
                    
                    if (totalRows > maxRows) {
                        htmlContent.append(" • <span style='color: #dc3545;'>完整数据请下载文件查看</span>");
                    }
                    
                    htmlContent.append("</div>");
                } else {
                    htmlContent.append("<div style='padding: 40px; text-align: center; color: #6c757d;'>")
                              .append("<i style='font-size: 48px; margin-bottom: 16px; display: block;'>📄</i>")
                              .append("<p style='margin: 0; font-size: 14px;'>此工作表暂无数据</p>")
                              .append("</div>");
                }
                
                htmlContent.append("</div>");
            }
            
            workbook.close();
        }
        
        htmlContent.append("</div>");
        
        FilePreviewResponse response = FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(htmlContent.toString().getBytes(StandardCharsets.UTF_8))
        );
        
        // 缓存结果
        cachePreviewResult(fileName, response);
        return response;
    }
    
    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        return sdf.format(cell.getDateCellValue());
                    } else {
                        double numValue = cell.getNumericCellValue();
                        // 如果是整数，不显示小数点
                        if (numValue == Math.floor(numValue)) {
                            return String.valueOf((long) numValue);
                        } else {
                            return String.format("%.2f", numValue);
                        }
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? "是" : "否";
                case FORMULA:
                    try {
                        return getCellValueAsString(cell); // 递归获取计算结果
                    } catch (Exception e) {
                        return "=" + cell.getCellFormula();
                    }
                case BLANK:
                    return "";
                default:
                    return cell.toString();
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 判断是否为数字单元格
     */
    private boolean isNumericCell(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return false;
        }
        
        return cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && 
               !org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell);
    }
    
    /**
     * 判断是否为表头行
     */
    private boolean isHeaderRow(org.apache.poi.ss.usermodel.Row row, int maxCols) {
        if (row == null) {
            return false;
        }
        
        int textCells = 0;
        int totalCells = 0;
        
        for (int i = 0; i < Math.min(row.getLastCellNum(), maxCols); i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                totalCells++;
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    textCells++;
                }
            }
        }
        
        // 如果大部分单元格都是文本，可能是表头
        return totalCells > 0 && (double) textCells / totalCells > 0.7;
    }
    
    /**
     * 处理包含表格的DOC文档
     */
    private void processDocWithTables(org.apache.poi.hwpf.usermodel.Range range, StringBuilder htmlContent) {
        try {
            org.apache.poi.hwpf.usermodel.TableIterator tableIterator = new org.apache.poi.hwpf.usermodel.TableIterator(range);
            int currentPos = 0;
            
            while (tableIterator.hasNext()) {
                org.apache.poi.hwpf.usermodel.Table table = tableIterator.next();
                
                // 处理表格前的文本
                if (table.getStartOffset() > currentPos) {
                    String beforeTableText = range.text().substring(currentPos, table.getStartOffset());
                    processPlainText(beforeTableText, htmlContent);
                }
                
                // 处理表格
                htmlContent.append("<table style='border-collapse: collapse; width: 100%; margin: 20px 0; font-size: 14px;'>");
                
                for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
                    org.apache.poi.hwpf.usermodel.TableRow row = table.getRow(rowIndex);
                    htmlContent.append("<tr>");
                    
                    for (int cellIndex = 0; cellIndex < row.numCells(); cellIndex++) {
                        org.apache.poi.hwpf.usermodel.TableCell cell = row.getCell(cellIndex);
                        String cellText = cell.text().trim();
                        cellText = cleanWordText(cellText);
                        
                        String cellStyle = "border: 1px solid #ddd; padding: 8px 12px; vertical-align: top;";
                        if (rowIndex == 0) {
                            cellStyle += " background-color: #f8f9fa; font-weight: bold;";
                        }
                        
                        htmlContent.append("<td style='").append(cellStyle).append("'>")
                                  .append(escapeHtml(cellText))
                                  .append("</td>");
                    }
                    
                    htmlContent.append("</tr>");
                }
                
                htmlContent.append("</table>");
                currentPos = table.getEndOffset();
            }
            
            // 处理最后一个表格后的文本
            if (currentPos < range.text().length()) {
                String afterTableText = range.text().substring(currentPos);
                processPlainText(afterTableText, htmlContent);
            }
            
        } catch (Exception e) {
            // 如果表格处理失败，回退到纯文本处理
            processDocTextOnly(range, htmlContent);
        }
    }
    
    /**
     * 处理纯文本DOC文档
     */
    private void processDocTextOnly(org.apache.poi.hwpf.usermodel.Range range, StringBuilder htmlContent) {
        String text = range.text();
        processPlainText(text, htmlContent);
    }
    
    /**
     * 处理纯文本内容
     */
    private void processPlainText(String text, StringBuilder htmlContent) {
        text = cleanWordText(text);
        
        String[] paragraphs = text.split("[\r\n]+");
        if (paragraphs.length == 1 && text.length() > 200) {
            // 如果是长文本没有换行，尝试按句号分割
            paragraphs = text.split("(?<=。)\\s*");
        }
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }
            
            if (isTitle(paragraph)) {
                htmlContent.append("<h3 style='color: #2c3e50; margin-top: 25px; margin-bottom: 15px; font-weight: bold; font-size: 18px; border-bottom: 2px solid #3498db; padding-bottom: 5px;'>")
                          .append(escapeHtml(paragraph))
                          .append("</h3>");
            } else if (isList(paragraph)) {
                htmlContent.append("<li style='margin-bottom: 8px; line-height: 1.6; color: #333; font-size: 14px;'>")
                          .append(escapeHtml(paragraph.replaceFirst("^[\\d\\u4e00-\\u9fff]+[.、]\\s*", "")))
                          .append("</li>");
            } else {
                htmlContent.append("<p style='margin-bottom: 15px; text-indent: 2em; line-height: 1.8; color: #333; font-size: 14px;'>")
                          .append(escapeHtml(paragraph))
                          .append("</p>");
            }
        }
    }
    
    /**
     * 改进的标题检测
     */
    private boolean isTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        text = text.trim();
        
        // 长度限制
        if (text.length() > 80) {
            return false;
        }
        
        // 常见标题模式
        return text.matches(".*\\d+[.、].*") ||                    // 数字编号
               text.matches(".*[一二三四五六七八九十]+[、.].*") ||        // 中文数字编号
               text.matches("第.*[章节部分].*") ||                   // 第X章/节/部分
               text.matches(".*(摘要|总结|结论|引言|前言|概述|背景).*") ||  // 常见章节名
               text.matches(".*(目录|参考文献|附录).*") ||            // 文档结构
               (text.length() < 30 && text.matches(".*[：:].{0,20}")) || // 短标题带冒号
               (text.length() < 20 && !text.contains("，") && !text.contains("。")); // 短文本且无标点
    }
    
    /**
     * 检测是否为列表项
     */
    private boolean isList(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        text = text.trim();
        
        return text.matches("^[\\d\\u4e00-\\u9fff]+[.、]\\s*.*") ||  // 数字或中文数字开头的列表
               text.matches("^[\\u2022\\u25cf\\u25cb]\\s*.*") ||      // 项目符号
               text.matches("^[-*+]\\s*.*");                        // 破折号、星号、加号
    }
    
    /**
     * 清理Word文档文本中的特殊字符和控制字符
     */
    private String cleanWordText(String text) {
        if (text == null) {
            return "";
        }
        
        // 移除常见的Word控制字符和特殊字符（更保守的清理）
        text = text.replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]", ""); // 控制字符
        text = text.replaceAll("[\uFFF9-\uFFFF]", ""); // 特殊区域字符（更精确范围）
        text = text.replaceAll("[\u2000-\u200A]", " "); // 各种空格字符统一为普通空格（排除行分隔符）
        text = text.replaceAll("[\u2028-\u2029]", "\n"); // 行分隔符和段分隔符
        text = text.replaceAll("\u00A0", " "); // 不间断空格
        text = text.replaceAll("\u00AD", ""); // 软连字符
        text = text.replaceAll("[\u200B-\u200D]", ""); // 零宽字符
        text = text.replaceAll("\uFEFF", ""); // 字节顺序标记
        
        // 移除Word特有的字段代码和超链接标记（更精确的匹配）
        text = text.replaceAll("\\\\[a-zA-Z]+\\\\\\*[^\\\\]*\\\\", ""); // Word字段代码
        text = text.replaceAll("\\bHYPERLINK\\s+[^\n]*", ""); // 超链接字段（更精确）
        text = text.replaceAll("\\bPAGEREF\\s+[^\n]*", ""); // 页面引用字段（更精确）
        text = text.replaceAll("\\bTOC\\s+[^\n]*", ""); // 目录字段（更精确）
        
        // 清理多余的空白字符，但保留换行符
        text = text.replaceAll("[ \t]+", " "); // 只合并空格和制表符，保留换行符
        text = text.replaceAll("\n\\s*\n", "\n\n"); // 清理空行
        
        return text.trim();
    }
    
    /**
     * HTML转义，防止XSS攻击，并处理特殊字符
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        
        // 基本HTML转义
        text = text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
        
        // 处理可能导致显示问题的特殊字符
        text = text.replace("\u00A0", "&nbsp;"); // 不间断空格
        text = text.replace("\u2013", "&ndash;"); // en dash
        text = text.replace("\u2014", "&mdash;"); // em dash
        text = text.replace("\u2018", "&lsquo;"); // 左单引号
        text = text.replace("\u2019", "&rsquo;"); // 右单引号
        text = text.replace("\u201C", "&ldquo;"); // 左双引号
        text = text.replace("\u201D", "&rdquo;"); // 右双引号
        text = text.replace("\u2026", "&hellip;"); // 省略号
        
        // 移除或替换其他可能导致方框的字符
        text = text.replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]", ""); // 控制字符
        text = text.replaceAll("[\uFFF0-\uFFFF]", ""); // 特殊区域字符
        
        return text;
    }


}
