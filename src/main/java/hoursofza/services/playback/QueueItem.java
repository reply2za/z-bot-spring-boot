package hoursofza.services.playback;

public class QueueItem {
    private final String playableLink;

    private final String originalLink;

    public QueueItem(String link) {
        this(link, link);
    }

    public QueueItem(String playableLink, String originalLink) {
        this.playableLink = playableLink;
        this.originalLink = originalLink;
    }

    public String getPlayableLink() {
        return playableLink;
    }

    public String getOriginalLink() {
        return originalLink;
    }




}
