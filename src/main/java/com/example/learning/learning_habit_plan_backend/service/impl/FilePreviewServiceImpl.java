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
     * 预览文本文件
     */
    private FilePreviewResponse previewTextFile(Path filePath, String fileName, String fileExtension) throws IOException {
        // 读取文本内容，对大文件进行截断
        byte[] fileBytes = Files.readAllBytes(filePath);
        String content;
        
        if (fileBytes.length > MAX_TEXT_PREVIEW_SIZE) {
            // 截断大文件，只显示前面部分
            byte[] truncatedBytes = new byte[MAX_TEXT_PREVIEW_SIZE];
            System.arraycopy(fileBytes, 0, truncatedBytes, 0, MAX_TEXT_PREVIEW_SIZE);
            content = new String(truncatedBytes, StandardCharsets.UTF_8);
            content += "\n\n[文件过大，仅显示前" + (MAX_TEXT_PREVIEW_SIZE / 1024) + "KB内容，完整内容请下载查看]";
        } else {
            content = new String(fileBytes, StandardCharsets.UTF_8);
        }
        
        // 返回文本内容的Base64编码
        FilePreviewResponse response = FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8))
        );
        
        // 缓存结果
        cachePreviewResult(fileName, response);
        return response;
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
     * 预览Word文件 - 转换为PDF再转换为图片
     */
    private FilePreviewResponse previewWordFile(Path filePath, String fileName, String fileExtension) throws IOException {
        try {
            if ("docx".equals(fileExtension)) {
                // DOCX文件转换为PDF再转换为图片
                return convertDocxToPdfImages(filePath, fileName, fileExtension);
            } else {
                // DOC文件使用HTML方式预览（DOC转PDF的库不可用）
                return convertDocToHtml(filePath, fileName, fileExtension);
            }
        } catch (Exception e) {
            logger.warning("Word文档转换失败，使用备用方案: " + e.getMessage());
            // 如果转换失败，回退到原有的HTML方式
            return convertDocToHtml(filePath, fileName, fileExtension);
        }
    }
    
    /**
     * 将DOCX文件转换为PDF，再转换为图片
     */
    private FilePreviewResponse convertDocxToPdfImages(Path filePath, String fileName, String fileExtension) throws IOException {
        List<String> imageBase64List = new ArrayList<>();
        
        try (InputStream is = Files.newInputStream(filePath)) {
            // 读取DOCX文档
            XWPFDocument document = new XWPFDocument(is);
            
            // 创建临时PDF文件
            Path tempPdfPath = Files.createTempFile("word_preview_", ".pdf");
            
            try (OutputStream pdfOut = Files.newOutputStream(tempPdfPath)) {
                // 转换为PDF
                PdfOptions options = PdfOptions.create();
                PdfConverter.getInstance().convert(document, pdfOut, options);
            }
            
            // 将PDF转换为图片
            try (PDDocument pdfDocument = PDDocument.load(tempPdfPath.toFile())) {
                PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                int pageCount = Math.min(pdfDocument.getNumberOfPages(), MAX_PDF_PAGES);
                
                for (int page = 0; page < pageCount; page++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, PDF_DPI);
                    
                    // 转换为Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", baos);
                    String imageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                    imageBase64List.add(imageBase64);
                }
            }
            
            // 清理临时文件
            Files.deleteIfExists(tempPdfPath);
            document.close();
            
        } catch (Exception e) {
            throw new IOException("DOCX转换失败: " + e.getMessage(), e);
        }
        
        // 创建HTML容器来显示图片
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; padding: 20px; text-align: center;'>");
        
        for (int i = 0; i < imageBase64List.size(); i++) {
            htmlContent.append("<div style='margin-bottom: 20px; page-break-after: always;'>")
                      .append("<h4 style='color: #666; margin-bottom: 10px;'>第 ").append(i + 1).append(" 页</h4>")
                      .append("<img src='data:image/png;base64,").append(imageBase64List.get(i))
                      .append("' style='max-width: 100%; height: auto; border: 1px solid #ddd; box-shadow: 0 2px 8px rgba(0,0,0,0.1);' />")
                      .append("</div>");
        }
        
        htmlContent.append("</div>");
        
        return FilePreviewResponse.success(
                fileName,
                fileExtension,
                Base64.getEncoder().encodeToString(htmlContent.toString().getBytes(StandardCharsets.UTF_8))
        );
    }
    

    /**
     * DOC文件转换为HTML（保留原有逻辑作为备用方案）
     */
    private FilePreviewResponse convertDocToHtml(Path filePath, String fileName, String fileExtension) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px;'>");
        
        try (InputStream is = Files.newInputStream(filePath)) {
            if ("doc".equals(fileExtension)) {
                // 处理DOC文件
                HWPFDocument doc = new HWPFDocument(is);
                
                // 使用Range来更好地提取文本内容
                org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
                String text = range.text();
                
                // 清理和处理文本，保留原始文本作为备份
                String originalText = text;
                text = cleanWordText(text);
                
                // 如果清理后文本过短（可能过度清理），使用原始文本
                if (text.length() < originalText.length() * 0.5) {
                    text = originalText;
                    // 只进行基本的控制字符清理
                    text = text.replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]", "");
                }
                
                // 将文本转换为HTML格式，保持段落结构
                String[] paragraphs = text.split("[\r\n]+");
                if (paragraphs.length == 1) {
                    // 如果没有换行符，尝试按句号分割长文本
                    paragraphs = text.split("(?<=。)\\s*");
                }
                
                for (String paragraph : paragraphs) {
                    paragraph = paragraph.trim();
                    if (paragraph.isEmpty()) {
                        continue; // 跳过空段落
                    } else {
                        // 检测是否为标题
                        if (paragraph.length() < 50 && (paragraph.matches(".*\\d+\\..*") || paragraph.matches(".*[一二三四五六七八九十]+、.*"))) {
                            htmlContent.append("<h3 style='color: #333; margin-top: 20px; margin-bottom: 10px;'>").append(escapeHtml(paragraph)).append("</h3>");
                        } else {
                            htmlContent.append("<p style='margin-bottom: 10px; text-indent: 2em;'>").append(escapeHtml(paragraph)).append("</p>");
                        }
                    }
                }
                doc.close();
            } else {
                // 处理DOCX文件（备用方案）
                XWPFDocument docx = new XWPFDocument(is);
                docx.getParagraphs().forEach(paragraph -> {
                    String text = paragraph.getText().trim();
                    
                    if (!text.isEmpty()) {
                        // 基本的HTML转义
                        text = escapeHtml(text);
                        
                        // 检测标题样式
                        String styleName = paragraph.getStyle();
                        if (styleName != null && (styleName.toLowerCase().contains("heading") || styleName.toLowerCase().contains("title"))) {
                            htmlContent.append("<h3 style='color: #333; margin-top: 20px; margin-bottom: 10px;'>").append(text).append("</h3>");
                        } else {
                            htmlContent.append("<p style='margin-bottom: 10px; text-indent: 2em;'>").append(text).append("</p>");
                        }
                    }
                });
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
            
            try {
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
     * 预览Excel文件 - 转换为HTML表格
     */
    private FilePreviewResponse previewExcelFile(Path filePath, String fileName, String fileExtension) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; padding: 20px;'>");
        
        try (InputStream is = Files.newInputStream(filePath)) {
            Workbook workbook;
            
            if ("xls".equals(fileExtension)) {
                workbook = new HSSFWorkbook(is);
            } else {
                workbook = new XSSFWorkbook(is);
            }
            
            // 限制预览的工作表数量和行数
            int maxSheets = Math.min(workbook.getNumberOfSheets(), 3);
            int maxRows = 100; // 最多预览100行
            
            for (int sheetIndex = 0; sheetIndex < maxSheets; sheetIndex++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                if (maxSheets > 1) {
                    htmlContent.append("<h3 style='color: #333; margin-top: 20px;'>工作表: ")
                              .append(escapeHtml(sheet.getSheetName()))
                              .append("</h3>");
                }
                
                htmlContent.append("<table style='border-collapse: collapse; width: 100%; margin-bottom: 20px; font-size: 12px;'>");
                
                int lastRowNum = Math.min(sheet.getLastRowNum(), maxRows - 1);
                
                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                    htmlContent.append("<tr>");
                    
                    if (row != null) {
                        int lastCellNum = Math.min(row.getLastCellNum(), 20); // 最多20列
                        
                        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                            org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
                            String cellValue = "";
                            
                            if (cell != null) {
                                switch (cell.getCellType()) {
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        break;
                                    case NUMERIC:
                                        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                            cellValue = cell.getDateCellValue().toString();
                                        } else {
                                            cellValue = String.valueOf(cell.getNumericCellValue());
                                        }
                                        break;
                                    case BOOLEAN:
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        try {
                                            cellValue = String.valueOf(cell.getNumericCellValue());
                                        } catch (Exception e) {
                                            cellValue = cell.getCellFormula();
                                        }
                                        break;
                                    default:
                                        cellValue = "";
                                }
                            }
                            
                            String style = "border: 1px solid #ddd; padding: 4px 8px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;";
                            if (rowIndex == 0) {
                                style += " background-color: #f5f5f5; font-weight: bold;";
                            }
                            
                            htmlContent.append("<td style='").append(style).append("'>")
                                      .append(escapeHtml(cellValue))
                                      .append("</td>");
                        }
                    }
                    
                    htmlContent.append("</tr>");
                }
                
                htmlContent.append("</table>");
                
                if (sheet.getLastRowNum() > maxRows - 1) {
                    htmlContent.append("<p style='color: #666; font-style: italic;'>注：仅显示前")
                              .append(maxRows)
                              .append("行数据，完整内容请下载查看</p>");
                }
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
