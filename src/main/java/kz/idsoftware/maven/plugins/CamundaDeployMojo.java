package kz.idsoftware.maven.plugins;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

@Mojo(name = "deploy")
public class CamundaDeployMojo extends AbstractMojo {

    @Parameter( property = "camundaUrl", defaultValue = "http://localhost:8080/rest/deployment/create" )
    private String camundaUrl;

    public void execute() {
        File dir = new File("src/main/resources");
        try {
            deployBpmn(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deployBpmn(File dir) throws IOException {
        File [] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deployBpmn(file);
            } else {
                if (file.getName().endsWith(".bpmn")) {
                    uploadBpmn(file);
                }
            }
        }
    }

    private void uploadBpmn(File file) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(camundaUrl);
        FileBody data = new FileBody(file);
        StringBody deploymenName = new StringBody(file.getName(), ContentType.TEXT_PLAIN);
        StringBody deploymentSource = new StringBody("maven plugin", ContentType.TEXT_PLAIN);
        StringBody enableDuplicateFiltering = new StringBody("true", ContentType.TEXT_PLAIN);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("deployment-name", deploymenName)
                .addPart("deployment-source", deploymentSource)
                .addPart("enable-duplicate-filtering", enableDuplicateFiltering)
                .addPart("data", data)
                .build();

        post.setEntity(reqEntity);

        CloseableHttpResponse response = httpClient.execute(post);
        getLog().info(String.format("Uploading %s -> %s", file.getName(), response.getStatusLine().getStatusCode()));
        response.close();
    }
}
