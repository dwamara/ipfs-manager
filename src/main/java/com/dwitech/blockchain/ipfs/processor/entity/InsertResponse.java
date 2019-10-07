package com.dwitech.blockchain.ipfs.processor.entity;

import java.util.HashMap;
import java.util.Map;

public class InsertResponse {
	private String contentId;
	private Map<String, Object> fields = new HashMap<>();
	private String indexDocumentId;
	private String indexName;

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getContentId() {
		return contentId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
	public void setField(String key, Object object) {
		this.fields.put(key, object);
	}

	@Override
	public String toString() {
		return "InsertResponse{" +
				       "indexName='" + indexName + '\'' +
				       "indexDocumentId='" + indexDocumentId + '\'' +
				       "contentId='" + contentId + '\'' +
				       ", fields=" + fields +
				       '}';
	}

	public void setIndexDocumentId(String indexDocumentId) {
		this.indexDocumentId = indexDocumentId;
	}
	public String getIndexDocumentId() {
		return indexDocumentId;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getIndexName() {
		return indexName;
	}
}
