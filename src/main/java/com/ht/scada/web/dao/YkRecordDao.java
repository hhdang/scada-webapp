/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ht.scada.web.dao;

import com.ht.scada.data.entity.YkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 遥控
 * @author Administrator
 */
public interface YkRecordDao extends JpaRepository<YkRecord, Integer>{
    
}
