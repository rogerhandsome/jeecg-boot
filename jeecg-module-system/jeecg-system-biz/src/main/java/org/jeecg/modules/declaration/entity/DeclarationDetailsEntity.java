package org.jeecg.modules.declaration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("declaration_details")
public class DeclarationDetailsEntity extends JeecgEntity {

    private String customerId;
    private String companyName;
    private String companyCode;
    private String state;
    private String declarationDate;
    private String declarationMethod;
    private String declarationMonth;
    private String taxType;

}
