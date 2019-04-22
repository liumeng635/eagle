package com.eagle.test;

import com.eagle.common.ConfigFactory;
import com.eagle.entity.BaseUniversity;
import com.eagle.exception.EagleException;
import com.eagle.sqlclient.SqlMapClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMain {
	public static void main(String[] args) throws EagleException {
		SqlMapClient sqlClient = ConfigFactory.getSqlMapClient();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("areaName", "É½¶«Ê¡");
		List<BaseUniversity> list = (List<BaseUniversity>)sqlClient.selectForList("universityMapper.queryUniversity", map);
		for(BaseUniversity u : list){
			System.out.println(u.getSchoolName());
		}
	}
}
