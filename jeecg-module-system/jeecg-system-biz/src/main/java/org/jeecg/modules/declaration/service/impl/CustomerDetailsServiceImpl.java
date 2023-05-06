package org.jeecg.modules.declaration.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.declaration.entity.CustomerDetailsEntity;
import org.jeecg.modules.declaration.mapper.CustomerDetailsMapper;
import org.jeecg.modules.declaration.service.CustomerDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsServiceImpl extends JeecgServiceImpl<CustomerDetailsMapper, CustomerDetailsEntity> implements CustomerDetailsService {

}
