package hoursofza.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DatabaseService {
    private static final Gson GSON = new Gson();
    private final String spreadsheetId;
    private final Sheets service;

    private static final String DEFAULT_CELL = "Sheet1!B2:B2";

    public DatabaseService(@Value("${spreadsheetId}") String spreadsheetId) throws GeneralSecurityException, IOException {
        this.spreadsheetId = spreadsheetId;
        service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(getCredentials()))
                .build();
    }

    /**
     * Saves data to the default cell (B2)
     * @param data The data to save.
     */
    public void update(String data) throws IOException {
        this.update(DEFAULT_CELL, List.of(List.of(data)));
    }

    public void update(String range, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
        if (result.values().isEmpty()) {
            log.warn("No cells were updated range: {} values: {}", range, values);
        }
    }

    public List<List<Object>> getData() throws IOException {
        return this.getData(DEFAULT_CELL);
    }

    public List<List<Object>> getData(String range) throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    private static GoogleCredentials getCredentials() throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream("./src/main/resources/google-sheets-service-key.json");
        return ServiceAccountCredentials.fromStream(serviceAccountStream)
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
    }

}
