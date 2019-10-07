package com.dwitech.blockchain.ipfs.processor.entity;

import org.apache.tika.Tika;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import java.io.File;
import java.io.IOException;

public class FormData {
    private File document;
    private String clientId;
    private String documentType;
    private String uploader;

    @FormParam("document") @PartType("application/octet-stream")
    public void setDocument(File document) { this.document = document; }
    public File getDocument() {
        return document;
    }

    @FormParam("clientId")
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientId() { return clientId; }

    public String getFileContentType() {
        try {
            return new Tika().detect(document);
        } catch (IOException e) {
            return ""; // maybe return PDF
        }
    }

    @FormParam("uploader")
    public void setUploader(String uploader) { this.uploader = uploader; }
    public String getUploader() { return uploader; }

    @FormParam("documentType")
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDocumentType() { return documentType; }
}