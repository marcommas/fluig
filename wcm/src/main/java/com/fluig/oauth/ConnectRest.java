package com.fluig.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.json.JSONObject;

@Path("/conn")
public class ConnectRest {

    // Estas KEYS deve ser cadastradas em uma OAuth Application do fluig
    public static final String FLUIG_CONSUMER_KEY = "consumer_key";
    public static final String FLUIG_CONSUMER_SECRET = "consumer_secret";

    private OAuthClient oAuthClient;
    private String domainProvider;

    private void config(String domain) {
        try {
            createConnect(domain, "usuario", "senha");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createConnect(String domain, String user, String password) throws MalformedURLException {
        domainProvider = domain;
        
        
        // Cria o client e atribui dominio e consumer keys
        //Não é necessario informar as urls de OAuth, pois esta usando as URLS do provider padrão.
        oAuthClient = new OAuthClient(domainProvider, FLUIG_CONSUMER_KEY, FLUIG_CONSUMER_SECRET);

        try {
            // O retorno da negociação e autenticação do usuário.
            LoginResult result = oAuthClient.prepareResources(user, password);
            HttpHelper.closeResource(result.getConnection());

            // Checa o código de retorno
            if (HttpHelper.returnSuccess(result.getConnection())) {
//                System.out.println(result.getResponse());
//                System.out.println("ACCESS TOKEN: " + oAuthClient.getToken());
//                System.out.println("TOKEN SECRET: " + oAuthClient.getTokenSecret());
            } else {
                System.out.println("NOK");
            }
        } catch (OAuthException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    @POST
    @Path("/dsConsulta")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postDSConsulta(String dataParam) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
    	
    	try {
    		JSONObject jsonObj = new JSONObject(dataParam);
    		
            String dataParamDomain = (String) jsonObj.get("domain"); 
            
            config(dataParamDomain);
             
            
            URL url = new URL(dataParamDomain + "/api/public/ecm/dataset/datasets");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(Constants.REQUEST_METHOD_POST);
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            oAuthClient.sign(conn);
            
            //Aqui força para buscar do dataset: NomeDataset
            String dataParams = (String) jsonObj.get("parms");  
            JSONObject jsonObjDataParam = new JSONObject(dataParams);
            jsonObjDataParam.put("name", "dsNomeDataset");
          
            String dataParameters = (String) jsonObjDataParam.toString(); 
            
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(dataParameters);
            wr.flush();
            wr.close();

            conn.connect();

            Reader inputCreateUser = new BufferedReader(new InputStreamReader(conn.getInputStream(), Constants.UTF_8_ENCODE));
            String retCreateUser = "";
            for (int c = inputCreateUser.read(); c != -1; c = inputCreateUser.read()) {
                retCreateUser += (char) c;
            }
            int code = conn.getResponseCode();
//            System.out.println(String.format("RESPONSE: %d - %s: data: %s", code, conn.getResponseMessage(), retCreateUser));

            HttpHelper.closeResource(conn);
            return Response.status(code).entity(retCreateUser).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    	
    }

}