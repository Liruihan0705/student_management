package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Honor荣誉表实体类，保存每个学生荣誉的信息
 * Integer honorId 荣誉表 honor 主键 honor_id
 * Student student 关联到该学生所用的Student对象，关联 student 表主键 personId
 * String honorName 荣誉名称
 * String obtainTime 获得时间
 * String honorLevel 荣誉等级
 * String issuingOrganization 颁发组织
 */
@Getter
@Setter
@Entity
@Table(name = "honor",
        uniqueConstraints = {})
public class Honor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer honorId;

    @ManyToOne
    @JoinColumn(name="personId")
    private Student student; // 修改为关联 Student 类

    @Size(max = 100)
    @Column(name = "honor_name")
    private String honorName;

    @Size(max = 20)
    @Column(name = "obtain_time")
    private String obtainTime;

    @Size(max = 20)
    @Column(name = "honor_level")
    private String honorLevel;

    @Size(max = 100)
    @Column(name = "issuing_organization")
    private String issuingOrganization;
}