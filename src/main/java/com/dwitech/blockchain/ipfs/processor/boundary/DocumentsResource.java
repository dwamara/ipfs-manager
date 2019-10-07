package com.dwitech.blockchain.ipfs.processor.boundary;

import com.dwitech.blockchain.ipfs.processor.control.Documents;
import com.dwitech.blockchain.ipfs.processor.entity.FormData;
import com.dwitech.blockchain.ipfs.processor.entity.InsertResponse;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@ApplicationScoped
@Path("/documents")
@Consumes({APPLICATION_JSON})
public class DocumentsResource {
    @Inject Documents documents;

    @POST @Path("/upload")
    @Consumes(MULTIPART_FORM_DATA) @Produces(APPLICATION_JSON)
    public Response upload(@MultipartForm FormData formData) {
        if (formData.getDocument() == null || formData.getDocument().length() == 0) {
            ResponseBuilder response = status(BAD_REQUEST);
            response.header("Reason", "No Document was uploaded (use form parameter 'file')");
            return response.build();
        }

        InsertResponse insertResponse = documents.storeAndIndex(formData);
        System.out.println(insertResponse);
        ResponseBuilder response = ok(insertResponse);
        return response.build();
    }

    @GET @Path("/{contentId}")
    @Produces(APPLICATION_JSON)
    public Response load(@PathParam("contentId") String contentId) {
        if (contentId == null || contentId.length() == 0) {
            ResponseBuilder response = status(BAD_REQUEST);
            response.header("Reason", "No contentId was given");
            return response.build();
        }

        documents.getFile(contentId);
        //System.out.println(insertResponse);
        ResponseBuilder response = ok();
        return response.build();
    }
}