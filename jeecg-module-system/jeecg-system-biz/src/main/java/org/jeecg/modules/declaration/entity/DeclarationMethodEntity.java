package org.jeecg.modules.declaration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("declaration_method")
public class DeclarationMethodEntity extends JeecgEntity {

    private String companyType;
    private String taxType;
    private String declarationMethod;

}
