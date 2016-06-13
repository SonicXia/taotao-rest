package com.taotao.rest.solrj;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;


public class SolrJTest {
	
	@Test
	public void addDocument() throws Exception{
		//创建一连接
		HttpSolrServer solrServer = new HttpSolrServer("http://192.168.3.96:8080/solr");
		//创建一文档对象
		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", "test01");
		document.addField("item_title", "测试商品2");
		document.addField("item_price", 1234562);
		//把文档对象写入索引库
		solrServer.add(document);
		//提交
		solrServer.commit();
	}
	
	@Test
	public void deleteDocument() throws Exception {
		HttpSolrServer solrServer = new HttpSolrServer("http://192.168.3.96:8080/solr");
//		solrServer.deleteById("test01");
		solrServer.deleteByQuery("*:*");
		solrServer.commit();
	}
}
