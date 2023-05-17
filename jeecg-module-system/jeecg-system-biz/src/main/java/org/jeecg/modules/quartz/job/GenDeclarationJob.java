package org.jeecg.modules.quartz.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.declaration.entity.CustomerDetailsEntity;
import org.jeecg.modules.declaration.entity.DeclarationDetailsEntity;
import org.jeecg.modules.declaration.entity.DeclarationMethodEntity;
import org.jeecg.modules.declaration.service.CustomerDetailsService;
import org.jeecg.modules.declaration.service.DeclarationDetailsService;
import org.jeecg.modules.declaration.service.DeclarationMethodService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成申报明细定时任务
 *
 * @Author luojie
 */
@Slf4j
public class GenDeclarationJob implements Job {

    @Resource
    private DeclarationMethodService declarationMethodService;

    @Resource
    private CustomerDetailsService customerDetailsService;

    @Resource
    private DeclarationDetailsService declarationDetailsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("开始生成申报明细定时任务");
        log.info(" Job Execution key：" + jobExecutionContext.getJobDetail().getKey());
        Date date = new Date();

        String temp = String.valueOf(DateUtil.month(date) == 0 ? 12 : DateUtil.month(date));
        if (temp.length() == 1) {
            temp = "0" + temp;
        }
        String month = DateUtil.format(date, "yyyyMMdd").substring(0, 4) + temp;
        String year = String.valueOf(Integer.valueOf(DateUtil.format(date, "yyyyMMdd").substring(0, 4)) - 1);
        int monthInteger = DateUtil.month(date) + 1;
        //按月申报
        QueryWrapper<DeclarationMethodEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("declaration_method", "1");
        List<DeclarationMethodEntity> list = declarationMethodService.list(queryWrapper);
        List<DeclarationDetailsEntity> detailsEntities = new ArrayList<>();
        list.forEach(declarationMethodEntity -> {
            //查询该方式下所有公司
            QueryWrapper<CustomerDetailsEntity> queryWrapperC = new QueryWrapper<>();
            queryWrapperC.eq("company_type", declarationMethodEntity.getCompanyType())
                    .like("tax_type", declarationMethodEntity.getTaxType())
                    .eq("cus_state", "1");
            List<CustomerDetailsEntity> customerDetailsEntities = customerDetailsService.list(queryWrapperC);
            if (CollectionUtil.isNotEmpty(customerDetailsEntities)) {
                QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
                queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
                        .eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
                        .in("customer_id", customerDetailsEntities.stream().map
                                (CustomerDetailsEntity::getId).collect(Collectors.toList())
                        ).eq("declaration_month", month);
                List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
                        .map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
                List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(
                                customerDetailsEntity -> !ids.contains(customerDetailsEntity.getId())).
                        map(customerDetailsEntity -> {
                            DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
                            entity.setCustomerId(customerDetailsEntity.getId());
                            entity.setCompanyName(customerDetailsEntity.getCompanyName());
                            entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
                            entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
                            entity.setDeclarationMonth(month);
                            entity.setCreateTime(date);
                            entity.setCreateBy("system");
                            entity.setTaxType(declarationMethodEntity.getTaxType());
                            return entity;
                        }).collect(Collectors.toList());
                detailsEntities.addAll(entityList);
            }
        });


        //按年
        if (monthInteger <= 6) {
            QueryWrapper<DeclarationMethodEntity> queryWrapperY = new QueryWrapper<>();
            queryWrapperY.eq("declaration_method", "3");
            List<DeclarationMethodEntity> listY = declarationMethodService.list(queryWrapperY);
            listY.forEach(declarationMethodEntity -> {
                //查询该方式下所有公司
                QueryWrapper<CustomerDetailsEntity> queryWrapperC = new QueryWrapper<>();
                queryWrapperC.eq("company_type", declarationMethodEntity.getCompanyType())
                        .like("tax_type", declarationMethodEntity.getTaxType())
                        .eq("cus_state", "1");
                List<CustomerDetailsEntity> customerDetailsEntities = customerDetailsService.list(queryWrapperC);
                if (CollectionUtil.isNotEmpty(customerDetailsEntities)) {
                    if (monthInteger == 6) {
                        //如果是6月。排除企业所得税
                        customerDetailsEntities = customerDetailsEntities.stream().
                                filter(cus -> !cus.getTaxType().equals("3")).collect(Collectors.toList());
                    }
                    QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
                    queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
                            .eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
                            .in("customer_id", customerDetailsEntities.stream().map
                                    (CustomerDetailsEntity::getId).collect(Collectors.toList())
                            ).eq("declaration_month", year);
                    List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
                            .map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
                    List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(
                                    customerDetailsEntity -> !ids.contains(customerDetailsEntity.getId())).
                            map(customerDetailsEntity -> {
                                DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
                                entity.setCustomerId(customerDetailsEntity.getId());
                                entity.setCompanyName(customerDetailsEntity.getCompanyName());
                                entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
                                entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
                                entity.setDeclarationMonth(year);
                                entity.setCreateTime(date);
                                entity.setCreateBy("system");
                                entity.setTaxType(declarationMethodEntity.getTaxType());
                                return entity;
                            }).collect(Collectors.toList());
                    detailsEntities.addAll(entityList);
                }
            });
        }

        //季度
        if (monthInteger == 1 || monthInteger == 4 || monthInteger == 7 || monthInteger == 10) {
            String quarter = monthInteger == 1 ? year + "10-12" : (monthInteger == 4 ? year + "01-03" : (monthInteger == 7 ? year + "04-06" : year + "07-09"));
            QueryWrapper<DeclarationMethodEntity> queryWrapperY = new QueryWrapper<>();
            queryWrapperY.eq("declaration_method", "2");
            List<DeclarationMethodEntity> listQ = declarationMethodService.list(queryWrapperY);
            listQ.forEach(declarationMethodEntity -> {
                //查询该方式下所有公司
                QueryWrapper<CustomerDetailsEntity> queryWrapperC = new QueryWrapper<>();
                queryWrapperC.eq("company_type", declarationMethodEntity.getCompanyType())
                        .like("tax_type", declarationMethodEntity.getTaxType())
                        .eq("cus_state", "1");
                List<CustomerDetailsEntity> customerDetailsEntities = customerDetailsService.list(queryWrapperC);
                if (CollectionUtil.isNotEmpty(customerDetailsEntities)) {
                    QueryWrapper<DeclarationDetailsEntity> queryWrapperD = new QueryWrapper<>();
                    queryWrapperD.eq("tax_type", declarationMethodEntity.getTaxType())
                            .eq("declaration_method", declarationMethodEntity.getDeclarationMethod())
                            .in("customer_id", customerDetailsEntities.stream().map
                                    (CustomerDetailsEntity::getId).collect(Collectors.toList())
                            ).eq("declaration_month", quarter);
                    List<String> ids = declarationDetailsService.list(queryWrapperD).stream()
                            .map(DeclarationDetailsEntity::getCustomerId).collect(Collectors.toList());
                    List<DeclarationDetailsEntity> entityList = customerDetailsEntities.stream().filter(
                                    customerDetailsEntity -> !ids.contains(customerDetailsEntity.getId())).
                            map(customerDetailsEntity -> {
                                DeclarationDetailsEntity entity = new DeclarationDetailsEntity();
                                entity.setCustomerId(customerDetailsEntity.getId());
                                entity.setCompanyName(customerDetailsEntity.getCompanyName());
                                entity.setCompanyCode(customerDetailsEntity.getCompanyCode());
                                entity.setDeclarationMethod(declarationMethodEntity.getDeclarationMethod());
                                entity.setDeclarationMonth(quarter);
                                entity.setCreateTime(date);
                                entity.setCreateBy("system");
                                entity.setTaxType(declarationMethodEntity.getTaxType());
                                return entity;
                            }).collect(Collectors.toList());
                    detailsEntities.addAll(entityList);
                }
            });
        }


        try {
            declarationDetailsService.saveBatch(detailsEntities);
            log.info("生成结果:成功,条数:{}", detailsEntities.size());
        } catch (Exception e) {
            log.info("生成结果:失败:{}", e.getMessage());
        }
        log.info("生成申报明细定时任务结束");
    }
}
