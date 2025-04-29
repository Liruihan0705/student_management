package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.HonorService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

/**
 * HonorController 主要是为学生荣誉信息管理提供的Web请求服务
 */
// origins： 允许可访问的域列表
// maxAge:准备响应前的缓存持续的最大时间（以秒为单位）。
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/honor")
public class HonorController {

    private final HonorService honorService;

    public HonorController(HonorService honorService) {
        this.honorService = honorService;
    }

    /**
     * getHonorList 荣誉管理 点击查询按钮请求
     * 前台请求参数 keywords 荣誉名称或颁发机构等的查询串
     * 返回前端 存储荣誉信息的 MapList 框架会自动将Map转换为用于前后台传输数据的Json对象，Map的嵌套结构和Json的嵌套结构类似
     */
    @PostMapping("/getHonorList")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getHonorList(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.getHonorList(dataRequest);
    }

    /**
     * honorDelete 删除荣誉信息Web服务
     * Honor页面的列表里点击删除按钮则可以删除已经存在的荣誉信息，
     * 前端会将该记录的id 回传到后端，方法从参数获取id，查出相关记录，调用delete方法删除
     *
     * @param dataRequest 前端honorId 要删除的荣誉的主键 honor_id
     * @return 正常操作
     */
    @PostMapping("/honorDelete")
    public DataResponse honorDelete(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.honorDelete(dataRequest);
    }

    /**
     * getHonorInfo 前端点击荣誉列表时前端获取荣誉详细信息请求服务
     *
     * @param dataRequest 从前端获取 honorId 查询荣誉信息的主键 honor_id
     * @return 根据honorId从数据库中查出数据，存在Map对象里，并返回前端
     */
    @PostMapping("/getHonorInfo")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getHonorInfo(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.getHonorInfo(dataRequest);
    }

    /**
     * honorEditSave 前端荣誉信息提交服务
     * 前端把所有数据打包成一个Json对象作为参数传回后端，
     * 后端直接可以获得对应的Map对象form, 再从form里取出所有属性，复制到
     * 实体对象里，保存到数据库里即可，
     * 如果是添加一条记录， honorId 为空，这时先 new Honor 计算新的id， 复制相关属性，保存，
     * 如果是编辑原来的信息，honorId不为空。则查询出实体对象，复制相关属性，保存后修改数据库信息，永久修改
     *
     * @return 新建修改荣誉的主键 honor_id 返回前端
     */
    @PostMapping("/honorEditSave")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse honorEditSave(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.honorEditSave(dataRequest);
    }

    /**
     * 导入荣誉数据服务
     *
     * @param barr         文件二进制数据
     * @param uploader     上传者
     * @param honorIdStr   honor 主键
     * @param fileName     前端上传的文件名
     */
    @PostMapping(path = "/importHonorData")
    public DataResponse importHonorData(
            @RequestBody byte[] barr,
            @RequestParam(name = "uploader") String uploader,
            @RequestParam(name = "honorId") String honorIdStr,
            @RequestParam(name = "fileName") String fileName) {
        return honorService.importHonorData(barr, honorIdStr);
    }

    /**
     * 前端下载导出荣誉信息Excl表数据服务
     */
    @PostMapping("/getHonorListExcl")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StreamingResponseBody> getHonorListExcl(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.getHonorListExcl(dataRequest);
    }

    /**
     * 分页获取荣誉列表
     */
    @PostMapping("/getHonorPageList")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getHonorPageList(@Valid @RequestBody DataRequest dataRequest, Pageable pageable) {
        return honorService.getHonorPageList(dataRequest, pageable);
    }
    @PostMapping("/getHonorListByStudentId")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getHonorListByStudentId(@Valid @RequestBody DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        List<Map<String, Object>> dataList = honorService.getHonorListByStudentId(personId);
        return CommonMethod.getReturnData(dataList);
    }
}