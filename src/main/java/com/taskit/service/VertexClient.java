package com.taskit.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class VertexClient  {

    public void callVertexEndpoint() throws IOException {
        String project = "pro-pulsar-458303-a2";
        String location = "us-central1";
        String endpointId = "3288987823864020992";

        // Load credentials from src/main/resources/key.json
        ClassPathResource resource = new ClassPathResource("key.json");
        try (InputStream inputStream = resource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);

            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (PredictionServiceClient client = PredictionServiceClient.create(settings)) {
                EndpointName endpointName = EndpointName.of(project, location, endpointId);

                // Build input instance
                Struct.Builder instance = Struct.newBuilder();
                instance.putFields("task_priority", Value.newBuilder().setStringValue("2").build());
                instance.putFields("deadline_hours", Value.newBuilder().setStringValue("7").build());
                instance.putFields("num_skills_required", Value.newBuilder().setStringValue("4").build());
                instance.putFields("employee_skill_match_count", Value.newBuilder().setStringValue("1").build());
                instance.putFields("employee_available_bandwidth", Value.newBuilder().setStringValue("6").build());
                instance.putFields("employee_skill_match_percentage", Value.newBuilder().setStringValue("50.0").build());

                PredictRequest request = PredictRequest.newBuilder()
                        .setEndpoint(endpointName.toString())
                        .addInstances(Value.newBuilder().setStructValue(instance).build())
                        .build();
                System.out.println("Request: "+ request.toString());

                PredictResponse response = client.predict(request);
                System.out.println("Prediction Response: " + response.getPredictionsList());
            }
        }
    }
}
