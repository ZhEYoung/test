package com.exam.controller.admin;

import com.exam.common.Result;
import com.exam.entity.Log;
import com.exam.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/logs")
@Api(tags = "系统日志管理接口")
public class AdminSystemLogController {

    @Autowired
    private LogService logService;

    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(dateStr);
    }

    @ApiOperation("获取操作日志列表")
    @GetMapping("/operation")
    public Result getOperationLogs(@RequestParam(required = false) String username,
                                  @RequestParam(required = false) Integer operation,
                                  @RequestParam(required = false) String startTime,
                                  @RequestParam(required = false) String endTime,
                                   @RequestParam(required = false) Integer actionType,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("username", username);
//            condition.put("operationType", operation);
            condition.put("startTime", parseDate(startTime));
            condition.put("endTime", parseDate(endTime));
            condition.put("actionType", operation);

            Map<String, Object> data = new HashMap<>();
            data.put("total", logService.getCountByCondition(condition));
            data.put("records", logService.getPageByCondition(condition, pageNum, pageSize));

            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取操作日志列表失败", e);
            return Result.error("获取操作日志列表失败");
        }
    }

    @ApiOperation("获取登录日志列表")
    @GetMapping("/login")
    public Result getLoginLogs(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String ip,
                              @RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("username", username);
            condition.put("ipAddress", ip);
            condition.put("actionType", 3);
            condition.put("startTime", parseDate(startTime));
            condition.put("endTime", parseDate(endTime));

            Map<String, Object> data = new HashMap<>();
            data.put("total", logService.getCountByCondition(condition));
            data.put("records", logService.getPageByCondition(condition, pageNum, pageSize));


            return Result.success(data);
        } catch (Exception e) {
            log.error("获取登录日志列表失败", e);
            return Result.error("获取登录日志列表失败");
        }
    }

    @ApiOperation("获取系统异常日志列表")
    @GetMapping("/error")
    public Result getErrorLogs(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String errorInfo,
                              @RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("username", username);
            condition.put("actionType", 5);
            condition.put("actionDescription", errorInfo);
            condition.put("startTime", parseDate(startTime));
            condition.put("endTime", parseDate(endTime));


            Map<String, Object> data = new HashMap<>();
            data.put("total", logService.getCountByCondition(condition));
            data.put("records", logService.getPageByCondition(condition, pageNum, pageSize));

            return Result.success(data);

        } catch (Exception e) {
            log.error("获取系统异常日志列表失败", e);
            return Result.error("获取系统异常日志列表失败");
        }
    }

    @ApiOperation("清空操作日志")
    @DeleteMapping("/operation")
    public Result clearOperationLogs() {
        try {
            logService.cleanExpiredLogs(0);
            return Result.success("清空操作日志成功");
        } catch (Exception e) {
            log.error("清空操作日志失败", e);
            return Result.error("清空操作日志失败");
        }
    }

    @ApiOperation("清空登录日志")
    @DeleteMapping("/login")
    public Result clearLoginLogs() {
        try {
            logService.cleanExpiredLogs(0);
            return Result.success("清空登录日志成功");
        } catch (Exception e) {
            log.error("清空登录日志失败", e);
            return Result.error("清空登录日志失败");
        }
    }

    @ApiOperation("清空异常日志")
    @DeleteMapping("/error")
    public Result clearErrorLogs() {
        try {
            logService.cleanExpiredLogs(0);
            return Result.success("清空异常日志成功");
        } catch (Exception e) {
            log.error("清空异常日志失败", e);
            return Result.error("清空异常日志失败");
        }
    }


    @ApiOperation("根据用户ID查询操作日志")
    @GetMapping("/user/{userId}")
    public Result getLogsByUserId(@PathVariable Integer userId) {
        try {
            return Result.success(logService.getByUserId(userId));
        } catch (Exception e) {
            log.error("根据用户ID查询操作日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @ApiOperation("根据IP地址查询操作日志")
    @GetMapping("/ip/{ipAddress}")
    public Result getLogsByIp(@PathVariable String ipAddress) {
        try {
            return Result.success(logService.getByIpAddress(ipAddress));
        } catch (Exception e) {
            log.error("根据IP地址查询操作日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @ApiOperation("根据操作类型查询日志")
    @GetMapping("/type/{actionType}")
    public Result getLogsByOperationType(@PathVariable Integer actionType) {
        try {
            return Result.success(logService.getByActionType(actionType));
        } catch (Exception e) {
            log.error("根据操作类型查询日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @ApiOperation("统计用户操作次数")
    @GetMapping("/count/user/{userId}")
    public Result countUserOperations(@PathVariable Integer userId) {
        try {
            return Result.success(logService.countUserOperations(userId));
        } catch (Exception e) {
            log.error("统计用户操作次数失败", e);
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取操作类型统计分布")
    @GetMapping("/stats/operation-types")
    public Result getOperationTypeStats() {
        try {
            return Result.success(logService.countByOperationType());
        } catch (Exception e) {
            log.error("获取操作类型统计分布失败", e);
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取高频操作用户")
    @GetMapping("/stats/frequent-users")
    public Result getFrequentUsers(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            return Result.success(logService.getFrequentUsers(limit));
        } catch (Exception e) {
            log.error("获取高频操作用户失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取IP访问统计")
    @GetMapping("/stats/ip-access")
    public Result getIpAccessStats() {
        try {
            return Result.success(logService.countByIpAddress());
        } catch (Exception e) {
            log.error("获取IP访问统计失败", e);
            return Result.error("统计失败：" + e.getMessage());
        }
    }

    private void setExcelResponseHeaders(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        // 设置下载头
        response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");
        // 禁用缓存
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        // 允许跨域
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

    }

    private void writeExcelHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @ApiOperation("导出全部日志")
    @GetMapping("/all/export")
    public void exportAllLogs(HttpServletResponse response,
                              @RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime) {
        try {
            // 获取日志数据
            Map<String, Object> data = logService.exportOperationLogs(parseDate(startTime), parseDate(endTime));
            
            // 设置响应头
            setExcelResponseHeaders(response, "系统操作日志");
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("系统日志记录");
                
                // 设置表头
                String[] headers = {
                    "日志ID", 
                    "用户ID", 
                    "用户名称",
                    "操作类型", 
                    "操作描述",
                    "操作时间", 
                    "操作对象",
                    "IP地址",
                    "设备信息",
                    "操作状态"
                };
                writeExcelHeader(sheet, headers);
                
                // 写入数据
                @SuppressWarnings("unchecked")
                List<Log> logs = (List<Log>) data.get("logs");
                if (logs == null || logs.isEmpty()) {
                    log.warn("没有找到符合条件的日志数据");
                    sheet.createRow(1).createCell(0).setCellValue("没有找到符合条件的日志数据");
                } else {
                    int rowNum = 1;
                    CellStyle dateCellStyle = workbook.createCellStyle();
                    dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
                    
                    for (Log logEntry : logs) {
                        Row row = sheet.createRow(rowNum++);
                        
                        // 填充单元格数据
                        row.createCell(0).setCellValue(logEntry.getLogId() != null ? logEntry.getLogId().toString() : "");
                        row.createCell(1).setCellValue(logEntry.getUserId() != null ? logEntry.getUserId().toString() : "");
                        
                        // 获取关联的用户信息
                        String username = "";
                        if (logEntry.getUser() != null) {
                            username = logEntry.getUser().getUsername();
                        }
                        row.createCell(2).setCellValue(username);
                        
                        // 处理操作类型
                        String actionTypeStr = convertActionType(logEntry.getActionType());
                        row.createCell(3).setCellValue(actionTypeStr);
                        
                        row.createCell(4).setCellValue(logEntry.getActionDescription() != null ? 
                            logEntry.getActionDescription() : "");
                        
                        // 处理时间格式
                        Cell timeCell = row.createCell(5);
                        if (logEntry.getCreatedTime() != null) {
                            timeCell.setCellValue(logEntry.getCreatedTime());
                            timeCell.setCellStyle(dateCellStyle);
                        }
                        
                        row.createCell(6).setCellValue(logEntry.getObjectType() != null ? 
                            logEntry.getObjectType() : "");
                        row.createCell(7).setCellValue(logEntry.getIpAddress() != null ? 
                            logEntry.getIpAddress() : "");
                        row.createCell(8).setCellValue(logEntry.getDeviceInfo() != null ? 
                            logEntry.getDeviceInfo() : "");
                        row.createCell(9).setCellValue(logEntry.getStatus() != null ? 
                            logEntry.getStatus() : "");
                    }
                    
                    // 自动调整列宽
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }
                }
                
                workbook.write(response.getOutputStream());
                log.info("成功导出{}条日志记录", logs != null ? logs.size() : 0);
            }
        } catch (ParseException e) {
            log.error("解析日期参数失败", e);
            handleExportError(response, "日期格式错误，请使用正确的日期格式（yyyy-MM-dd HH:mm:ss）");
        } catch (Exception e) {
            log.error("导出日志失败", e);
            handleExportError(response, "导出失败：" + e.getMessage());
        }
    }

    /**
     * 转换操作类型为可读文本
     */
    private String convertActionType(Integer actionType) {
        if (actionType == null) return "未知";
        switch (actionType) {
            case 0: return "新增";
            case 1: return "更新";
            case 2: return "删除";
            case 3: return "登录";
            case 4: return "提交考试";
            case 5: return "系统异常";
            case 6: return "模拟登陆";
            case 7: return "信息查询";
            default: return "未知操作";
        }
    }

    private void handleExportError(HttpServletResponse response, String errorMessage) {
        try {
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":500,\"message\":\"" + errorMessage + "\"}");
        } catch (IOException ex) {
            log.error("写入错误响应失败", ex);
        }
    }

    @ApiOperation("导出异常日志")
    @GetMapping("/error/export")
    public void exportErrorLogs(HttpServletResponse response,
                              @RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime) {
        try {
            // 获取异常日志数据
            Map<String, Object> data = logService.exportExceptionLogs(parseDate(startTime), parseDate(endTime));
            
            // 设置响应头
            setExcelResponseHeaders(response, "系统异常日志");
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("异常日志记录");
                
                // 设置表头
                String[] headers = {
                    "日志ID", 
                    "用户ID", 
                    "用户名称",
                    "异常类型", 
                    "异常描述",
                    "发生时间", 
                    "操作对象",
                    "IP地址",
                    "设备信息",
                    "异常状态"
                };
                writeExcelHeader(sheet, headers);
                
                // 写入数据
                @SuppressWarnings("unchecked")
                List<Log> logs = (List<Log>) data.get("logs");
                if (logs == null || logs.isEmpty()) {
                    log.warn("没有找到符合条件的异常日志数据");
                    sheet.createRow(1).createCell(0).setCellValue("没有找到符合条件的异常日志数据");
                } else {
                    int rowNum = 1;
                    CellStyle dateCellStyle = workbook.createCellStyle();
                    dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
                    
                    for (Log logEntry : logs) {
                        Row row = sheet.createRow(rowNum++);
                        
                        // 填充单元格数据
                        row.createCell(0).setCellValue(logEntry.getLogId() != null ? logEntry.getLogId().toString() : "");
                        row.createCell(1).setCellValue(logEntry.getUserId() != null ? logEntry.getUserId().toString() : "");
                        
                        // 获取关联的用户信息
                        String username = "";
                        if (logEntry.getUser() != null) {
                            username = logEntry.getUser().getUsername();
                        }
                        row.createCell(2).setCellValue(username);
                        
                        // 处理异常类型
                        row.createCell(3).setCellValue("系统异常");
                        
                        row.createCell(4).setCellValue(logEntry.getActionDescription() != null ? 
                            logEntry.getActionDescription() : "");
                        
                        // 处理时间格式
                        Cell timeCell = row.createCell(5);
                        if (logEntry.getCreatedTime() != null) {
                            timeCell.setCellValue(logEntry.getCreatedTime());
                            timeCell.setCellStyle(dateCellStyle);
                        }
                        
                        row.createCell(6).setCellValue(logEntry.getObjectType() != null ? 
                            logEntry.getObjectType() : "");
                        row.createCell(7).setCellValue(logEntry.getIpAddress() != null ? 
                            logEntry.getIpAddress() : "");
                        row.createCell(8).setCellValue(logEntry.getDeviceInfo() != null ? 
                            logEntry.getDeviceInfo() : "");
                        row.createCell(9).setCellValue(logEntry.getStatus() != null ? 
                            logEntry.getStatus() : "");
                    }
                    
                    // 自动调整列宽
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }
                }
                
                workbook.write(response.getOutputStream());
                log.info("成功导出{}条异常日志记录", logs != null ? logs.size() : 0);
            }
        } catch (ParseException e) {
            log.error("解析日期参数失败", e);
            handleExportError(response, "日期格式错误，请使用正确的日期格式（yyyy-MM-dd HH:mm:ss）");
        } catch (Exception e) {
            log.error("导出异常日志失败", e);
            handleExportError(response, "导出失败：" + e.getMessage());
        }
    }
} 