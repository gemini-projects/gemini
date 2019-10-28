package it.at7.gemini.auth.core;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeminiSecurityConfigurationInitImpl implements GeminiSecurityConfigurationInit {

    private List<String> urls = new ArrayList<>();

    @Override
    public void addPublicUrl(String url) {
        this.urls.add(url);
    }

    public List<String> getUrls() {
        return Collections.unmodifiableList(urls);
    }
}
