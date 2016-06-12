package com.taotao.rest.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.utils.JsonUtils;
import com.taotao.mapper.TbItemCatMapper;
import com.taotao.pojo.TbItemCat;
import com.taotao.pojo.TbItemCatExample;
import com.taotao.pojo.TbItemCatExample.Criteria;
import com.taotao.rest.dao.JedisClient;
import com.taotao.rest.pojo.CatNode;
import com.taotao.rest.pojo.CatResult;
import com.taotao.rest.service.ItemCatService;

/**
 * 商品分类服务
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private JedisClient jedisClient;
	@Value("${INDEX_ITEMCAT_REDIS_KEY}")
	private String INDEX_ITEMCAT_REDIS_KEY;
	
	@Override
	public CatResult getItemCatList() {
		//先从redis缓存中查找是否存在
//		try {
//			String result = jedisClient.hget(INDEX_ITEMCAT_REDIS_KEY, "");
//			if(!StringUtils.isBlank(result)){
//				CatResult jsonToList = JsonUtils.jsonToList(result, );
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		//若缓存中没有，则从数据库查询
		CatResult catResult = new CatResult();
		//查询分类列表
		catResult.setData(getCatList(0));
		
		//再存入redis缓存中
//		try {
//			String cacheString = JsonUtils.objectToJson(catResult);
//			jedisClient.hset(INDEX_ITEMCAT_REDIS_KEY, "", cacheString);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		return catResult;
	}
	
	/**
	 * 查询分类列表
	 * @param parentId
	 * @return
	 */
	private List<?> getCatList(long parentId) {
		//创建查询条件
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		//执行查询
		List<TbItemCat> list = itemCatMapper.selectByExample(example);
		//返回值list
		List resultList = new ArrayList<>();
		//向list中添加节点
		int count = 0;
		for (TbItemCat tbItemCat : list) {
			//判断是否为父节点
			if (tbItemCat.getIsParent()) {
				CatNode catNode = new CatNode();
				if (parentId == 0) {
					catNode.setName("<a href='/products/"+tbItemCat.getId()+".html'>"+tbItemCat.getName()+"</a>");
				} else {
					catNode.setName(tbItemCat.getName());
				}
				catNode.setUrl("/products/"+tbItemCat.getId()+".html");
				catNode.setItem(getCatList(tbItemCat.getId()));	//递归
				
				resultList.add(catNode);
				count ++;
				//第一层只取14条记录
				if (parentId == 0 && count >=14) {
					break;
				}
			//如果是叶子节点
			} else {
				resultList.add("/products/"+tbItemCat.getId()+".html|" + tbItemCat.getName());
			}
		}
		return resultList;
	}

}
