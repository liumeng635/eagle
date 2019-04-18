package com.eagle.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.eagle.common.ConfigFactory;
import com.eagle.entity.TsmUser;
import com.eagle.exception.EagleException;
import com.eagle.sqlClient.SqlMapClient;

public class TestMain {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws EagleException {
		SqlMapClient sqlClient = ConfigFactory.getSqlMapClient();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userName", "¡ı√Õ");
		List<TsmUser> list = (List<TsmUser>)sqlClient.selectForList("userMapper.queryUser", map);
		for(TsmUser user : list){
			System.out.println(user.getUserId());
			System.out.println(user.getUserName());
			System.out.println(user.getEmail());
			System.out.println(user.getTel());
		}
		TsmUser user = new TsmUser();
		user.setUserName("≥¬≤®");
		String count = (String)sqlClient.selectForObject("userMapper.queryUserDetail",user );
		System.out.println(count+"========");
	}
}
