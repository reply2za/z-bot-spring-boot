package hoursofza.services;

/**
 * Sample Java code for youtube.search.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/code-samples#java
 */

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Service
public class YoutubeSearchService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final YouTube youtubeService;

    YoutubeSearchService() throws GeneralSecurityException, IOException {
        youtubeService = getService();
    }

    /**
     * Searched YouTube for the provided term and returns the first video result.
     *
     * @param searchTerm The term to search for.
     * @return A video link.
     */
    public String searchAndGetLink(String searchTerm) {
        try {
            YouTube.Search.List request = youtubeService.search()
                    .list("snippet");
            SearchListResponse response = request.setMaxResults(25L)
                    .setQ(searchTerm)
                    .execute();
            Optional<SearchResult> searchResultOptional = response.getItems().parallelStream().filter(item -> item.getId().getVideoId() != null).findFirst();
            return searchResultOptional.map(searchResult -> searchResult.getId().getVideoId()).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials g = ServiceAccountCredentials.fromStream(new FileInputStream("./src/main/resources/google-sheets-service-key.json"))
                .createScoped(List.of(YouTubeScopes.YOUTUBE_READONLY));
        return new YouTube.Builder(
                httpTransport,
                JSON_FACTORY,
                new HttpCredentialsAdapter(g))
                .setApplicationName("z-bot-spring-boot")
                .build();
    }


}


