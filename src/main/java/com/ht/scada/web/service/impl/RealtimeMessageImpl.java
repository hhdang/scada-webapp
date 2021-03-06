package com.ht.scada.web.service.impl;

import com.ht.comet.BroadcasterCache;
import com.ht.scada.common.tag.entity.EndTag;
import com.ht.scada.common.tag.entity.TagCfgTpl;
import com.ht.scada.common.tag.service.EndTagService;
import com.ht.scada.common.tag.service.TagService;
import com.ht.scada.common.tag.util.AlarmTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ht.scada.data.RealtimeMessageListener;
import com.ht.scada.data.entity.FaultRecord;
import com.ht.scada.data.entity.OffLimitsRecord;
import com.ht.scada.data.entity.YxRecord;
import com.ht.scada.oildata.OilDataMessageListener;
import com.ht.scada.oildata.entity.FaultDiagnoseRecord;
import com.ht.scada.security.entity.User;
import com.ht.scada.web.alarm.AlarmCache;
import com.ht.scada.web.entity.AlarmRecord;
import com.ht.scada.web.entity.UserExtInfo;
import com.ht.scada.web.service.AlarmInfoService;
import com.ht.scada.web.service.UserExtInfoService;
import java.util.List;
import javax.inject.Inject;
import org.atmosphere.cpr.MetaBroadcaster;

public class RealtimeMessageImpl implements RealtimeMessageListener, OilDataMessageListener {

    private static final Logger log = LoggerFactory.getLogger(RealtimeMessageImpl.class);
    private static final int ALARM_STATUS_OCCURED = 0;
    private static final int ALARM_STATUS_RESUMED = 1;
    @Inject
    private EndTagService endTagService;
    @Inject
    private AlarmInfoService alarmInfoService;
    @Inject
    private UserExtInfoService userExtInfoService;
    @Inject
    private TagService tagService;

    @Override
    public synchronized void faultOccured(FaultRecord record) {
        log.info("接收报警信息——faultOccured");
        if (AlarmCache.getInstance().getAlarm(record.getId()) != null) {//报警未解除
            return;
        }
//        AlarmRecord oldalarm = alarmInfoService.getAlarmByAlarmId(record.getId());
//        if(oldalarm != null){
//            return;
//        }

        EndTag endTag = endTagService.getByCode(record.getCode());
        AlarmRecord alarm = new AlarmRecord();
        alarm.setAlarmType(AlarmTypeEnum.ALARM_FAULT.toString());
        alarm.setAlarmId(record.getId());
        alarm.setEndTag(endTag);
        alarm.setActionTime(record.getActionTime());
        alarm.setInfo(record.getInfo());
        alarm.setVarName(record.getName());
        alarm.setRemark(record.getInfo());

        AlarmCache.getInstance().putAlarm(record.getId(), alarm);//加入报警缓存
        pushAlarm(alarm);
    }

    @Override
    public synchronized void faultResumed(FaultRecord record) {
        // TODO Auto-generated method stub
        log.info("报警信息解除--faultResumed");
        AlarmRecord alarm = AlarmCache.getInstance().getAlarm(record.getId());//从缓存取报警
        if(alarm == null) {
            return;
        }
        alarm.setResumeTime(record.getResumeTime());
        alarm.setStatus(ALARM_STATUS_RESUMED);
        
        AlarmCache.getInstance().removeAlarm(record.getId());//解除报警
        pushAlarm(alarm);
    }

    @Override
    public synchronized void offLimitsOccured(OffLimitsRecord record) {
        // TODO Auto-generated method stub
        log.info("接收报警信息——offLimitsOccured");
        AlarmRecord oldalarm = alarmInfoService.getAlarmByAlarmId(record.getId());
        if (oldalarm != null) {
            return;
        }
        EndTag endTag = endTagService.getByCode(record.getCode());
        AlarmRecord alarm = new AlarmRecord();
        alarm.setAlarmType(AlarmTypeEnum.ALARM_OFFLIMITS.toString());
        alarm.setAlarmId(record.getId());
        alarm.setEndTag(endTag);
        alarm.setActionTime(record.getActionTime());
        alarm.setInfo(record.getInfo());
        alarm.setVarName(record.getName());
        alarm.setRemark(record.getInfo());
        //alarmInfoService.saveAlarmRecord(alarm);
        pushAlarm(alarm);
    }

    @Override
    public synchronized void offLimitsResumed(OffLimitsRecord record) {
        // TODO Auto-generated method stub
        log.info("报警信息解除——offLimitsResumed");
        AlarmRecord alarm = alarmInfoService.getAlarmByAlarmId(record.getId());
        alarm.setResumeTime(record.getResumeTime());
        alarm.setStatus(ALARM_STATUS_RESUMED);
        //alarmInfoService.saveAlarmRecord(alarm);
        pushAlarm(alarm);
    }

    @Override
    public synchronized void yxChanged(YxRecord record) {
        // TODO Auto-generated method stub
        log.info("接收报警信息——yxChanged");
        AlarmRecord oldalarm = alarmInfoService.getAlarmByAlarmId(record.getId());
        if (oldalarm != null) {
            return;
        }
        //ALARM_YXCHANGED
        EndTag endTag = endTagService.getByCode(record.getCode());
        AlarmRecord alarm = new AlarmRecord();
        alarm.setAlarmType(AlarmTypeEnum.ALARM_YXCHANGED.toString());
        alarm.setAlarmId(record.getId());
        alarm.setEndTag(endTag);
        alarm.setActionTime(record.getDatetime());
        alarm.setResumeTime(record.getDatetime());
        alarm.setStatus(ALARM_STATUS_RESUMED);
        alarm.setInfo(record.getInfo());
        alarm.setVarName(record.getName());
        alarm.setRemark(record.getInfo());
        //alarmInfoService.saveAlarmRecord(alarm);
        pushAlarm(alarm);
    }

    @Override
    public synchronized void faultOccured(FaultDiagnoseRecord record) {
        EndTag endTag = endTagService.getByCode(record.getCode());
        AlarmRecord alarm = new AlarmRecord();
        alarm.setAlarmType(AlarmTypeEnum.ALARM_FAULTDIAGNOSE.toString());
        alarm.setAlarmId(record.getId());
        alarm.setEndTag(endTag);
        alarm.setActionTime(record.getActionTime());
        alarm.setInfo(record.getInfo());
        alarm.setVarName(record.getName());
        alarm.setRemark(record.getInfo());
        //alarmInfoService.saveAlarmRecord(alarm);
        pushAlarm(alarm);
    }

    @Override
    public synchronized void faultResumed(FaultDiagnoseRecord record) {
        AlarmRecord alarm = alarmInfoService.getAlarmByAlarmId(record.getId());
        alarm.setResumeTime(record.getResumeTime());
        alarm.setStatus(ALARM_STATUS_RESUMED);
        //alarmInfoService.saveAlarmRecord(alarm);
        pushAlarm(alarm);
    }

    private synchronized void pushAlarm(AlarmRecord alarm) {
        log.debug(AlarmTypeEnum.ALARM_FAULT.toString() + Thread.currentThread().getId());
        List<UserExtInfo> list = userExtInfoService.getUserExtInfoByEndTag(alarm.getEndTag().getId());
        for (UserExtInfo extInfo : list) {
            if (extInfo.getHeadflg().equals("1")) {
                User user = extInfo.getUser();
                //MetaBroadcaster.getDefault().broadcastTo("/" + user.getUsername(), alarm.getId().toString());
                BroadcasterCache.getInstance().broadcast("/" + user.getUsername(), alarm.getAlarmId());
            }
        }
        TagCfgTpl cfgtpl = tagService.getTagCfgTplByCodeAndVarName(alarm.getEndTag().getCode(), alarm.getVarName());
        alarm.setVarCnName(cfgtpl.getTagName());
        alarmInfoService.saveAlarmRecord(alarm);    //将报警记录写入数据库
    }
}
