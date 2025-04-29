package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Honor;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.HonorRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class HonorService {

    private static final Logger log = LoggerFactory.getLogger(HonorService.class);
    private final HonorRepository honorRepository;

    public HonorService(HonorRepository honorRepository) {
        this.honorRepository = honorRepository;
    }

    /**
     * 将Honor对象转换为Map
     * 方便后续处理和数据传输
     */
    public Map<String, Object> getMapFromHonor(Honor h) {
        Map<String, Object> m = new HashMap<>();
        if (h == null) {
            return m;
        }
        m.put("honorId", h.getHonorId());
        m.put("honorName", h.getHonorName());
        m.put("obtainTime", h.getObtainTime());
        m.put("honorLevel", h.getHonorLevel());
        m.put("issuingOrganization", h.getIssuingOrganization());
        return m;
    }

    /**
     * 根据查询关键词获取荣誉信息的Map列表
     * 如果关键词为空，则获取所有荣誉信息
     */
    public List<Map<String, Object>> getHonorMapList(String nameOrOrganization) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Honor> hList;
        String keywordPattern = nameOrOrganization.isEmpty() ? "" : "%" + nameOrOrganization + "%";
        if (nameOrOrganization == null || nameOrOrganization.isEmpty()) {
            hList = honorRepository.findAll();
        } else {
            hList = honorRepository.findHonorListByNameOrOrganization(keywordPattern);
        }
        if (hList == null || hList.isEmpty()) {
            return dataList;
        }
        for (Honor honor : hList) {
            dataList.add(getMapFromHonor(honor));
        }
        return dataList;
    }

    /*
     * 获取荣誉列表
     * 从请求参数中获取查询关键词，调用getHonorMapList获取荣誉信息列表
     * 并封装成DataResponse返回给前端
     */
    public DataResponse getHonorList(DataRequest dataRequest) {
        String nameOrOrganization = dataRequest.getString("nameOrOrganization");
        List<Map<String, Object>> dataList = getHonorMapList(nameOrOrganization);
        return CommonMethod.getReturnData(dataList);
    }

    /*
     * 删除荣誉信息
     * 根据前端传入的honorId查询并删除对应的荣誉记录
     */
    @Transactional
    public DataResponse honorDelete(DataRequest dataRequest) {
        try {
            Integer honorId = dataRequest.getInteger("honorId");
            Optional<Honor> op = honorRepository.findByHonorId(honorId);
            if (op.isPresent()) {
                honorRepository.delete(op.get());
            }
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("删除荣誉信息时发生错误: {}", e.getMessage(), e);
            return CommonMethod.getReturnMessageError("删除荣誉信息失败");
        }
    }

    /*
     * 获取荣誉详细信息
     * 根据前端传入的honorId查询对应的荣誉记录
     * 将其转换为Map并封装成DataResponse返回
     */
    public DataResponse getHonorInfo(DataRequest dataRequest) {
        Integer honorId = dataRequest.getInteger("honorId");
        Optional<Honor> op = honorRepository.findByHonorId(honorId);
        if (op.isPresent()) {
            Honor h = op.get();
            Map<String, Object> honorMap = getMapFromHonor(h);
            // 添加关联的学生信息
            if (h.getStudent() != null && h.getStudent().getPerson() != null) {
                // 假设 Person 类有 getName 和 getPersonId 方法
                honorMap.put("studentName", h.getStudent().getPerson().getName());
                honorMap.put("studentId", h.getStudent().getPersonId());
                honorMap.put("major", h.getStudent().getMajor());
                honorMap.put("className", h.getStudent().getClassName());
            }
            return CommonMethod.getReturnData(honorMap);
        }
        return CommonMethod.getReturnMessageError("未找到对应荣誉信息");
    }

    /*
     * 保存或更新荣誉信息
     * 如果honorId存在，则更新已有荣誉记录；否则创建新的荣誉记录
     */
    @Transactional
    public DataResponse honorEditSave(DataRequest dataRequest) {
        Integer honorId = dataRequest.getInteger("honorId");
        Map<String, Object> form = dataRequest.getMap("form");
        Honor h = null;
        Optional<Honor> op;
        if (honorId != null) {
            op = honorRepository.findByHonorId(honorId);
            if (op.isPresent()) {
                h = op.get();
            }
        }
        if (h == null) {
            h = new Honor();
        }
        // 设置荣誉名称
        String honorName = CommonMethod.getString(form, "honorName");
        if (honorName != null) {
            h.setHonorName(honorName);
        }
        // 设置获得时间
        String obtainTime = CommonMethod.getString(form, "obtainTime");
        if (obtainTime != null) {
            h.setObtainTime(obtainTime);
        }
        // 设置荣誉等级
        String honorLevel = CommonMethod.getString(form, "honorLevel");
        if (honorLevel != null) {
            h.setHonorLevel(honorLevel);
        }
        // 设置颁发组织
        String issuingOrganization = CommonMethod.getString(form, "issuingOrganization");
        if (issuingOrganization != null) {
            h.setIssuingOrganization(issuingOrganization);
        }
        honorRepository.save(h);
        return CommonMethod.getReturnData(h.getHonorId());
    }

    /*
     * 导入荣誉数据服务
     * 解析上传的Excel文件内容，并保存荣誉信息到数据库
     */
    @Transactional
    public DataResponse importHonorData(byte[] barr, String honorIdStr) {
        try (InputStream inputStream = new ByteArrayInputStream(barr);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    Honor honor = new Honor();
                    // 假设第一列是荣誉ID（如果是自增ID，这里可以忽略或根据逻辑处理）
                    Cell honorIdCell = row.getCell(0);
                    if (honorIdCell != null) {
                        honor.setHonorId((int) honorIdCell.getNumericCellValue());
                    }
                    // 第二列是荣誉名称
                    Cell honorNameCell = row.getCell(1);
                    if (honorNameCell != null) {
                        honor.setHonorName(honorNameCell.getStringCellValue());
                    }
                    // 第三列是获得时间
                    Cell obtainTimeCell = row.getCell(2);
                    if (obtainTimeCell != null) {
                        honor.setObtainTime(obtainTimeCell.getStringCellValue());
                    }
                    // 第四列是荣誉等级
                    Cell honorLevelCell = row.getCell(3);
                    if (honorLevelCell != null) {
                        honor.setHonorLevel(honorLevelCell.getStringCellValue());
                    }
                    // 第五列是颁发组织
                    Cell issuingOrganizationCell = row.getCell(4);
                    if (issuingOrganizationCell != null) {
                        honor.setIssuingOrganization(issuingOrganizationCell.getStringCellValue());
                    }
                    honorRepository.save(honor);
                }
            }
            return CommonMethod.getReturnMessageOK();
        } catch (IOException e) {
            log.error("导入荣誉数据时发生错误: {}", e.getMessage(), e);
            return CommonMethod.getReturnMessageError("导入荣誉数据失败");
        }
    }


    /*
     * 导出荣誉信息为Excel文件并返回给前端
     */
    public ResponseEntity<StreamingResponseBody> getHonorListExcl(DataRequest dataRequest) {
        String keywords = dataRequest.getString("nameOrOrganization");
        List<Map<String, Object>> honorMapList = getHonorMapList(keywords);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Honor List");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"荣誉ID", "荣誉名称", "获得时间", "荣誉等级", "颁发组织"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 填充数据
        for (int i = 0; i < honorMapList.size(); i++) {
            Map<String, Object> honorMap = honorMapList.get(i);
            Row dataRow = sheet.createRow(i + 1);
            dataRow.createCell(0).setCellValue((Integer) honorMap.get("honorId"));
            dataRow.createCell(1).setCellValue((String) honorMap.get("honorName"));
            dataRow.createCell(2).setCellValue((String) honorMap.get("obtainTime"));
            dataRow.createCell(3).setCellValue((String) honorMap.get("honorLevel"));
            dataRow.createCell(4).setCellValue((String) honorMap.get("issuingOrganization"));
        }

        // 调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 将Excel文件写入输出流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
        } catch (IOException e) {
            log.error("导出荣誉信息为Excel文件时发生错误: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        // 设置响应头
        HttpHeaders headersResponse = new HttpHeaders();
        headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headersResponse.setContentDispositionFormData("attachment", "honor_list.xlsx");

        // 创建StreamingResponseBody并返回
        StreamingResponseBody stream = outputStream -> outputStream.write(bos.toByteArray());
        return ResponseEntity.ok()
                .headers(headersResponse)
                .body(stream);
    }

    // 分页获取荣誉列表
    public DataResponse getHonorPageList(DataRequest dataRequest, Pageable pageable) {
        String nameOrOrganization = dataRequest.getString("nameOrOrganization");
        Page<Honor> honorPage = honorRepository.findHonorPageByNameOrOrganization(nameOrOrganization, pageable);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Honor honor : honorPage.getContent()) {
            dataList.add(getMapFromHonor(honor));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("content", dataList);
        result.put("totalElements", honorPage.getTotalElements());
        result.put("totalPages", honorPage.getTotalPages());
        return CommonMethod.getReturnData(result);
    }

    public List<Map<String, Object>> getHonorListByStudentId(Integer personId) {
        List<Honor> hList = honorRepository.findByStudentPersonId(personId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (hList != null && !hList.isEmpty()) {
            for (Honor honor : hList) {
                dataList.add(getMapFromHonor(honor));
            }
        }
        return dataList;
    }
}