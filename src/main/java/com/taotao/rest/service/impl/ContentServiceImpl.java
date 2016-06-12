package com.taotao.rest.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.utils.JsonUtils;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;
import com.taotao.rest.dao.JedisClient;
import com.taotao.rest.service.ContentService;

@Service
public class ContentServiceImpl implements ContentService {
	
	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient jedisClient;
	@Value("${INDEX_CONTENT_REDIS_KEY}")
	private String INDEX_CONTENT_REDIS_KEY;
	
	@Override
	public List<TbContent> getContentList(long contentCid) {
		
		//先从缓存中取数据，有则取出，返回
		try {
			String result = jedisClient.hget(INDEX_CONTENT_REDIS_KEY, contentCid + "");
			if(!StringUtils.isBlank(result)){
				List<TbContent> contentList = JsonUtils.jsonToList(result, TbContent.class);
				return contentList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//若缓存中没有，则根据内容分类id查询内容列表
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(contentCid);
		//执行查询
		List<TbContent> contentList = contentMapper.selectByExample(example);
		
		//再向缓存中添加内容
		try {
			//把list转换成字符串
			String cacheString = JsonUtils.objectToJson(contentList);
			jedisClient.hset(INDEX_CONTENT_REDIS_KEY, contentCid + "", cacheString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return contentList;
	}

}
