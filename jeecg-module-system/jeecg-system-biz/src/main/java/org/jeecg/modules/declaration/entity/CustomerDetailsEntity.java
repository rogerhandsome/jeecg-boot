package org.jeecg.modules.declaration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("customer_details")
public class CustomerDetailsEntity extends JeecgEntity {

    private String companyName;
    private String companyCode;
    private String companyType;
    private String serviceContent;
    private String taxType;
    private String renewalTime;
    private String mobile;

}
